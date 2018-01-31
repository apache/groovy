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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.ImmutableASTTransformation.createConstructorMapCommon;
import static org.codehaus.groovy.transform.ImmutableASTTransformation.createConstructorStatement;
import static org.codehaus.groovy.transform.ImmutableASTTransformation.createConstructorStatementMapSpecial;

/**
 * Handles generation of code for the @MapConstructor annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MapConstructorASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = MapConstructor.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false);
    private static final ClassNode IMMUTABLE_XFORM_TYPE = make(ImmutableASTTransformation.class);

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
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", true);
            boolean useSetters = memberHasValue(anno, "useSetters", true);
            boolean allProperties = memberHasValue(anno, "allProperties", true);
            boolean noArg = memberHasValue(anno, "noArg", true);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            boolean allNames = memberHasValue(anno, "allNames", true);
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

            createConstructors(this, cNode, includeFields, includeProperties, includeSuperProperties, includeSuperFields, useSetters, noArg, allNames, allProperties, excludes, includes, (ClosureExpression) pre, (ClosureExpression) post, source);

            if (pre != null) {
                anno.setMember("pre", new ClosureExpression(new Parameter[0], EmptyStatement.INSTANCE));
            }
            if (post != null) {
                anno.setMember("post", new ClosureExpression(new Parameter[0], EmptyStatement.INSTANCE));
            }
        }
    }

    private static void createConstructors(AbstractASTTransformation xform, ClassNode cNode, boolean includeFields, boolean includeProperties, boolean includeSuperProperties, boolean includeSuperFields, boolean useSetters, boolean noArg, boolean allNames, boolean allProperties, List<String> excludes, List<String> includes, ClosureExpression pre, ClosureExpression post, SourceUnit source) {
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        boolean foundEmpty = constructors.size() == 1 && constructors.get(0).getFirstStatement() == null;
        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        if (foundEmpty) constructors.remove(0);

        Set<String> names = new HashSet<String>();
        List<PropertyNode> superList;
        if (includeSuperProperties || includeSuperFields) {
            superList = getAllProperties(names, cNode, cNode.getSuperClass(), includeSuperProperties, includeSuperFields, allProperties, true, true);
        } else {
            superList = new ArrayList<PropertyNode>();
        }
        List<PropertyNode> list = getAllProperties(names, cNode, true, includeFields, allProperties, false, true);

        boolean makeImmutable = ImmutableASTTransformation.makeImmutable(cNode);

        Parameter map = param(MAP_TYPE, "args");
        final BlockStatement body = new BlockStatement();
        ClassCodeExpressionTransformer transformer = makeMapTypedArgsTransformer();
        if (pre != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(pre);
            copyStatementsWithSuperAdjustment(transformed, body);
        }
        final BlockStatement inner = new BlockStatement();
        superList.addAll(list);
        boolean specialHashMapCase = ImmutableASTTransformation.isSpecialHashMapCase(superList);
        if (!specialHashMapCase) {
            processProps(xform, cNode, makeImmutable && !specialHashMapCase, useSetters, allNames, excludes, includes, superList, map, inner);
            body.addStatement(ifS(equalsNullX(varX("args")), assignS(varX("args"), new MapExpression())));
        }
        body.addStatement(inner);
        if (post != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(post);
            body.addStatement(transformed.getCode());
        }
        if (makeImmutable && !specialHashMapCase) {
            body.addStatement(stmt(callX(IMMUTABLE_XFORM_TYPE, "checkPropNames", args("this", "args"))));
            createConstructorMapCommon(cNode, body);
        } else if (specialHashMapCase) {
            inner.addStatement(createConstructorStatementMapSpecial(superList.get(0).getField()));
            createConstructorMapCommon(cNode, body);
        } else {
            cNode.addConstructor(new ConstructorNode(ACC_PUBLIC, params(map), ClassNode.EMPTY_ARRAY, body));
        }
        if (noArg && !superList.isEmpty() && !hasNoArgConstructor(cNode) && !specialHashMapCase) {
            createNoArgConstructor(cNode);
        }
    }

    private static void processProps(AbstractASTTransformation xform, ClassNode cNode, boolean makeImmutable, boolean useSetters, boolean allNames, List<String> excludes, List<String> includes, List<PropertyNode> superList, Parameter map, BlockStatement inner) {
        for (PropertyNode pNode : superList) {
            String name = pNode.getName();
            if (shouldSkipUndefinedAware(name, excludes, includes, allNames)) continue;
            if (makeImmutable) {
                inner.addStatement(createConstructorStatement(xform, cNode, pNode, true));
            } else {
                assignField(useSetters, map, inner, name);
            }
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
