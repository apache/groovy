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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;

public class PrimitiveHelper {
    private PrimitiveHelper() {
    }

    public static Expression getDefaultValueForPrimitive(ClassNode type) {
        if (type == ClassHelper.int_TYPE) {
            return new ConstantExpression(0);
        }
        if (type == ClassHelper.long_TYPE) {
            return new ConstantExpression(0L);
        }
        if (type == ClassHelper.double_TYPE) {
            return new ConstantExpression(0.0);
        }
        if (type == ClassHelper.float_TYPE) {
            return new ConstantExpression(0.0F);
        }
        if (type == ClassHelper.boolean_TYPE) {
            return ConstantExpression.FALSE;
        }
        if (type == ClassHelper.short_TYPE) {
            return new ConstantExpression((short) 0);
        }
        if (type == ClassHelper.byte_TYPE) {
            return new ConstantExpression((byte) 0);
        }
        if (type == ClassHelper.char_TYPE) {
            return new ConstantExpression((char) 0);
        }
        return null;
    }
}
