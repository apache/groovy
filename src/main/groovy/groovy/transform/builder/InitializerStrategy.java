/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.transform.builder;

import groovy.transform.TupleConstructor;
import groovy.transform.Undefined;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.BuilderASTTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.markAsGenerated;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorThisS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractSuperClassGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics;
import static org.codehaus.groovy.transform.AbstractASTTransformation.getMemberStringValue;
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
 * {@code
 * [Static type checking] - Cannot find matching method Person#<init>(Person$PersonInitializer <groovy.transform.builder.InitializerStrategy$SET, groovy.transform.builder.InitializerStrategy$SET, groovy.transform.builder.InitializerStrategy$UNSET>). Please check if the declared type is correct and if the method exists.
 * }
 * </pre>
 * The message is a little cryptic, but it is basically the static compiler telling us that the third parameter, {@code age} in our case, is unset.
 *
 * You can also add this annotation to your predefined constructors. These will be made private and an initializer will be set up
 * to call your constructor. Any parameters to your constructor become the properties expected by the initializer.
 * If you use such a builder on a constructor as well as on the class or on more than one constructor, then it is up to you
 * to define unique values for 'builderClassName' and 'builderMethodName' for each annotation.
 */
public class InitializerStrategy extends BuilderASTTransformation.AbstractBuilderStrategy {

    /**
     * Internal phantom type used by the {@code InitializerStrategy} to indicate that a property has been set. It is used in conjunction with the generated parameterized type helper class.
     */
    public abstract static class SET {
    }

    /**
     * Internal phantom type used by the {@code InitializerStrategy} to indicate that a property remains unset. It is used in conjunction with the generated parameterized type helper class.
     */
    public abstract static class UNSET {
    }

    private static final int PUBLIC_STATIC = ACC_PUBLIC | ACC_STATIC;
    private static final Expression DEFAULT_INITIAL_VALUE = null;
    private static final ClassNode TUPLECONS_TYPE = ClassHelper.make(TupleConstructor.class);

    public void build(BuilderASTTransformation transform, AnnotatedNode annotatedNode, AnnotationNode anno) {
        if (unsupportedAttribute(transform, anno, "forClass")) return;
        if (unsupportedAttribute(transform, anno, "allProperties")) return;
        boolean useSetters = transform.memberHasValue(anno, "useSetters", true);
        boolean allNames = transform.memberHasValue(anno, "allNames", true);
        boolean force = transform.memberHasValue(anno, "force", true);
        if (annotatedNode instanceof ClassNode) {
            createBuilderForAnnotatedClass(transform, (ClassNode) annotatedNode, anno, useSetters, allNames, force);
        } else if (annotatedNode instanceof MethodNode) {
            createBuilderForAnnotatedMethod(transform, (MethodNode) annotatedNode, anno, useSetters);
        }
    }

    private void createBuilderForAnnotatedClass(BuilderASTTransformation transform, ClassNode buildee, AnnotationNode anno, boolean useSetters, boolean allNames, boolean force) {
        List<String> excludes = new ArrayList<String>();
        List<String> includes = new ArrayList<String>();
        includes.add(Undefined.STRING);
        if (!getIncludeExclude(transform, anno, buildee, excludes, includes)) return;
        if (includes.size() == 1 && Undefined.isUndefined(includes.get(0))) includes = null;
        List<FieldNode> fields = getFields(transform, anno, buildee);
        List<FieldNode> filteredFields = filterFields(fields, includes, excludes, allNames);
        if (filteredFields.isEmpty()) {
            transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME +
                    " processing: at least one property is required for this strategy", anno);
        }
        ClassNode builder = createInnerHelperClass(buildee, getBuilderClassName(buildee, anno), filteredFields.size());
        addFields(buildee, filteredFields, builder);

        buildCommon(buildee, anno, filteredFields, builder);
        boolean needsConstructor = !AnnotatedNodeUtils.hasAnnotation(buildee, TUPLECONS_TYPE) || force;
        createBuildeeConstructors(transform, buildee, builder, filteredFields, needsConstructor, useSetters);
    }

    private void createBuilderForAnnotatedMethod(BuilderASTTransformation transform, MethodNode mNode, AnnotationNode anno, boolean useSetters) {
        if (transform.getMemberValue(anno, "includes") != null || transform.getMemberValue(anno, "excludes") != null) {
            transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME +
                    " processing: includes/excludes only allowed on classes", anno);
        }
        if (mNode instanceof ConstructorNode) {
            mNode.setModifiers(ACC_PRIVATE | ACC_SYNTHETIC);
        } else {
            if ((mNode.getModifiers() & ACC_STATIC) == 0) {
                transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME +
                        " processing: method builders only allowed on static methods", anno);
            }
            mNode.setModifiers(ACC_PRIVATE | ACC_SYNTHETIC | ACC_STATIC);
        }
        ClassNode buildee = mNode.getDeclaringClass();
        Parameter[] parameters = mNode.getParameters();
        if (parameters.length == 0) {
            transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME +
                    " processing: at least one parameter is required for this strategy", anno);
        }
        ClassNode builder = createInnerHelperClass(buildee, getBuilderClassName(buildee, anno), parameters.length);
        List<FieldNode> convertedFields = convertParamsToFields(builder, parameters);

        buildCommon(buildee, anno, convertedFields, builder);
        if (mNode instanceof ConstructorNode) {
            createBuildeeConstructors(transform, buildee, builder, convertedFields, false, useSetters);
        } else {
            createBuildeeMethods(buildee, mNode, builder, convertedFields);
        }
    }

    private static String getBuilderClassName(ClassNode buildee, AnnotationNode anno) {
        return getMemberStringValue(anno, "builderClassName", buildee.getNameWithoutPackage() + "Initializer");
    }

    private static void addFields(ClassNode buildee, List<FieldNode> filteredFields, ClassNode builder) {
        for (FieldNode filteredField : filteredFields) {
            builder.addField(createFieldCopy(buildee, filteredField));
        }
    }

    private void buildCommon(ClassNode buildee, AnnotationNode anno, List<FieldNode> fieldNodes, ClassNode builder) {
        String prefix = getMemberStringValue(anno, "prefix", "");
        String buildMethodName = getMemberStringValue(anno, "buildMethodName", "create");
        createBuilderConstructors(builder, buildee, fieldNodes);
        buildee.getModule().addClass(builder);
        String builderMethodName = getMemberStringValue(anno, "builderMethodName", "createInitializer");
        buildee.addMethod(createBuilderMethod(buildMethodName, builder, fieldNodes.size(), builderMethodName));
        for (int i = 0; i < fieldNodes.size(); i++) {
            builder.addMethod(createBuilderMethodForField(builder, fieldNodes, prefix, i));
        }
        builder.addMethod(createBuildMethod(builder, buildMethodName, fieldNodes));
    }

    private static List<FieldNode> convertParamsToFields(ClassNode builder, Parameter[] parameters) {
        List<FieldNode> fieldNodes = new ArrayList<FieldNode>();
        for(Parameter parameter: parameters) {
            Map<String,ClassNode> genericsSpec = createGenericsSpec(builder);
            ClassNode correctedType = correctToGenericsSpecRecurse(genericsSpec, parameter.getType());
            FieldNode fieldNode = new FieldNode(parameter.getName(), parameter.getModifiers(), correctedType, builder, DEFAULT_INITIAL_VALUE);
            fieldNodes.add(fieldNode);
            builder.addField(fieldNode);
        }
        return fieldNodes;
    }

    private static ClassNode createInnerHelperClass(ClassNode buildee, String builderClassName, int fieldsSize) {
        final String fullName = buildee.getName() + "$" + builderClassName;
        ClassNode builder = new InnerClassNode(buildee, fullName, PUBLIC_STATIC, OBJECT_TYPE);
        GenericsType[] gtypes = new GenericsType[fieldsSize];
        for (int i = 0; i < gtypes.length; i++) {
            gtypes[i] = makePlaceholder(i);
        }
        builder.setGenericsTypes(gtypes);
        return builder;
    }

    private static MethodNode createBuilderMethod(String buildMethodName, ClassNode builder, int numFields, String builderMethodName) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(returnS(callX(builder, buildMethodName)));
        ClassNode returnType = makeClassSafeWithGenerics(builder, unsetGenTypes(numFields));
        return new MethodNode(builderMethodName, PUBLIC_STATIC, returnType, NO_PARAMS, NO_EXCEPTIONS, body);
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

    private static void createBuilderConstructors(ClassNode builder, ClassNode buildee, List<FieldNode> fields) {
        builder.addConstructor(ACC_PRIVATE, NO_PARAMS, NO_EXCEPTIONS, block(ctorSuperS()));
        final BlockStatement body = new BlockStatement();
        body.addStatement(ctorSuperS());
        initializeFields(fields, body, false);
        builder.addConstructor(ACC_PRIVATE, getParams(fields, buildee), NO_EXCEPTIONS, body);
    }

    private static void createBuildeeConstructors(BuilderASTTransformation transform, ClassNode buildee, ClassNode builder, List<FieldNode> fields, boolean needsConstructor, boolean useSetters) {
        ConstructorNode initializer = createInitializerConstructor(buildee, builder, fields);
        markAsGenerated(buildee, initializer);
        if (needsConstructor) {
            final BlockStatement body = new BlockStatement();
            body.addStatement(ctorSuperS());
            initializeFields(fields, body, useSetters);
            ConstructorNode helperCons = buildee.addConstructor(ACC_PRIVATE | ACC_SYNTHETIC, getParams(fields, buildee), NO_EXCEPTIONS, body);
            markAsGenerated(buildee, helperCons);
        }
    }

    private static void createBuildeeMethods(ClassNode buildee, MethodNode mNode, ClassNode builder, List<FieldNode> fields) {
        ClassNode paramType = makeClassSafeWithGenerics(builder, setGenTypes(fields.size()));
        List<Expression> argsList = new ArrayList<Expression>();
        Parameter initParam = param(paramType, "initializer");
        for (FieldNode fieldNode : fields) {
            argsList.add(propX(varX(initParam), fieldNode.getName()));
        }
        String newName = "$" + mNode.getName(); // can't have private and public methods of the same name, so rename original
        buildee.addMethod(mNode.getName(), PUBLIC_STATIC, mNode.getReturnType(), params(param(paramType, "initializer")), NO_EXCEPTIONS,
                block(stmt(callX(buildee, newName, args(argsList)))));
        renameMethod(buildee, mNode, newName);
    }

    // no rename so delete and add
    private static void renameMethod(ClassNode buildee, MethodNode mNode, String newName) {
        addGeneratedMethod(buildee, newName, mNode.getModifiers(), mNode.getReturnType(), mNode.getParameters(), mNode.getExceptions(), mNode.getCode());
        buildee.removeMethod(mNode);
    }

    private static Parameter[] getParams(List<FieldNode> fields, ClassNode cNode) {
        Parameter[] parameters = new Parameter[fields.size()];
        for (int i = 0; i < parameters.length; i++) {
            FieldNode fNode = fields.get(i);
            Map<String,ClassNode> genericsSpec = createGenericsSpec(fNode.getDeclaringClass());
            extractSuperClassGenerics(fNode.getType(), cNode, genericsSpec);
            ClassNode correctedType = correctToGenericsSpecRecurse(genericsSpec, fNode.getType());
            parameters[i] = new Parameter(correctedType, fNode.getName());
        }
        return parameters;
    }

    private static ConstructorNode createInitializerConstructor(ClassNode buildee, ClassNode builder, List<FieldNode> fields) {
        ClassNode paramType = makeClassSafeWithGenerics(builder, setGenTypes(fields.size()));
        List<Expression> argsList = new ArrayList<Expression>();
        Parameter initParam = param(paramType, "initializer");
        for (FieldNode fieldNode : fields) {
            argsList.add(propX(varX(initParam), fieldNode.getName()));
        }
        return buildee.addConstructor(ACC_PUBLIC, params(param(paramType, "initializer")), NO_EXCEPTIONS, block(ctorThisS(args(argsList))));
    }

    private static MethodNode createBuildMethod(ClassNode builder, String buildMethodName, List<FieldNode> fields) {
        ClassNode returnType = makeClassSafeWithGenerics(builder, unsetGenTypes(fields.size()));
        return new MethodNode(buildMethodName, PUBLIC_STATIC, returnType, NO_PARAMS, NO_EXCEPTIONS, block(returnS(ctorX(returnType))));
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
        FieldNode fNode = fields.get(fieldPos);
        Map<String,ClassNode> genericsSpec = createGenericsSpec(fNode.getDeclaringClass());
        extractSuperClassGenerics(fNode.getType(), builder, genericsSpec);
        ClassNode correctedType = correctToGenericsSpecRecurse(genericsSpec, fNode.getType());
        return new MethodNode(setterName, ACC_PUBLIC, returnType, params(param(correctedType, fieldName)), NO_EXCEPTIONS, block(
                stmt(assignX(propX(varX("this"), constX(fieldName)), varX(fieldName, correctedType))),
                returnS(ctorX(returnType, args(argList)))
        ));
    }

    private static GenericsType makePlaceholder(int i) {
        ClassNode type = ClassHelper.makeWithoutCaching("T" + i);
        type.setRedirect(OBJECT_TYPE);
        type.setGenericsPlaceHolder(true);
        return new GenericsType(type);
    }

    private static FieldNode createFieldCopy(ClassNode buildee, FieldNode fNode) {
        Map<String,ClassNode> genericsSpec = createGenericsSpec(fNode.getDeclaringClass());
        extractSuperClassGenerics(fNode.getType(), buildee, genericsSpec);
        ClassNode correctedType = correctToGenericsSpecRecurse(genericsSpec, fNode.getType());
        return new FieldNode(fNode.getName(), fNode.getModifiers(), correctedType, buildee, DEFAULT_INITIAL_VALUE);
    }

    private static List<FieldNode> filterFields(List<FieldNode> fieldNodes, List<String> includes, List<String> excludes, boolean allNames) {
        List<FieldNode> fields = new ArrayList<FieldNode>();
        for (FieldNode fNode : fieldNodes) {
            if (AbstractASTTransformation.shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            fields.add(fNode);
        }
        return fields;
    }

    private static void initializeFields(List<FieldNode> fields, BlockStatement body, boolean useSetters) {
        for (FieldNode field : fields) {
            String name = field.getName();
            body.addStatement(
                    stmt(useSetters && !field.isFinal()
                                    ? callThisX(getSetterName(name), varX(param(field.getType(), name)))
                                    : assignX(propX(varX("this"), field.getName()), varX(param(field.getType(), name)))
                    )
            );
        }
    }

    private static String getSetterName(String name) {
        return "set" + Verifier.capitalize(name);
    }
}
