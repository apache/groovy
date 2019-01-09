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
package org.codehaus.groovy.transform.tailrec

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement

/**
 * Wrap the body of a method in a while loop, nested in a try-catch.
 * This is the first step in making a tail recursive method iterative.
 *
 * There are two ways to invoke the next iteration step:
 * <ol>
 * <li>"continue _RECURE_HERE_" is used by recursive calls outside of closures</li>
 * <li>"throw LOOP_EXCEPTION" is used by recursive calls within closures b/c you cannot invoke "continue" from there</li>
 * </ol>
 */
@CompileStatic
class InWhileLoopWrapper {
	
	static final String LOOP_LABEL = '_RECUR_HERE_'
    static final GotoRecurHereException  LOOP_EXCEPTION = new GotoRecurHereException()

	void wrap(MethodNode method) {
		BlockStatement oldBody = method.code as BlockStatement
        TryCatchStatement tryCatchStatement = new TryCatchStatement(
                oldBody,
                new EmptyStatement()
        )
        tryCatchStatement.addCatch(new CatchStatement(
                new Parameter(ClassHelper.make(GotoRecurHereException), 'ignore'),
                new ContinueStatement(InWhileLoopWrapper.LOOP_LABEL)
        ))

        WhileStatement whileLoop = new WhileStatement(
                new BooleanExpression(new ConstantExpression(true)),
                new BlockStatement([tryCatchStatement] as List<Statement>, new VariableScope(method.variableScope))
        )
        List<Statement> whileLoopStatements = ((BlockStatement) whileLoop.loopBlock).statements
        if (whileLoopStatements.size() > 0)
            whileLoopStatements[0].statementLabel = LOOP_LABEL
		BlockStatement newBody = new BlockStatement([] as List<Statement>, new VariableScope(method.variableScope))
		newBody.addStatement(whileLoop)
		method.code = newBody
	}
}

/**
 * Exception will be thrown by recursive calls in closures and caught in while loop to continue to LOOP_LABEL
 */
class GotoRecurHereException extends Throwable {

}
