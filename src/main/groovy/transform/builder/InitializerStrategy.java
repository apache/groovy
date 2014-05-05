/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.builder;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.BuilderASTTransformation;
import org.codehaus.groovy.transform.ImmutableASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorThisS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.codehaus.groovy.transform.BuilderASTTransformation.NO_EXCEPTIONS;
import static org.codehaus.groovy.transform.BuilderASTTransformation.NO_PARAMS;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * This strategy is used with the {@link Builder} AST transform to create a builder helper class
 * for the fluent and type-safe creation of instances of a specified class.
 *
 * It is modelled roughly on the design outlined here:
 * http://michid.wordpress.com/2008/08/13/type-safe-builder-pattern-in-java/
 *
 * You define classes which use the type-safe initializer pattern as follows:
 * <pre>
 * import groovy.transform.builder.*
 * import groovy.transform.*
 *
 * {@code @ToString}
 * {@code @Builder}(builderStrategy=InitializerStrategy) class Person {
 *     String firstName
 *     String lastName
 *     int age
 * }
 * </pre>
 * While it isn't required to do so, the benefit of this builder strategy comes in conjunction with static type-checking or static compilation. Typical usage is as follows:
 * <pre>
 * {@code @CompileStatic}
 * def main() {
 *     println new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21))
 * }
 * </pre>
 * which prints:
 * <pre>
 * Person(John, Smith, 21)
 * </pre>
 * If you don't initialise some of the properties, your code won't compile, e.g. if the method body above was changed to this:
 * <pre>
 * println new Person(Person.createInitializer().firstName("John").lastName("Smith"))
 * </pre>
 * then the following compile-time error would result:
 * <pre>
 * [Static type checking] - Cannot find matching method Person#<init>(Person$PersonInitializer <groovy.transform.builder.InitializerStrategy$SET, groovy.transform.builder.InitializerStrategy$SET, groovy.transform.builder.InitializerStrategy$UNSET>). Please check if the declared type is right and if the method exists.
 * </pre>
 * The message is a little cryptic, but it is basically the static compiler telling us that the third parameter, {@code age} in our case, is unset.
 *
 * @author Paul King
 */
public class InitializerStrategy extends BuilderASTTransformation.AbstractBuilderStrategy {

    /**
     * Internal phantom type used by the {@code InitializerStrategy} to indicate that a property has been set. It is used in conjunction with the generated parameterized type helper class.
     */
    public static abstract class SET {
    }

    /**
     * Internal phantom type used by the {@code InitializerStrategy} to indicate that a property remains unset. It is used in conjunction with the generated parameterized type helper class.
     */
    public static abstract class UNSET {
    }

    private static final Expression DEFAULT_INITIAL_VALUE = null;

    public void build(BuilderASTTransformation transform, AnnotatedNode annotatedNode, AnnotationNode anno) {
        if (!(annotatedNode instanceof ClassNode)) {
            transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME + " processing: building for " +
                    annotatedNode.getClass().getSimpleName() + " not supported by " + getClass().getSimpleName(), annotatedNode);
            return;
        }
        ClassNode buildee = (ClassNode) annotatedNode;
        List<String> excludes = new ArrayList<String>();
        List<String> includes = new ArrayList<String>();
        if (!getIncludeExclude(transform, anno, buildee, excludes, includes)) return;
        String prefix = transform.getMemberStringValue(anno, "prefix", "");
        if (unsupportedAttribute(transform, anno, "forClass")) return;
        String builderClassName = transform.getMemberStringValue(anno, "builderClassName", buildee.getName() + "Initializer");
        String buildMethodName = transform.getMemberStringValue(anno, "buildMethodName", "create");
        List<FieldNode> fields = getInstancePropertyFields(buildee);
        List<FieldNode> filteredFields = selectFieldsFromExistingClass(fields, includes, excludes);
        int numFields = filteredFields.size();
        ClassNode builder = createInnerHelperClass(buildee, builderClassName, filteredFields);
        createBuilderConstructors(builder, filteredFields);
        createBuildeeConstructors(transform, buildee, builder, filteredFields);
        buildee.getModule().addClass(builder);
        buildee.addMethod(createBuilderMethod(transform, anno, buildMethodName, builder, numFields));
        for (int i = 0; i < numFields; i++) {
            builder.addField(createFieldCopy(buildee, filteredFields.get(i)));
            builder.addMethod(createBuilderMethodForField(builder, filteredFields, prefix, i));
        }
        builder.addMethod(createBuildMethod(builder, buildMethodName, filteredFields));
    }

    private ClassNode createInnerHelperClass(ClassNode buildee, String builderClassName, List<FieldNode> fields) {
        final String fullName = buildee.getName() + "$" + builderClassName;
        final int visibility = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC;
        ClassNode builder = new InnerClassNode(buildee, fullName, visibility, ClassHelper.OBJECT_TYPE);
        GenericsType[] gtypes = new GenericsType[fields.size()];
        for (int i = 0; i < gtypes.length; i++) {
            gtypes[i] = makePlaceholder(i);
        }
        builder.setGenericsTypes(gtypes);
        return builder;
    }

    private static MethodNode createBuilderMethod(BuilderASTTransformation transform, AnnotationNode anno, String buildMethodName, ClassNode builder, int numFields) {
        String builderMethodName = transform.getMemberStringValue(anno, "builderMethodName", "createInitializer");
        final BlockStatement body = new BlockStatement();
        body.addStatement(returnS(callX(builder, buildMethodName)));
        final int visibility = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC;
        ClassNode returnType = makeClassSafeWithGenerics(builder, unsetGenTypes(numFields));
        return new MethodNode(builderMethodName, visibility, returnType, NO_PARAMS, NO_EXCEPTIONS, body);
    }

    private static GenericsType[] unsetGenTypes(int numFields) {
        GenericsType[] gtypes = new GenericsType[numFields];
        for (int i = 0; i < gtypes.length; i++) {
            gtypes[i] = new GenericsType(ClassHelper.make(UNSET.class));
        }
        return gtypes;
    }

    private static GenericsType[] setGenTypes(int numFields) {
        GenericsType[] gtypes = new GenericsType[numFields];
        for (int i = 0; i < gtypes.length; i++) {
            gtypes[i] = new GenericsType(ClassHelper.make(SET.class));
        }
        return gtypes;
    }

    private static void createBuilderConstructors(ClassNode builder, List<FieldNode> fields) {
        builder.addConstructor(ACC_PRIVATE, NO_PARAMS, NO_EXCEPTIONS, block(ctorSuperS()));
        final BlockStatement body = new BlockStatement();
        body.addStatement(ctorSuperS());
        initializeFields(fields, body);
        builder.addConstructor(ACC_PRIVATE, getParams(fields), NO_EXCEPTIONS, body);
    }

    private static void createBuildeeConstructors(BuilderASTTransformation transform, ClassNode buildee, ClassNode builder, List<FieldNode> fields) {
        ClassNode paramType = makeClassSafeWithGenerics(builder, setGenTypes(fields.size()));
        List<Expression> argsList = new ArrayList<Expression>();
        for (FieldNode fieldNode : fields) {
            argsList.add(propX(varX("initializer"), fieldNode.getName()));
        }
        Expression args = new ArgumentListExpression(argsList);
        buildee.addConstructor(ACC_PUBLIC | ACC_SYNTHETIC, params(param(paramType, "initializer")), NO_EXCEPTIONS, block(ctorThisS(args)));
        if (!transform.hasAnnotation(buildee, ImmutableASTTransformation.MY_TYPE)) {
            final BlockStatement body = new BlockStatement();
            body.addStatement(ctorSuperS());
            initializeFields(fields, body);
            buildee.addConstructor(ACC_PRIVATE | ACC_SYNTHETIC, getParams(fields), NO_EXCEPTIONS, body);
        }
    }

    private static Parameter[] getParams(List<FieldNode> fields) {
        Parameter[] parameters = new Parameter[fields.size()];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = new Parameter(newClass(fields.get(i).getType()), fields.get(i).getName());
        }
        return parameters;
    }

    private static MethodNode createBuildMethod(ClassNode builder, String buildMethodName, List<FieldNode> fields) {
        ClassNode returnType = makeClassSafeWithGenerics(builder, unsetGenTypes(fields.size()));
        return new MethodNode(buildMethodName, ACC_PUBLIC | ACC_STATIC, returnType, NO_PARAMS, NO_EXCEPTIONS, block(returnS(ctorX(returnType))));
    }

    private MethodNode createBuilderMethodForField(ClassNode builder, List<FieldNode> fields, String prefix, int fieldPos) {
        String fieldName = fields.get(fieldPos).getName();
        String setterName = getSetterName(prefix, fieldName);
        GenericsType[] gtypes = new GenericsType[fields.size()];
        List<Expression> argList = new ArrayList<Expression>();
        for (int i = 0; i < fields.size(); i++) {
            gtypes[i] = i == fieldPos ? new GenericsType(ClassHelper.make(SET.class)) : makePlaceholder(i);
            argList.add(i == fieldPos ? propX(varX("this"), constX(fieldName)) : varX(fields.get(i).getName()));
        }
        ClassNode returnType = makeClassSafeWithGenerics(builder, gtypes);
        return new MethodNode(setterName, ACC_PUBLIC, returnType, params(param(fields.get(fieldPos).getType(), fieldName)), NO_EXCEPTIONS, block(
                stmt(assignX(propX(varX("this"), constX(fieldName)), varX(fieldName))),
                returnS(ctorX(returnType, args(argList)))
        ));
    }

    private GenericsType makePlaceholder(int i) {
        ClassNode type = ClassHelper.makeWithoutCaching("T" + i);
        type.setRedirect(ClassHelper.OBJECT_TYPE);
        type.setGenericsPlaceHolder(true);
        return new GenericsType(type);
    }

    private static FieldNode createFieldCopy(ClassNode buildee, FieldNode fNode) {
        return new FieldNode(fNode.getName(), fNode.getModifiers(), ClassHelper.make(fNode.getType().getName()), buildee, DEFAULT_INITIAL_VALUE);
    }

    private static List<FieldNode> selectFieldsFromExistingClass(List<FieldNode> fieldNodes, List<String> includes, List<String> excludes) {
        List<FieldNode> fields = new ArrayList<FieldNode>();
        for (FieldNode fNode : fieldNodes) {
            if (AbstractASTTransformation.shouldSkip(fNode.getName(), excludes, includes)) continue;
            fields.add(fNode);
        }
        return fields;
    }

    private static void initializeFields(List<FieldNode> fields, BlockStatement body) {
        for (FieldNode field : fields) {
            body.addStatement(stmt(assignX(propX(varX("this"), field.getName()), varX(field))));
        }
    }
}
