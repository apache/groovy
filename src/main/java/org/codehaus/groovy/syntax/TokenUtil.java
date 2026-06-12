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
package org.codehaus.groovy.syntax;

import java.util.Optional;

/**
 * Utility methods for working with tokens and operators.
 * Provides helper functions for operator type manipulation and conversion.
 */
public class TokenUtil {

    private TokenUtil() {
    }

    /**
     * Converts an increment or decrement operator to its corresponding assignment operator.
     * For example, {@code ++} becomes {@code +=}, and {@code --} becomes {@code -=}.
     *
     * @param op the operator type (e.g., {@code Types.PLUS_PLUS}, {@code Types.PREFIX_PLUS_PLUS})
     * @return an {@link Optional} containing the assignment operator token, or empty if not applicable
     */
    public static Optional<Token> asAssignment(int op) {
        return switch (op) {
            case Types.PLUS_PLUS, Types.PREFIX_PLUS_PLUS, Types.POSTFIX_PLUS_PLUS ->
                Optional.of(Token.newSymbol(Types.PLUS_EQUAL, -1, -1));
            case Types.MINUS_MINUS, Types.PREFIX_MINUS_MINUS, Types.POSTFIX_MINUS_MINUS ->
                Optional.of(Token.newSymbol(Types.MINUS_EQUAL, -1, -1));
            default -> Optional.empty();
        };
    }

    /**
     * Removes the assignment portion of an assignment operator, returning the base operator.
     * For example, {@code +=} becomes {@code +}, {@code -=} becomes {@code -}.
     * If the operator is not an assignment operator, returns it unchanged.
     *
     * @param op the operator type from {@link Types}
     * @return the base operator type without assignment, or the original type if not an assignment operator
     */
    public static int removeAssignment(int op) {
        return switch (op) {
            case Types.PLUS_EQUAL -> Types.PLUS;
            case Types.MINUS_EQUAL -> Types.MINUS;
            case Types.MULTIPLY_EQUAL -> Types.MULTIPLY;
            case Types.LEFT_SHIFT_EQUAL -> Types.LEFT_SHIFT;
            case Types.RIGHT_SHIFT_EQUAL -> Types.RIGHT_SHIFT;
            case Types.RIGHT_SHIFT_UNSIGNED_EQUAL -> Types.RIGHT_SHIFT_UNSIGNED;
            case Types.LOGICAL_OR_EQUAL -> Types.LOGICAL_OR;
            case Types.LOGICAL_AND_EQUAL -> Types.LOGICAL_AND;
            case Types.MOD_EQUAL -> Types.MOD;
            case Types.DIVIDE_EQUAL -> Types.DIVIDE;
            case Types.INTDIV_EQUAL -> Types.INTDIV;
            case Types.POWER_EQUAL -> Types.POWER;
            case Types.BITWISE_OR_EQUAL -> Types.BITWISE_OR;
            case Types.BITWISE_AND_EQUAL -> Types.BITWISE_AND;
            case Types.BITWISE_XOR_EQUAL -> Types.BITWISE_XOR;
            case Types.REMAINDER_EQUAL -> Types.REMAINDER;
            default -> op;
        };
    }
}
