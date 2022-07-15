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
package org.codehaus.groovy.antlr;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;

import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;

public class PrimitiveHelper {

    private PrimitiveHelper() {
    }

    public static Expression getDefaultValueForPrimitive(final ClassNode type) {
        if (type == int_TYPE) {
            return new ConstantExpression(0, true);
        }
        if (type == long_TYPE) {
            return new ConstantExpression(0L, true);
        }
        if (type == double_TYPE) {
            return new ConstantExpression(0.0, true);
        }
        if (type == boolean_TYPE) {
            return new ConstantExpression(Boolean.FALSE, true);
        }

        if (type == byte_TYPE) {
            return new ConstantExpression((byte) 0, true);
        }
        if (type == char_TYPE) {
            return new ConstantExpression((char) 0, true);
        }
        if (type == float_TYPE) {
            return new ConstantExpression((float) 0, true);
        }
        if (type == short_TYPE) {
            return new ConstantExpression((short) 0, true);
        }

        return null;
    }
}
