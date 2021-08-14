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
package org.codehaus.groovy.transform.tailrec;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.List;

/**
 * Wrap the body of a method in a while loop, nested in a try-catch.
 * This is the first step in making a tail recursive method iterative.
 * <p>
 * There are two ways to invoke the next iteration step:
 * <ol>
 * <li>"continue _RECUR_HERE_" is used by recursive calls outside of closures</li>
 * <li>"throw LOOP_EXCEPTION" is used by recursive calls within closures b/c you cannot invoke "continue" from there</li>
 * </ol>
 */
class InWhileLoopWrapper {
    public void wrap(MethodNode method) {
        BlockStatement oldBody = DefaultGroovyMethods.asType(method.getCode(), BlockStatement.class);
        TryCatchStatement tryCatchStatement = GeneralUtils.tryCatchS(oldBody, EmptyStatement.INSTANCE, GeneralUtils.catchS(GeneralUtils.param(ClassHelper.make(GotoRecurHereException.class), "ignore"), new ContinueStatement(InWhileLoopWrapper.LOOP_LABEL)));

        WhileStatement whileLoop = new WhileStatement(GeneralUtils.boolX(GeneralUtils.constX(true)), GeneralUtils.block(new VariableScope(method.getVariableScope()), tryCatchStatement));
        List<Statement> whileLoopStatements = ((BlockStatement) whileLoop.getLoopBlock()).getStatements();
        if (whileLoopStatements.size() > 0) whileLoopStatements.get(0).setStatementLabel(LOOP_LABEL);
        BlockStatement newBody = GeneralUtils.block(new VariableScope(method.getVariableScope()));
        newBody.addStatement(whileLoop);
        method.setCode(newBody);
    }

    public static final String LOOP_LABEL = "_RECUR_HERE_";
    public static final GotoRecurHereException LOOP_EXCEPTION = new GotoRecurHereException();
}
