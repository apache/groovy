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

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

import java.util.Collections;
import java.util.Map;

/**
 * Handles the implementation of the {@link groovy.transform.TypeChecked} transformation.
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class StaticTypesTransformation implements ASTTransformation, CompilationUnitAware {

    public static final String STATIC_ERROR_PREFIX = "[Static type checking] - ";
    protected CompilationUnit compilationUnit;

    //    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        Map<String,Expression> members = annotationInformation.getMembers();
        Expression extensions = members.get("extensions");
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        StaticTypeCheckingVisitor visitor = null;
        if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            visitor = newVisitor(source, classNode);
            visitor.setCompilationUnit(compilationUnit);
            addTypeCheckingExtensions(visitor, extensions);
            visitor.initialize();
            visitor.visitClass(classNode);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            visitor = newVisitor(source, methodNode.getDeclaringClass());
            visitor.setCompilationUnit(compilationUnit);
            addTypeCheckingExtensions(visitor, extensions);
            visitor.setMethodsToBeVisited(Collections.singleton(methodNode));
            visitor.initialize();
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type",
                    node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()));
        }
        if (visitor != null) {
            visitor.performSecondPass();
        }
    }

    protected void addTypeCheckingExtensions(StaticTypeCheckingVisitor visitor, Expression extensions) {
        if (extensions instanceof ConstantExpression) {
            visitor.addTypeCheckingExtension(new GroovyTypeCheckingExtensionSupport(
                    visitor,
                    extensions.getText(),
                    compilationUnit
            ));
        } else if (extensions instanceof ListExpression) {
            ListExpression list = (ListExpression) extensions;
            for (Expression ext : list.getExpressions()) {
                addTypeCheckingExtensions(visitor, ext);
            }
        }
    }

    /**
     * Allows subclasses to provide their own visitor. This is useful for example for transformations relying
     * on the static type checker.
     *
     *
     * @param unit the source unit
     * @param node the current classnode
     * @return a static type checking visitor
     */
    protected StaticTypeCheckingVisitor newVisitor(SourceUnit unit, ClassNode node) {
        return new StaticTypeCheckingVisitor(unit, node);
    }

    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}
