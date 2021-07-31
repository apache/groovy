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
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * Handles generation of code for the {@code @GQ} annotation.
 * @since 4.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GinqASTTransformation extends AbstractASTTransformation {
    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit);
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        if (!GQ_CLASS_NODE.equals(annotationNode.getClassNode())) return;

        if (annotatedNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) annotatedNode;
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
            VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit);
            variableScopeVisitor.visitClass(methodNode.getDeclaringClass());
        }
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

    private static Object getDefaultOptionValue(String optionName) {
        try {
            return GQ_CLASS_NODE.getTypeClass().getMethod(optionName).getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new GroovyBugError("Unknown GINQ option: " + optionName, e);
        }
    }

    private static final String VALUE = "value";
    private static final ClassNode GQ_CLASS_NODE = make(GQ.class);
    private static final ClassNode DEFAULT_RESULT_TYPE = ClassHelper.makeWithoutCaching((Class<?>) getDefaultOptionValue(VALUE));
    private static final Map<String, Expression> DEFAULT_OPTION_MAP = unmodifiableMap(CONF_LIST.stream().collect(Collectors.toMap(
            c -> c,
            c -> constX(getDefaultOptionValue(c))
    )));
}
