/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;


import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.expr.*

/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ThreadInterrupt
 *
 * @author Cedric Champeau
 * @author Hamlet D'Arcy
 *
 * @since 1.8.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ThreadInterruptibleASTTransformation extends AbstractInterruptibleASTTransformation {

    private static final ClassNode MY_TYPE = ClassHelper.make(ThreadInterrupt)

    protected ClassNode type() {
        return MY_TYPE;
    }

    protected String getErrorMessage() {
        'Execution interrupted. The current thread has been interrupted.'
    }

    protected Expression createCondition() {
        new MethodCallExpression(
                new StaticMethodCallExpression(ClassHelper.make(Thread),
                        'currentThread',
                        ArgumentListExpression.EMPTY_ARGUMENTS),
                'isInterrupted', ArgumentListExpression.EMPTY_ARGUMENTS)
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
