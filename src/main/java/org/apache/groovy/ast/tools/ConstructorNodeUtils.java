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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.ImmutableASTTransformation;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Utility class for working with ConstructorNodes
 */
public class ConstructorNodeUtils {
    private static final ClassNode IMMUTABLE_TYPE = make(ImmutableASTTransformation.class);

    private ConstructorNodeUtils() { }

    /**
     * Return the first statement from the constructor code if it is a call to super or this, otherwise null.
     *
     * @param code
     * @return the first statement if a special call or null
     */
    public static ConstructorCallExpression getFirstIfSpecialConstructorCall(Statement code) {
        if (code == null) return null;

        if (code instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement) code;
            final List<Statement> statementList = block.getStatements();
            if (statementList.isEmpty()) return null;
            // handle blocks of blocks
            return getFirstIfSpecialConstructorCall(statementList.get(0));
        }

        if (!(code instanceof ExpressionStatement)) return null;

        Expression expression = ((ExpressionStatement) code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        if (cce.isSpecialCall()) return cce;
        return null;
    }

    public static StaticMethodCallExpression checkPropNamesExpr(VariableExpression namedArgs) {
        return callX(IMMUTABLE_TYPE, "checkPropNames", args(varX("this"), namedArgs));
    }
}
