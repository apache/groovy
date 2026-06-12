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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;

/**
 * Resolves constants in annotation definitions.
 */
public class AnnotationConstantsVisitor extends ClassCodeVisitorSupport {

    private boolean annotationDef;
    private SourceUnit sourceUnit;

    /**
     * Returns the source unit currently being visited.
     *
     * @return the active source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    /**
     * Visits an annotation definition and inlines constant values in its members.
     *
     * @param classNode the annotation class to inspect
     * @param sourceUnit the owning source unit
     */
    public void visitClass(final ClassNode classNode, final SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
        this.annotationDef = classNode.isAnnotationDefinition();
        super.visitClass(classNode);
        this.annotationDef = false;
    }

    /**
     * Rewrites annotation member bodies so constant expressions are resolved eagerly.
     *
     * @param node the constructor or method to inspect
     * @param isConstructor whether {@code node} is a constructor
     */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        if (annotationDef) {
            Statement statement = node.getFirstStatement();
            if (statement instanceof ReturnStatement rs) {
                rs.setExpression(transformInlineConstants(rs.getExpression(), node.getReturnType()));
            } else if (statement instanceof ExpressionStatement es) {
                es.setExpression(transformInlineConstants(es.getExpression(), node.getReturnType()));
            }
        }
    }
}
