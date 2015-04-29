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
package org.codehaus.groovy.transform.trait;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;

import java.util.Collection;

import static org.codehaus.groovy.syntax.Types.*;


/**
 * Rewrites some expressions found in traits so that they are compatible. For example, x++ will be rewritten into x = x
 * + 1 and x += y into x = x + y
 *
 * @author Cédric Champeau
 */
class NAryOperationRewriter extends ClassCodeExpressionTransformer {
    private final SourceUnit sourceUnit;
    private final Collection<String> knownFields;

    public NAryOperationRewriter(final SourceUnit sourceUnit, final Collection<String> knownFields) {
        this.sourceUnit = sourceUnit;
        this.knownFields = knownFields;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        if (exp instanceof PrefixExpression) {
            return transformPrefixExpression((PrefixExpression) exp);
        }
        if (exp instanceof PostfixExpression) {
            return transformPostfixExpression((PostfixExpression) exp);
        }
        return super.transform(exp);
    }

    private boolean isInternalFieldAccess(final Expression exp) {
        if (exp instanceof VariableExpression) {
            Variable accessedVariable = ((VariableExpression) exp).getAccessedVariable();
            if (accessedVariable instanceof FieldNode) {
                return knownFields.contains(accessedVariable.getName());
            }
        }
        if (exp instanceof PropertyExpression) {
            if (((PropertyExpression) exp).isImplicitThis() || "this".equals(((PropertyExpression) exp).getObjectExpression().getText())) {
                return knownFields.contains(((PropertyExpression) exp).getProperty().getText());
            }
        }
        return false;
    }

    private Expression transformPrefixExpression(final PrefixExpression exp) {
        if (isInternalFieldAccess(exp.getExpression())) {
            Token operation = exp.getOperation();
            sourceUnit.addError(new SyntaxException("Prefix expressions on trait fields/properties are not supported in traits.", operation.getStartLine(), operation.getStartColumn()));
            return exp;
        } else {
            return super.transform(exp);
        }
    }

    private Expression transformPostfixExpression(final PostfixExpression exp) {
        if (isInternalFieldAccess(exp.getExpression())) {
            Token operation = exp.getOperation();
            sourceUnit.addError(new SyntaxException("Postfix expressions on trait fields/properties  are not supported in traits.", operation.getStartLine(), operation.getStartColumn()));
            return exp;
        } else {
            return super.transform(exp);
        }
    }

    private Expression transformBinaryExpression(final BinaryExpression exp) {
        final int op = exp.getOperation().getType();
        int token = getOperationWithoutEqual(op);
        if (token < 0) {
            // no transform needed
            return super.transform(exp);
        }
        BinaryExpression operation = new BinaryExpression(
                exp.getLeftExpression(),
                Token.newSymbol(token, -1, -1),
                exp.getRightExpression()
        );
        operation.setSourcePosition(exp);
        BinaryExpression result = new BinaryExpression(
                exp.getLeftExpression(),
                Token.newSymbol(EQUAL, -1, -1),
                operation
        );
        result.setSourcePosition(exp);
        return result;
    }

    private int getOperationWithoutEqual(final int op) {
        int token = -1;
        switch (op) {
            case LOGICAL_OR_EQUAL:
                token = LOGICAL_OR;
                break;
            case LOGICAL_AND_EQUAL:
                token = LOGICAL_AND;
                break;
            case PLUS_EQUAL:
                token = PLUS;
                break;
            case MINUS_EQUAL:
                token = MINUS;
                break;
            case MULTIPLY_EQUAL:
                token = MULTIPLY;
                break;
            case DIVIDE_EQUAL:
                token = DIVIDE;
                break;
            case INTDIV_EQUAL:
                token = INTDIV;
                break;
            case MOD_EQUAL:
                token = MOD;
                break;
            case POWER_EQUAL:
                token = POWER;
                break;
            case LEFT_SHIFT_EQUAL:
                token = LEFT_SHIFT;
                break;
            case RIGHT_SHIFT_EQUAL:
                token = RIGHT_SHIFT;
                break;
            case RIGHT_SHIFT_UNSIGNED_EQUAL:
                token = RIGHT_SHIFT_UNSIGNED;
                break;
            case BITWISE_OR_EQUAL:
                token = BITWISE_OR;
                break;
            case BITWISE_AND_EQUAL:
                token = BITWISE_AND;
                break;
            case BITWISE_XOR_EQUAL:
                token = BITWISE_XOR;
                break;
            default:
                token = -1;
        }
        return token;
    }
}
