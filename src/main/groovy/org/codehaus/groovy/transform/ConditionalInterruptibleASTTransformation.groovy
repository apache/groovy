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
package org.codehaus.groovy.transform

import groovy.transform.CompileStatic
import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.tools.ClosureUtils
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase

/**
 * Allows "interrupt-safe" executions of scripts by adding a custom conditional
 * check on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ConditionalInterrupt
 * @since 1.8.0
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ConditionalInterruptibleASTTransformation extends AbstractInterruptibleASTTransformation {

    private ClosureExpression conditionNode
    private String conditionMethod
    private ClassNode currentClass

    protected ClassNode type() {
        ClassHelper.make(ConditionalInterrupt)
    }

    protected void setupTransform(AnnotationNode node) {
        super.setupTransform(node)
        def member = node.getMember('value')
        if (member !instanceof ClosureExpression)
            internalError("Expected closure value for annotation parameter 'value'. Found $member")
        conditionNode = (ClosureExpression) member
        conditionMethod = 'conditionalTransform' + node.hashCode() + '$condition'
    }

    protected String getErrorMessage() {
        'Execution interrupted. The following condition failed: ' + convertClosureToSource(conditionNode)
    }

    void visitClass(ClassNode type) {
        currentClass = type
        type.addSyntheticMethod(conditionMethod, ACC_PRIVATE, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, conditionNode.code)
        if (applyToAllMembers) {
            super.visitClass(type)
        }
    }

    protected Expression createCondition() {
        GeneralUtils.callThisX(conditionMethod)
    }

    @Override
    void visitAnnotations(AnnotatedNode node) {
        // this transformation does not apply on annotation nodes
        // visiting could lead to stack overflows
    }

    @Override
    void visitField(FieldNode node) {
        if (!node.isStatic() && !node.isSynthetic()) {
            super.visitField node
        }
    }

    @Override
    void visitProperty(PropertyNode node) {
        if (!node.isStatic() && !node.isSynthetic()) {
            super.visitProperty node
        }
    }

    @Override
    void visitClosureExpression(ClosureExpression closureExpr) {
        if (closureExpr == conditionNode) return // do not visit the closure from the annotation itself
        def code = closureExpr.code
        closureExpr.code = wrapBlock(code)
        super.visitClosureExpression closureExpr
    }

    @Override
    void visitMethod(MethodNode node) {
        if (node.name == conditionMethod && !node.isSynthetic()) return // do not visit the generated method
        if (node.name == 'run' && currentClass.isScript() && node.parameters.length == 0) {
            // the run() method should not have the statement added, otherwise the script binding won't be set before
            // the condition is actually tested
            super.visitMethod(node)
        } else {
            if (checkOnMethodStart && !node.isSynthetic() && !node.isStatic() && !node.isAbstract()) {
                def code = node.code
                node.code = wrapBlock(code)
            }
            if (!node.isSynthetic() && !node.isStatic()) super.visitMethod(node)
        }
    }

    /**
     * Converts a ClosureExpression into the String source.
     * @param expression a closure
     * @return the source the closure was created from
     */
    private String convertClosureToSource(ClosureExpression expression) {
        try {
            return ClosureUtils.convertClosureToSource(this.source.source, expression)
        } catch (Exception e) {
            return e.message
        }
    }
}
