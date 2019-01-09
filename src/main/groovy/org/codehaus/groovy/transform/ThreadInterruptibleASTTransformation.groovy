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
import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilePhase

/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ThreadInterrupt
 * @since 1.8.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
public class ThreadInterruptibleASTTransformation extends AbstractInterruptibleASTTransformation {

    private static final ClassNode MY_TYPE = ClassHelper.make(ThreadInterrupt)
    private static final ClassNode THREAD_TYPE = ClassHelper.make(Thread)
    private static final MethodNode CURRENTTHREAD_METHOD
    private static final MethodNode ISINTERRUPTED_METHOD

    static {
        CURRENTTHREAD_METHOD = THREAD_TYPE.getMethod('currentThread', Parameter.EMPTY_ARRAY)
        ISINTERRUPTED_METHOD = THREAD_TYPE.getMethod('isInterrupted', Parameter.EMPTY_ARRAY)
    }

    protected ClassNode type() {
        return MY_TYPE;
    }

    protected String getErrorMessage() {
        'Execution interrupted. The current thread has been interrupted.'
    }

    protected Expression createCondition() {
        def currentThread = new MethodCallExpression(new ClassExpression(THREAD_TYPE),
                'currentThread',
                ArgumentListExpression.EMPTY_ARGUMENTS)
        currentThread.methodTarget = CURRENTTHREAD_METHOD
        def isInterrupted = new MethodCallExpression(
                currentThread,
                'isInterrupted', ArgumentListExpression.EMPTY_ARGUMENTS)
        isInterrupted.methodTarget = ISINTERRUPTED_METHOD
        [currentThread, isInterrupted]*.implicitThis = false

        isInterrupted
    }


    @Override
    public void visitClosureExpression(ClosureExpression closureExpr) {
        def code = closureExpr.code
        closureExpr.code = wrapBlock(code)
        super.visitClosureExpression closureExpr
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (checkOnMethodStart && !node.isSynthetic() && !node.isAbstract()) {
            def code = node.code
            node.code = wrapBlock(code);
        }
        super.visitMethod(node)
    }
}
