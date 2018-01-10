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
package org.codehaus.groovy.transform;

import groovy.transform.MapConstructor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.hasNoArgConstructor;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.copyStatementsWithSuperAdjustment;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @MapConstructor annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MapConstructorASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = MapConstructor.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false);
//    private static final ClassNode IMMUTABLE_CLASS_TYPE = makeWithoutCaching(KnownImmutable.class, false);
//    private static final ClassNode IMMUTABLE_BASE_TYPE = makeWithoutCaching(ImmutableBase.class, false);
//    private static final ClassNode CHECK_METHOD_TYPE = make(ImmutableASTTransformation.class);

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
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", true);
            boolean useSetters = memberHasValue(anno, "useSetters", true);
            boolean allProperties = memberHasValue(anno, "allProperties", true);
            boolean noArg = memberHasValue(anno, "noArg", true);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            boolean allNames = memberHasValue(anno, "allNames", true);
            boolean makeImmutable = memberHasValue(anno, "makeImmutable", true);
            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes, "includes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, false))
                return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, false))
                return;

            Expression pre = anno.getMember("pre");
            if (pre != null && !(pre instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'pre'. Found " + pre, cNode);
                return;
            }
            Expression post = anno.getMember("post");
            if (post != null && !(post instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'post'. Found " + post, cNode);
                return;
            }

            createConstructors(this, cNode, includeFields, includeProperties, includeSuperProperties, useSetters, noArg, allNames, allProperties, makeImmutable, excludes, includes, (ClosureExpression) pre, (ClosureExpression) post, source);

            if (pre != null) {
                anno.setMember("pre", new ClosureExpression(new Parameter[0], new EmptyStatement()));
            }
            if (post != null) {
                anno.setMember("post", new ClosureExpression(new Parameter[0], new EmptyStatement()));
            }
        }
    }

    private static void createConstructors(AbstractASTTransformation xform, ClassNode cNode, boolean includeFields, boolean includeProperties, boolean includeSuperProperties, boolean useSetters, boolean noArg, boolean allNames, boolean allProperties, boolean makeImmutable, List<String> excludes, List<String> includes, ClosureExpression pre, ClosureExpression post, SourceUnit source) {
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        boolean foundEmpty = constructors.size() == 1 && constructors.get(0).getFirstStatement() == null;
        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        if (foundEmpty) constructors.remove(0);

        // TODO remove duplicated code from alternative paths below
        if (makeImmutable) {
            List<PropertyNode> list = ImmutableASTTransformation.getProperties(cNode, includeSuperProperties, allProperties);
            boolean specialHashMapCase = ImmutableASTTransformation.isSpecialHashMapCase(list);
            if (specialHashMapCase) {
                ImmutableASTTransformation.createConstructorMapSpecial(cNode, list);
            } else {
                ImmutableASTTransformation.createConstructorMap(xform, cNode, list);
            }
            return;
        }

        List<FieldNode> superList = new ArrayList<FieldNode>();
        if (includeSuperProperties) {
            superList.addAll(getSuperPropertyFields(cNode.getSuperClass()));
        }

        List<FieldNode> list = new ArrayList<FieldNode>();
        if (includeProperties) {
            list.addAll(getInstancePropertyFields(cNode));
        }
        if (includeFields) {
            list.addAll(getInstanceNonPropertyFields(cNode));
        }

        Parameter map = param(MAP_TYPE, "args");
        final BlockStatement body = new BlockStatement();
        ClassCodeExpressionTransformer transformer = makeMapTypedArgsTransformer();
        if (pre != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(pre);
            copyStatementsWithSuperAdjustment(transformed, body);
        }
        final BlockStatement inner = new BlockStatement();
        for (FieldNode fNode : superList) {
            String name = fNode.getName();
            if (shouldSkip(name, excludes, includes, allNames)) continue;
            assignField(useSetters, map, inner, name);
        }
        for (FieldNode fNode : list) {
            String name = fNode.getName();
            if (shouldSkip(name, excludes, includes, allNames)) continue;
            assignField(useSetters, map, inner, name);
        }
        body.addStatement(ifS(notNullX(varX("args")), inner));
        if (post != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(post);
            body.addStatement(transformed.getCode());
        }
        cNode.addConstructor(new ConstructorNode(ACC_PUBLIC, params(map), ClassNode.EMPTY_ARRAY, body));
        if (noArg && !(list.isEmpty() && superList.isEmpty()) && !hasNoArgConstructor(cNode)) {
            createNoArgConstructor(cNode);
        }
    }

    private static void createNoArgConstructor(ClassNode cNode) {
        Statement body = stmt(ctorX(ClassNode.THIS, args(new MapExpression())));
        cNode.addConstructor(new ConstructorNode(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static void assignField(boolean useSetters, Parameter map, BlockStatement body, String name) {
        ArgumentListExpression nameArg = args(constX(name));
        body.addStatement(ifS(callX(varX(map), "containsKey", nameArg), useSetters ?
                stmt(callThisX(getSetterName(name), callX(varX(map), "get", nameArg))) :
                assignS(propX(varX("this"), name), callX(varX(map), "get", nameArg))));
    }

    private static ClassCodeExpressionTransformer makeMapTypedArgsTransformer() {
        return new ClassCodeExpressionTransformer() {
            @Override
            public Expression transform(Expression exp) {
                if (exp instanceof ClosureExpression) {
                    ClosureExpression ce = (ClosureExpression) exp;
                    ce.getCode().visit(this);
                } else if (exp instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression) exp;
                    if (ve.getName().equals("args") && ve.getAccessedVariable() instanceof DynamicVariable) {
                        VariableExpression newVe = new VariableExpression(new Parameter(MAP_TYPE, "args"));
                        newVe.setSourcePosition(ve);
                        return newVe;
                    }
                }
                return exp.transformExpression(this);
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }
        };
    }

}
