/*
 * Copyright 2008-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import groovy.transform.TupleConstructor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @TupleConstructor annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class TupleConstructorASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = TupleConstructor.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode LHMAP_TYPE = makeWithoutCaching(LinkedHashMap.class, false);
    private static final ClassNode HMAP_TYPE = makeWithoutCaching(HashMap.class, false);
    private static final ClassNode CHECK_METHOD_TYPE = make(ImmutableASTTransformation.class);
    private static Map<Class<?>, Expression> primitivesInitialValues;

    static {
        final ConstantExpression zero = constX(0);
        final ConstantExpression zeroDecimal = constX(.0);
        primitivesInitialValues = new HashMap<Class<?>, Expression>();
        primitivesInitialValues.put(int.class, zero);
        primitivesInitialValues.put(long.class, zero);
        primitivesInitialValues.put(short.class, zero);
        primitivesInitialValues.put(byte.class, zero);
        primitivesInitialValues.put(char.class, zero);
        primitivesInitialValues.put(float.class, zeroDecimal);
        primitivesInitialValues.put(double.class, zeroDecimal);
        primitivesInitialValues.put(boolean.class, ConstantExpression.FALSE);
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            boolean includeProperties = !memberHasValue(anno, "includeProperties", false);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", true);
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", true);
            boolean callSuper = memberHasValue(anno, "callSuper", true);
            boolean force = memberHasValue(anno, "force", true);
            List<String> excludes = getMemberList(anno, "excludes");
            List<String> includes = getMemberList(anno, "includes");
            if (hasAnnotation(cNode, CanonicalASTTransformation.MY_TYPE)) {
                AnnotationNode canonical = cNode.getAnnotations(CanonicalASTTransformation.MY_TYPE).get(0);
                if (excludes == null || excludes.isEmpty()) excludes = getMemberList(canonical, "excludes");
                if (includes == null || includes.isEmpty()) includes = getMemberList(canonical, "includes");
            }
            if (!checkIncludeExclude(anno, excludes, includes, MY_TYPE_NAME)) return;
            createConstructor(cNode, includeFields, includeProperties, includeSuperFields, includeSuperProperties, callSuper, force, excludes, includes);
        }
    }

    public static void createConstructor(ClassNode cNode, boolean includeFields, boolean includeProperties, boolean includeSuperFields, boolean includeSuperProperties, boolean callSuper, boolean force, List<String> excludes, List<String> includes) {
        // no processing if existing constructors found
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        if (constructors.size() > 1 && !force) return;
        boolean foundEmpty = constructors.size() == 1 && constructors.get(0).getFirstStatement() == null;
        if (constructors.size() == 1 && !foundEmpty && !force) return;
        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        if (foundEmpty) constructors.remove(0);

        List<FieldNode> superList = new ArrayList<FieldNode>();
        if (includeSuperProperties) {
            superList.addAll(getSuperPropertyFields(cNode.getSuperClass()));
        }
        if (includeSuperFields) {
            superList.addAll(getSuperNonPropertyFields(cNode.getSuperClass()));
        }

        List<FieldNode> list = new ArrayList<FieldNode>();
        if (includeProperties) {
            list.addAll(getInstancePropertyFields(cNode));
        }
        if (includeFields) {
            list.addAll(getInstanceNonPropertyFields(cNode));
        }

        final List<Parameter> params = new ArrayList<Parameter>();
        final List<Expression> superParams = new ArrayList<Expression>();
        final BlockStatement body = new BlockStatement();
        for (FieldNode fNode : superList) {
            String name = fNode.getName();
            if (shouldSkip(name, excludes, includes)) continue;
            params.add(createParam(fNode, name));
            if (callSuper) {
                superParams.add(varX(name));
            } else {
                body.addStatement(assignS(propX(varX("this"), name), varX(name)));
            }
        }
        if (callSuper) {
            body.addStatement(stmt(ctorX(ClassNode.SUPER, args(superParams))));
        }
        for (FieldNode fNode : list) {
            String name = fNode.getName();
            if (shouldSkip(name, excludes, includes)) continue;
            params.add(createParam(fNode, name));
            body.addStatement(assignS(propX(varX("this"), name), varX(name)));
        }
        cNode.addConstructor(new ConstructorNode(ACC_PUBLIC, params.toArray(new Parameter[params.size()]), ClassNode.EMPTY_ARRAY, body));
        // add map constructor if needed, don't do it for LinkedHashMap for now (would lead to duplicate signature)
        // or if there is only one Map property (for backwards compatibility)
        if (params.size() > 0) {
            ClassNode firstParam = params.get(0).getType();
            if (params.size() > 1 || firstParam.equals(ClassHelper.OBJECT_TYPE)) {
                if (firstParam.equals(ClassHelper.MAP_TYPE)) {
                    addMapConstructors(cNode, true, "The class " + cNode.getName() + " was incorrectly initialized via the map constructor with null.");
                } else {
                    ClassNode candidate = HMAP_TYPE;
                    while (candidate != null) {
                        if (candidate.equals(firstParam)) {
                            addMapConstructors(cNode, true, "The class " + cNode.getName() + " was incorrectly initialized via the map constructor with null.");
                            break;
                        }
                        candidate = candidate.getSuperClass();
                    }
                }
            }
        }
    }

    private static Parameter createParam(FieldNode fNode, String name) {
        Parameter param = new Parameter(fNode.getType(), name);
        param.setInitialExpression(providedOrDefaultInitialValue(fNode));
        return param;
    }

    private static Expression providedOrDefaultInitialValue(FieldNode fNode) {
        Expression initialExp = fNode.getInitialExpression() != null ? fNode.getInitialExpression() : ConstantExpression.NULL;
        final ClassNode paramType = fNode.getType();
        if (ClassHelper.isPrimitiveType(paramType) && initialExp.equals(ConstantExpression.NULL)) {
            initialExp = primitivesInitialValues.get(paramType.getTypeClass());
        }
        return initialExp;
    }

    public static void addMapConstructors(ClassNode cNode, boolean hasNoArg, String message) {
        Parameter[] parameters = params(new Parameter(LHMAP_TYPE, "__namedArgs"));
        BlockStatement code = new BlockStatement();
        VariableExpression namedArgs = varX("__namedArgs");
        namedArgs.setAccessedVariable(parameters[0]);
        code.addStatement(ifElseS(equalsNullX(namedArgs),
                illegalArgumentBlock(message),
                processArgsBlock(cNode, namedArgs)));
        ConstructorNode init = new ConstructorNode(ACC_PUBLIC, parameters, ClassNode.EMPTY_ARRAY, code);
        cNode.addConstructor(init);
        // add a no-arg constructor too
        if (!hasNoArg) {
            code = new BlockStatement();
            code.addStatement(stmt(ctorX(ClassNode.THIS, ctorX(LHMAP_TYPE))));
            init = new ConstructorNode(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code);
            cNode.addConstructor(init);
        }
    }

    private static BlockStatement illegalArgumentBlock(String message) {
        return block(
                new ThrowStatement(ctorX(make(IllegalArgumentException.class), args(constX(message)))));
    }

    private static BlockStatement processArgsBlock(ClassNode cNode, VariableExpression namedArgs) {
        BlockStatement block = new BlockStatement();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (pNode.isStatic()) continue;

            // if namedArgs.containsKey(propertyName) setProperty(propertyName, namedArgs.get(propertyName));
            Statement ifStatement = ifS(
                    callX(namedArgs, "containsKey", constX(pNode.getName())),
                    assignS(varX(pNode), propX(namedArgs, pNode.getName())));
            block.addStatement(ifStatement);
        }
        block.addStatement(stmt(callX(CHECK_METHOD_TYPE, "checkPropNames", args(varX("this"), namedArgs))));
        return block;
    }
}
