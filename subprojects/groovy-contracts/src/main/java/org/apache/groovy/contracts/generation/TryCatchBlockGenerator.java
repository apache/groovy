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
package org.apache.groovy.contracts.generation;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.PLUS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Creates a try-catch block around a given {@link org.codehaus.groovy.ast.stmt.AssertStatement} and catches
 * a PowerAssertionError to reuse the generated visual output.
 */
public class TryCatchBlockGenerator {

    public static BlockStatement generateTryCatchBlockForInlineMode(final ClassNode assertionErrorClass, final String message, final Statement assertStatement) {

        final Class powerAssertionErrorClass = loadPowerAssertionErrorClass();

        if (powerAssertionErrorClass == null)
            throw new GroovyBugError("groovy-contracts needs Groovy 1.7 or above!");

        VariableExpression newErrorVariableExpression = localVarX("newError", assertionErrorClass);

        Statement expr = declS(newErrorVariableExpression,
                ctorX(assertionErrorClass,
                        args(binX(constX(message), PLUS, callX(varX(param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error")), "getMessage")))));

        Statement exp2 = stmt(callX(newErrorVariableExpression, "setStackTrace", args(
                callX(varX(param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error")), "getStackTrace")
        )));

        final TryCatchStatement tryCatchStatement = tryCatchS(assertStatement);
        tryCatchStatement.addCatch(catchS(
                param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error"),
                block(expr, exp2, throwS(newErrorVariableExpression))));

        return block(tryCatchStatement);
    }

    public static BlockStatement generateTryCatchBlock(final ClassNode assertionErrorClass, final String message, final Statement assertStatement) {

        final String $_gc_closure_result = "$_gc_closure_result";

        final VariableExpression variableExpression = localVarX($_gc_closure_result, ClassHelper.Boolean_TYPE);

        // if the assert statement is successful the return variable will be true else false
        final BlockStatement overallBlock = new BlockStatement();
        overallBlock.addStatement(declS(variableExpression, ConstantExpression.FALSE));

        final BlockStatement assertBlockStatement = block(
                assertStatement,
                assignS(variableExpression, ConstantExpression.TRUE)
        );

        final Class powerAssertionErrorClass = loadPowerAssertionErrorClass();

        if (powerAssertionErrorClass == null)
            throw new GroovyBugError("groovy-contracts needs Groovy 1.7 or above!");

        VariableExpression newErrorVariableExpression = localVarX("newError", assertionErrorClass);

        Statement expr = declS(newErrorVariableExpression, ctorX(assertionErrorClass,
                args(binX(constX(message), PLUS, callX(varX(param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error")), "getMessage")))));

        Statement exp2 = stmt(callX(newErrorVariableExpression, "setStackTrace", args(
                callX(varX(param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error")), "getStackTrace")
        )));

        final TryCatchStatement tryCatchStatement = tryCatchS(assertBlockStatement);
        tryCatchStatement.addCatch(catchS(param(ClassHelper.makeWithoutCaching(powerAssertionErrorClass), "error"), block(expr, exp2)));

        overallBlock.addStatement(tryCatchStatement);
        overallBlock.addStatement(returnS(variableExpression));

        return overallBlock;
    }

    private static Class loadPowerAssertionErrorClass() {

        Class result = null;

        try {
            result = TryCatchBlockGenerator.class.getClassLoader().loadClass("org.codehaus.groovy.transform.powerassert.PowerAssertionError");
        } catch (ClassNotFoundException e) {
            try {
                result = TryCatchBlockGenerator.class.getClassLoader().loadClass("org.codehaus.groovy.runtime.powerassert.PowerAssertionError");
            } catch (ClassNotFoundException ignore) {
            }
        }

        return result;
    }
}
