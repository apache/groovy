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
import org.codehaus.groovy.ast.ClassHelper;
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
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ClassNodeUtils.hasNoArgConstructor;
import static org.apache.groovy.ast.tools.VisibilityUtils.getVisibility;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.copyStatementsWithSuperAdjustment;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Handles generation of code for the @MapConstructor annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MapConstructorASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    private CompilationUnit compilationUnit;

    static final Class<?> MY_CLASS = MapConstructor.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode MAP_TYPE = ClassHelper.MAP_TYPE.getPlainNodeReference();
    private static final ClassNode LHMAP_TYPE = ClassHelper.makeWithoutCaching(LinkedHashMap.class, false);

    @Override
    public String getAnnotationName() {
        return MY_TYPE_NAME;
    }

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            boolean includeFields = memberHasValue(anno, "includeFields", Boolean.TRUE);
            boolean includeProperties = !memberHasValue(anno, "includeProperties", Boolean.FALSE);
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", Boolean.TRUE);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", Boolean.TRUE);
            boolean includeStatic = memberHasValue(anno, "includeStatic", Boolean.TRUE);
            boolean allProperties = memberHasValue(anno, "allProperties", Boolean.TRUE);
            boolean noArg = memberHasValue(anno, "noArg", Boolean.TRUE);
            boolean specialNamedArgHandling = !memberHasValue(anno, "specialNamedArgHandling", Boolean.FALSE);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            boolean allNames = memberHasValue(anno, "allNames", Boolean.TRUE);
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

    private static void createConstructors(final AbstractASTTransformation xform, final AnnotationNode anno, final PropertyHandler handler, final ClassNode cNode,
                                           final boolean includeFields, final boolean includeProperties, final boolean includeSuperProperties, final boolean includeSuperFields,
                                           final boolean noArg, final boolean allNames, final boolean allProperties, final boolean specialNamedArgHandling, final boolean includeStatic,
                                           final List<String> excludes, final List<String> includes, final ClosureExpression pre, final ClosureExpression post, final SourceUnit source) {

        // HACK: JavaStubGenerator could have snuck in a constructor we don't want
        cNode.getDeclaredConstructors().removeIf(next -> next.getFirstStatement() == null);

        boolean includePseudoGetters = false, includePseudoSetters = allProperties, skipReadOnly = false; // GROOVY-4363
        Set<String> names = new HashSet<>();
        List<PropertyNode> properties;
        if (includeSuperProperties || includeSuperFields) {
            properties = getAllProperties(names, cNode, cNode.getSuperClass(), includeSuperProperties, includeSuperFields, includePseudoGetters, includePseudoSetters, /*super*/true, skipReadOnly, /*reverse*/false, allNames, includeStatic);
        } else {
            properties = new ArrayList<>();
        }
        properties.addAll(getAllProperties(names, cNode, cNode, includeProperties, includeFields, includePseudoGetters, includePseudoSetters, /*super*/false, skipReadOnly, /*reverse*/false, allNames, includeStatic));

        BlockStatement body = new BlockStatement();
        ClassCodeExpressionTransformer transformer = makeMapTypedArgsTransformer(source);
        if (pre != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(pre);
            copyStatementsWithSuperAdjustment(transformed, body);
        }
        if (!handler.validateProperties(xform, body, cNode, properties)) {
            return;
        }

        BlockStatement inner = new BlockStatement();
        Parameter map = new Parameter(MAP_TYPE, "args");
        boolean specialNamedArgsCase = specialNamedArgHandling
                && ImmutableASTTransformation.isSpecialNamedArgCase(properties, true);
        createInitializers(xform, anno, cNode, handler, allNames, excludes, includes, properties, map, inner);
        if (specialNamedArgsCase) map = new Parameter(LHMAP_TYPE, "args");
        body.addStatement(inner);
        if (post != null) {
            ClosureExpression transformed = (ClosureExpression) transformer.transform(post);
            body.addStatement(transformed.getCode());
        }

        int modifiers = getVisibility(anno, cNode, ConstructorNode.class, ACC_PUBLIC);
        createConstructors(cNode, modifiers, map, body, noArg && !properties.isEmpty()/* && !specialNamedArgsCase*/);
    }

    private static void createConstructors(final ClassNode cNode, final int mods, final Parameter args, final Statement body, final boolean noArg) {
        Parameter[] parameters = {args};
        if (cNode.getDeclaredConstructor(parameters) == null) {
            ConstructorNode ctor = addGeneratedConstructor(cNode, mods, parameters, ClassNode.EMPTY_ARRAY, body);
            // GROOVY-5814: fix compatibility with @CompileStatic
            ClassCodeVisitorSupport variableExpressionFix = new ClassCodeVisitorSupport() {
                @Override
                public void visitVariableExpression(final VariableExpression expression) {
                    super.visitVariableExpression(expression);
                    if ("args".equals(expression.getName())) {
                        expression.setAccessedVariable(args);
                    }
                }

                @Override
                protected SourceUnit getSourceUnit() {
                    return cNode.getModule().getContext();
                }
            };
            variableExpressionFix.visitConstructor(ctor);
        }
        if (noArg && !hasNoArgConstructor(cNode)) {
            addGeneratedConstructor(cNode, mods, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, stmt(ctorThisX(args(new MapExpression()))));
        }
    }

    private static void createInitializers(final AbstractASTTransformation xform, final AnnotationNode aNode, final ClassNode cNode, final PropertyHandler handler, final boolean allNames, final List<String> excludes, final List<String> includes, final List<PropertyNode> list, final Parameter map, final BlockStatement block) {
        for (PropertyNode pNode : list) {
            String name = pNode.getName();
            if (shouldSkipUndefinedAware(name, excludes, includes, allNames)) continue;
            Statement propInit = handler.createPropInit(xform, aNode, cNode, pNode, map);
            if (propInit != null) {
                block.addStatement(propInit);
            }
        }
    }

    private static ClassCodeExpressionTransformer makeMapTypedArgsTransformer(final SourceUnit unit) {
        return new ClassCodeExpressionTransformer() {
            @Override
            public Expression transform(final Expression exp) {
                if (exp instanceof ClosureExpression) {
                    ClosureExpression ce = (ClosureExpression) exp;
                    ce.getCode().visit(this);
                } else if (exp instanceof VariableExpression) {
                    VariableExpression ve = (VariableExpression) exp;
                    if ("args".equals(ve.getName()) && ve.getAccessedVariable() instanceof DynamicVariable) {
                        VariableExpression newVe = varX(new Parameter(MAP_TYPE, "args"));
                        newVe.setSourcePosition(ve);
                        return newVe;
                    }
                }
                return exp.transformExpression(this);
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return unit;
            }
        };
    }
}
