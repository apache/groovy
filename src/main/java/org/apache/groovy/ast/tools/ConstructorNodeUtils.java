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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.ImmutableASTTransformation;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.forS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Utility class for working with ConstructorNodes
 */
public class ConstructorNodeUtils {
    private static final ClassNode IMMUTABLE_TYPE = make(ImmutableASTTransformation.class);
    private static final ClassNode EXCEPTION = make(IllegalArgumentException.class);

    private ConstructorNodeUtils() { }

    /**
     * Return the first statement from the constructor code if it is a call to super or this, otherwise null.
     *
     * @param code the code to check
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

    public static Statement checkPropNamesS(VariableExpression namedArgs, boolean pojo, List<PropertyNode> props) {
        if (!pojo) {
            return stmt(callX(IMMUTABLE_TYPE, "checkPropNames", args(varX("this"), namedArgs)));
        }
        BlockStatement block = new BlockStatement();
        ListExpression knownPropNames = new ListExpression();
        for (PropertyNode pNode : props) {
            knownPropNames.addExpression(constX(pNode.getName()));
        }
        VariableExpression validNames = localVarX("validNames", ClassHelper.LIST_TYPE);
        Parameter name = param(ClassHelper.STRING_TYPE, "arg");
        Statement loopS = ifS(notX(callX(validNames, "contains", varX(name))),
                throwS(ctorX(EXCEPTION, plusX(constX("Unknown named argument: "), varX(name)))));
        block.addStatement(declS(validNames, knownPropNames));
        block.addStatement(forS(name, callX(namedArgs, "keySet"), loopS));
        return block;
    }
}
