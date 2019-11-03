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

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import groovy.transform.MapConstructor;
import groovy.transform.options.PropertyHandler;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.markAsGenerated;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasNoArgConstructor;
import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.copyStatementsWithSuperAdjustment;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @MapConstructor annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MapConstructorASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    private CompilationUnit compilationUnit;

    static final Class MY_CLASS = MapConstructor.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false);
    private static final ClassNode LHMAP_TYPE = makeWithoutCaching(LinkedHashMap.class, false);

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
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
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", true);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", true);
            boolean includeStatic = memberHasValue(anno, "includeStatic", true);
            boolean allProperties = memberHasValue(anno, "allProperties", true);
            boolean noArg = memberHasValue(anno, "noArg", true);
            boolean specialNamedArgHandling = !memberHasValue(anno, "specialNamedArgHandling", false);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            boolean allNames = memberHasValue(anno, "allNames", true);
            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes, "includes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties))
                return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties))
                return;
            final GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
            final PropertyHandler handler = PropertyHandler.createPropertyHandler(this, classLoader, cNode);
            if (handler == null) return;
            if (!handler.validateAttributes(this, anno)) return;

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

            createConstructors(this, anno, handler, cNode, includeFields, includeProperties, includeSuperProperties, includeSuperFields, noArg, allNames, allProperties, specialNamedArgHandling, includeStatic, excludes, includes, (ClosureExpression) pre, (ClosureExpression) post, source);

            if (pre != null) {
                anno.setMember("pre", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
            }
            if (post != null) {
                anno.setMember("post", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
            }
        }
    }

    private static void createConstructors(AbstractASTTransformation xform, AnnotationNode anno, PropertyHandler handler, ClassNode cNode, boolean includeFields, boolean includeProperties,
                                           boolean includeSuperProperties, boolean includeSuperFields, boolean noArg,
                                           boolean allNames, boolean allProperties, boolean specialNamedArgHandling, boolean includeStatic,
                                           List<String> excludes, List<String> includes, ClosureExpression pre, ClosureExpression post, SourceUnit source) {

        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        cNode.getDeclaredConstructors().removeIf(next -> next.getFirstStatement() == null);

        Set<String> names = new HashSet<String>();
        List<PropertyNode> superList;
        if (includeSuperProperties || includeSuperFields) {
            superList = getAllProperties(names, cNode, cNode.getSuperClass(), includeSuperProperties, includeSuperFields, false, allProperties, true, false, false, allNames, includeStatic);
        } else {
            superList = new ArrayList<PropertyNode>();
        }
        List<PropertyNode> list = getAllProperties(names, cNode, cNode, includeProperties, includeFields, false, allProperties, false, false, false, allNames, includeStatic);

        Parameter map = param(MAP_TYPE, "args");
        final BlockStatement body = new BlockStatement();
        ClassCodeExpressionTransformer transformer = makeMapTypedArgsTransformer();
        if (pre != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(pre);
            copyStatementsWithSuperAdjustment(transformed, body);
        }
        final BlockStatement inner = new BlockStatement();
        superList.addAll(list);

        if (!handler.validateProperties(xform, body, cNode, superList)) {
            return;
        }

        boolean specialNamedArgCase = specialNamedArgHandling && ImmutableASTTransformation.isSpecialNamedArgCase(superList, true);
        processProps(xform, anno, cNode, handler, allNames, excludes, includes, superList, map, inner);
        body.addStatement(inner);
        Parameter[] params = params(specialNamedArgCase ? new Parameter(LHMAP_TYPE, "args") : map);
        if (post != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(post);
            body.addStatement(transformed.getCode());
        }
        int modifiers = getVisibility(anno, cNode, ConstructorNode.class, ACC_PUBLIC);
        doAddConstructor(cNode, new ConstructorNode(modifiers, params, ClassNode.EMPTY_ARRAY, body));
        if (noArg && !superList.isEmpty() && !hasNoArgConstructor(cNode)/* && !specialNamedArgCase*/) {
            createNoArgConstructor(cNode, modifiers);
        }
    }

    private static void doAddConstructor(final ClassNode cNode, final ConstructorNode constructorNode) {
        markAsGenerated(cNode, constructorNode);
        cNode.addConstructor(constructorNode);
        // GROOVY-5814: Immutable is not compatible with @CompileStatic
        Parameter argsParam = null;
        for (Parameter p : constructorNode.getParameters()) {
            if ("args".equals(p.getName())) {
                argsParam = p;
                break;
            }
        }
        if (argsParam != null) {
            final Parameter arg = argsParam;
            ClassCodeVisitorSupport variableExpressionFix = new ClassCodeVisitorSupport() {
                @Override
                protected SourceUnit getSourceUnit() {
                    return cNode.getModule().getContext();
                }

                @Override
                public void visitVariableExpression(final VariableExpression expression) {
                    super.visitVariableExpression(expression);
                    if ("args".equals(expression.getName())) {
                        expression.setAccessedVariable(arg);
                    }
                }
            };
            variableExpressionFix.visitConstructor(constructorNode);
        }
    }

    private static void processProps(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyHandler handler, boolean allNames, List<String> excludes, List<String> includes, List<PropertyNode> superList, Parameter map, BlockStatement inner) {
        for (PropertyNode pNode : superList) {
            String name = pNode.getName();
            if (shouldSkipUndefinedAware(name, excludes, includes, allNames)) continue;
            Statement propInit = handler.createPropInit(xform, anno, cNode, pNode, map);
            if (propInit != null) {
                inner.addStatement(propInit);
            }
        }
    }

    private static void createNoArgConstructor(ClassNode cNode, int modifiers) {
        Statement body = stmt(ctorX(ClassNode.THIS, args(new MapExpression())));
        ConstructorNode consNode = new ConstructorNode(modifiers, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
        markAsGenerated(cNode, consNode);
        cNode.addConstructor(consNode);
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
                    if ("args".equals(ve.getName()) && ve.getAccessedVariable() instanceof DynamicVariable) {
                        VariableExpression newVe = varX(param(MAP_TYPE, "args"));
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

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}
