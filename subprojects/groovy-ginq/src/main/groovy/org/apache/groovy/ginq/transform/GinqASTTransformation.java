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
import org.apache.groovy.ginq.GinqGroovyMethods;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.groovy.ginq.GinqGroovyMethods.transformGinqCode;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;

/**
 * Handles generation of code for the {@code @GQ} annotation.
 * @since 4.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GinqASTTransformation extends AbstractASTTransformation {
    private static final ClassNode GQ_CLASS_NODE = make(GQ.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ModuleNode moduleNode = sourceUnit.getAST();
        List<ClassNode> classNodeList = moduleNode.getClasses();
        if (classNodeList == null) return;

        classNodeList.stream().flatMap(c -> c.getMethods().stream())
                .filter(m -> !m.getAnnotations(GQ_CLASS_NODE).isEmpty())
                .map(m -> {
                    if (m.isAbstract()) {
                        addError("Error during " + GQ_CLASS_NODE.getName() + " processing: annotation not allowed on abstract method '" + m.getName() + "'", m);
                        return m.getDeclaringClass();
                    }
                    BlockStatement origCode = (BlockStatement) m.getCode();
                    MapExpression ginqConfigurationMapExpression = makeGinqConfigurationMapExpression(m);
                    BlockStatement newCode = block(
                            returnS(transformGinqCode(sourceUnit, ginqConfigurationMapExpression, origCode))
                    );
                    newCode.setSourcePosition(origCode);
                    m.setCode(newCode);
                    return m.getDeclaringClass();
                }).distinct()
                .forEach(c -> {
                    VariableScopeVisitor variableScopeVisitor = new VariableScopeVisitor(sourceUnit);
                    variableScopeVisitor.visitClass(c);
                });
    }

    private MapExpression makeGinqConfigurationMapExpression(MethodNode m) {
        Map<String, Expression> resultMembers = new HashMap<>();
        Map<String, Expression> defaultMembers = GinqGroovyMethods.CONF_LIST.stream().collect(Collectors.toMap(
                c -> c,
                c -> {
                    try {
                        return constX(GQ_CLASS_NODE.getTypeClass().getMethod(c).getDefaultValue());
                    } catch (NoSuchMethodException e) {
                        throw new GroovyBugError("Unknown GINQ option: " + c, e);
                    }
                }
        ));
        resultMembers.putAll(defaultMembers);

        AnnotationNode gqAnnotationNode = m.getAnnotations(GQ_CLASS_NODE).get(0);
        Map<String, Expression> members = gqAnnotationNode.getMembers();
        resultMembers.putAll(members);

        MapExpression ginqConfigurationMapExpression =
                        mapX(resultMembers.entrySet().stream()
                                .map(e -> new MapEntryExpression(constX(e.getKey()), constX(e.getValue().getText())))
                                .collect(Collectors.toList()));

        return ginqConfigurationMapExpression;
    }
}
