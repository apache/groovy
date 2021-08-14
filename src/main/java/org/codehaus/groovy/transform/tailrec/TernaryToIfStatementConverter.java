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

import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asType;

/**
 * Since a ternary statement has more than one exit point tail-recursiveness testing cannot be easily done.
 * Therefore this class translates a ternary statement (or Elvis operator) into the equivalent if-else statement.
 */
class TernaryToIfStatementConverter {
    @SuppressWarnings("Instanceof")
    public Statement convert(ReturnStatement statementWithInnerTernaryExpression) {
        if (!(statementWithInnerTernaryExpression.getExpression() instanceof TernaryExpression))
            return statementWithInnerTernaryExpression;
        TernaryExpression ternary = asType(statementWithInnerTernaryExpression.getExpression(), TernaryExpression.class);
        return ifElseS(ternary.getBooleanExpression(), returnS(ternary.getTrueExpression()), returnS(ternary.getFalseExpression()));
    }
}
