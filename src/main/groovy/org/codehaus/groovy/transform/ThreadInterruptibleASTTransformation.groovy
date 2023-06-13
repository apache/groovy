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

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.isGenerated
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX

/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ThreadInterrupt
 * @since 1.8.0
 */
@AutoFinal @CompileStatic @GroovyASTTransformation
class ThreadInterruptibleASTTransformation extends AbstractInterruptibleASTTransformation {

    private static final ClassNode TYPE = ClassHelper.make(ThreadInterrupt)
    private static final ClassNode THREAD_TYPE = ClassHelper.makeCached(Thread)
    private static final MethodNode CURRENTTHREAD_METHOD = THREAD_TYPE.getMethod('currentThread')
    private static final MethodNode ISINTERRUPTED_METHOD = THREAD_TYPE.getMethod('isInterrupted')

    @Override
    protected ClassNode type() {
        TYPE
    }

    @Override
    protected String getErrorMessage() {
        'Execution interrupted. The current thread has been interrupted.'
    }

    @Override
    protected Expression createCondition() {
        def currentThread = callX(classX(THREAD_TYPE), 'currentThread')
        currentThread.methodTarget = CURRENTTHREAD_METHOD
        currentThread.implicitThis = false

        def isInterrupted = callX(currentThread, 'isInterrupted')
        isInterrupted.methodTarget = ISINTERRUPTED_METHOD
        isInterrupted.implicitThis = false

        isInterrupted
    }

    @Override
    void visitClosureExpression(ClosureExpression closure) {
        Statement code = closure.code
        closure.code = wrapBlock(code)
        super.visitClosureExpression(closure)
    }

    @Override
    void visitMethod(MethodNode method) {
        if (checkOnMethodStart && !method.isAbstract() && !method.isSynthetic()
                && !(method.getDeclaringClass().isRecord() && isGenerated(method))) {
            Statement code = method.code
            method.code = wrapBlock(code)
        }
        super.visitMethod(method)
    }
}
