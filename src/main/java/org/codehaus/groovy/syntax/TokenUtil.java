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

import static org.codehaus.groovy.syntax.Types.BITWISE_AND;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR_EQUAL;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
import static org.codehaus.groovy.syntax.Types.INTDIV;
import static org.codehaus.groovy.syntax.Types.INTDIV_EQUAL;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND;
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND_EQUAL;
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR;
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR_EQUAL;
import static org.codehaus.groovy.syntax.Types.MINUS;
import static org.codehaus.groovy.syntax.Types.MINUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.POWER;
import static org.codehaus.groovy.syntax.Types.POWER_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED_EQUAL;

/**
 * Utility methods for working with Tokens.
 *
 */
public class TokenUtil {
    private TokenUtil() {
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
            case PLUS_EQUAL: return PLUS;
            case MINUS_EQUAL: return MINUS;
            case MULTIPLY_EQUAL: return MULTIPLY;
            case LEFT_SHIFT_EQUAL: return LEFT_SHIFT;
            case RIGHT_SHIFT_EQUAL: return RIGHT_SHIFT;
            case RIGHT_SHIFT_UNSIGNED_EQUAL: return RIGHT_SHIFT_UNSIGNED;
            case LOGICAL_OR_EQUAL: return LOGICAL_OR;
            case LOGICAL_AND_EQUAL: return LOGICAL_AND;
            case MOD_EQUAL: return MOD;
            case DIVIDE_EQUAL: return DIVIDE;
            case INTDIV_EQUAL: return INTDIV;
            case POWER_EQUAL: return POWER;
            case BITWISE_OR_EQUAL: return BITWISE_OR;
            case BITWISE_AND_EQUAL: return BITWISE_AND;
            case BITWISE_XOR_EQUAL: return BITWISE_XOR;
            default: return op;
        }
    }
}
