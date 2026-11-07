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
package org.apache.groovy.ginq.transform;

import groovy.ginq.transform.GQ;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import groovy.transform.CompileStatic;
import groovy.transform.TypeCheckingMode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;
import static org.apache.groovy.ginq.GinqGroovyMethods.CONF_LIST;
import static org.apache.groovy.ginq.GinqGroovyMethods.transformGinqCode;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.isDynamicTyped;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * Handles generation of code for the {@code @GQ} annotation.
 * @since 4.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GinqASTTransformation extends AbstractASTTransformation {
    /**
     * Rewrites annotated methods by replacing their bodies with transformed GINQ queries.
     *
     * @param nodes the transformation nodes
     * @param sourceUnit the source unit being transformed
     */
    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit);
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        if (!GQ_CLASS_NODE.equals(annotationNode.getClassNode())) return;

        if (annotatedNode instanceof MethodNode methodNode) {
            if (methodNode.isAbstract()) {
                addError("Error during " + GQ_CLASS_NODE.getName() + " processing: annotation not allowed on abstract method '" + methodNode.getName() + "'", methodNode);
                return;
            }
            BlockStatement origCode = (BlockStatement) methodNode.getCode();
            MapExpression ginqConfigurationMapExpression = makeGinqConfigurationMapExpression(annotationNode);
            Expression valueExpression = annotationNode.getMember(VALUE);

            ClassNode targetType = DEFAULT_RESULT_TYPE;
            if (valueExpression instanceof ClassExpression) {
                ClassNode type = valueExpression.getType();
                if (!targetType.equals(type)) {
                    targetType = type;
                }
            }

            Expression resultExpression = transformGinqCode(sourceUnit, ginqConfigurationMapExpression, origCode);
            if (DEFAULT_RESULT_TYPE.equals(targetType)) { // same as `GQ {...}`
                // DO NOTHING
            } else if (LIST_TYPE.equals(targetType)) { // same as `GQL {...}`
                resultExpression = callX(resultExpression, "toList");
            } else { // same as `GQ {...} as TargetType`
                CastExpression castExpression = castX(targetType, resultExpression);
                castExpression.setCoerce(true);
                resultExpression = castExpression;
            }

            BlockStatement newCode = block(stmt(resultExpression));
            newCode.setSourcePosition(origCode);
            methodNode.setCode(newCode);
            // The lowered query uses dynamic property access, operators, and aggregate calls.
            // When the enclosing scope is statically checked, skip that checking for this
            // method so the query keeps its runtime behaviour and callers still see its type.
            if (staticallyChecked(methodNode)) {
                methodNode.addAnnotation(skipStaticCompilation());
            }
            widenDynamicReturn(methodNode, targetType);
            VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit);
            variableScopeVisitor.visitClass(methodNode.getDeclaringClass());
        }
    }

    private static boolean staticallyChecked(MethodNode methodNode) {
        Boolean mode = explicitMode(methodNode);
        if (mode != null) return mode;
        for (ClassNode current = methodNode.getDeclaringClass(); current != null; current = current.getOuterClass()) {
            mode = explicitMode(current);
            if (mode != null) return mode;
        }
        return false;
    }

    /**
     * @return {@code TRUE} for static checking, {@code FALSE} for an explicit skip, or {@code null} when unspecified
     */
    private static Boolean explicitMode(AnnotatedNode node) {
        Boolean mode = null;
        for (AnnotationNode annotation : node.getAnnotations()) {
            String raw = annotation.getClassNode().getName();
            int dot = raw.lastIndexOf('.');
            String simple = dot < 0 ? raw : raw.substring(dot + 1);
            if ("CompileDynamic".equals(simple)) {
                return Boolean.FALSE;
            }
            if (!"CompileStatic".equals(simple) && !"TypeChecked".equals(simple)) {
                continue;
            }
            Expression value = annotation.getMember("value");
            if (value instanceof org.codehaus.groovy.ast.expr.PropertyExpression property && "SKIP".equals(property.getPropertyAsString())) {
                return Boolean.FALSE;
            }
            mode = Boolean.TRUE;
        }
        return mode;
    }

    private static AnnotationNode skipStaticCompilation() {
        AnnotationNode annotation = new AnnotationNode(make(CompileStatic.class));
        annotation.addMember("value", propX(classX(make(TypeCheckingMode.class)), "SKIP"));
        return annotation;
    }

    /**
     * A {@code def} query method is widened to the declared GINQ result type so callers
     * compiled statically can use the result. An explicit return type is left alone.
     * A superclass method with a more specific return type is also left alone.
     */
    private static void widenDynamicReturn(MethodNode methodNode, ClassNode targetType) {
        if (!isDynamicTyped(methodNode.getReturnType()) || targetType == null) {
            return;
        }
        ClassNode widened = targetType.getPlainNodeReference();
        widened.setGenericsTypes(null);
        if (incompatibleSuperReturn(methodNode, widened)) {
            return;
        }
        methodNode.setReturnType(widened);
    }

    private static boolean incompatibleSuperReturn(MethodNode methodNode, ClassNode widened) {
        String name = methodNode.getName();
        int arity = methodNode.getParameters().length;
        for (ClassNode current = methodNode.getDeclaringClass().getSuperClass(); current != null; current = current.getSuperClass()) {
            for (MethodNode candidate : current.getDeclaredMethods(name)) {
                if (candidate.getParameters().length != arity) continue;
                ClassNode returned = candidate.getReturnType();
                if (returned == null || isDynamicTyped(returned) || ClassHelper.OBJECT_TYPE.equals(returned)) {
                    continue;
                }
                if (!widened.equals(returned) && !widened.isDerivedFrom(returned) && !widened.implementsInterface(returned)) {
                    return true;
                }
            }
        }
        return false;
    }

    private MapExpression makeGinqConfigurationMapExpression(AnnotationNode annotationNode) {
        Map<String, Expression> resultMembers = new HashMap<>(DEFAULT_OPTION_MAP);
        Map<String, Expression> currentMembers = new HashMap<>(annotationNode.getMembers());
        resultMembers.putAll(currentMembers);
        resultMembers.remove(VALUE);

        return mapX(resultMembers.entrySet().stream()
                    .map(e -> new MapEntryExpression(constX(e.getKey()), constX(e.getValue().getText())))
                    .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private static Object getDefaultOptionValue(String optionName) {
        try {
            return GQ_CLASS_NODE.getTypeClass().getMethod(optionName).getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new GroovyBugError("Unknown GINQ option: " + optionName, e);
        }
    }

    private static boolean hasOptionMember(String optionName) {
        try {
            GQ_CLASS_NODE.getTypeClass().getMethod(optionName);
            return true;
        } catch (NoSuchMethodException e) {
            // some options, e.g. `dataSource`, hold runtime values and have no annotation counterpart
            return false;
        }
    }

    private static final String VALUE = "value";
    private static final ClassNode GQ_CLASS_NODE = make(GQ.class);
    private static final ClassNode DEFAULT_RESULT_TYPE = ClassHelper.makeWithoutCaching((Class<?>) getDefaultOptionValue(VALUE));
    private static final Map<String, Expression> DEFAULT_OPTION_MAP = unmodifiableMap(CONF_LIST.stream()
            .filter(GinqASTTransformation::hasOptionMember)
            .collect(Collectors.toMap(
                c -> c,
                c -> constX(getDefaultOptionValue(c))
    )));
}
