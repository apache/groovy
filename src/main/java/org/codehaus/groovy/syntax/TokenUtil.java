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
 * Utility methods for working with Tokens.
 */
public class TokenUtil {

    private TokenUtil() {
    }

    public static Optional<Token> asAssignment(int op) {
        switch (op) {
            case Types.PLUS_PLUS:
            case Types.PREFIX_PLUS_PLUS:
            case Types.POSTFIX_PLUS_PLUS:
                return Optional.of(Token.newSymbol(Types.PLUS_EQUAL, -1, -1));
            case Types.MINUS_MINUS:
            case Types.PREFIX_MINUS_MINUS:
            case Types.POSTFIX_MINUS_MINUS:
                return Optional.of(Token.newSymbol(Types.MINUS_EQUAL, -1, -1));
        }
        return Optional.empty();
    }

    /**
     * Removes the assignment portion of a given token.  If the given token
     * is not an operator with assignment, the given token is returned.
     *
     * @param op token for which to remove assignment
     * @return token without assignment, or the original token
     *          if it was not an assignment operator
     */
    public static int removeAssignment(int op) {
        switch (op) {
            case Types.PLUS_EQUAL: return Types.PLUS;
            case Types.MINUS_EQUAL: return Types.MINUS;
            case Types.MULTIPLY_EQUAL: return Types.MULTIPLY;
            case Types.LEFT_SHIFT_EQUAL: return Types.LEFT_SHIFT;
            case Types.RIGHT_SHIFT_EQUAL: return Types.RIGHT_SHIFT;
            case Types.RIGHT_SHIFT_UNSIGNED_EQUAL: return Types.RIGHT_SHIFT_UNSIGNED;
            case Types.LOGICAL_OR_EQUAL: return Types.LOGICAL_OR;
            case Types.LOGICAL_AND_EQUAL: return Types.LOGICAL_AND;
            case Types.MOD_EQUAL: return Types.MOD;
            case Types.DIVIDE_EQUAL: return Types.DIVIDE;
            case Types.INTDIV_EQUAL: return Types.INTDIV;
            case Types.POWER_EQUAL: return Types.POWER;
            case Types.BITWISE_OR_EQUAL: return Types.BITWISE_OR;
            case Types.BITWISE_AND_EQUAL: return Types.BITWISE_AND;
            case Types.BITWISE_XOR_EQUAL: return Types.BITWISE_XOR;
            default: return op;
        }
    }
}
