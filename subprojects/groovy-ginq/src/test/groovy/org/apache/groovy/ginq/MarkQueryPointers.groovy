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
package org.apache.groovy.ginq

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * Marks method pointers inside a GINQ query closure so the static writer
 * must keep them as Groovy pointers. Used to cover the
 * {@code DYNAMIC_RESOLUTION} guard in the method-reference writer.
 */
class MarkQueryPointers extends AbstractTypeCheckingExtension {

    MarkQueryPointers(StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor)
    }

    @Override
    void afterVisitMethod(MethodNode node) {
        if (node.code == null) {
            return
        }
        node.code.visit(new CodeVisitorSupport() {
            private int queryDepth

            @Override
            void visitClosureExpression(ClosureExpression expression) {
                boolean query = Boolean.TRUE.equals(expression.getNodeMetaData(GinqRowChecks.QUERY))
                if (query) {
                    queryDepth++
                }
                super.visitClosureExpression(expression)
                if (query) {
                    queryDepth--
                }
            }

            @Override
            void visitMethodPointerExpression(MethodPointerExpression expression) {
                if (queryDepth > 0) {
                    expression.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE)
                }
                super.visitMethodPointerExpression(expression)
            }

            @Override
            void visitMethodReferenceExpression(MethodReferenceExpression expression) {
                visitMethodPointerExpression(expression)
            }
        })
    }
}
