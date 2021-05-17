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

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveByte;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveChar;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveFloat;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveInt;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveShort;

public class PrimitiveHelper {
    private PrimitiveHelper() {
    }

    public static Expression getDefaultValueForPrimitive(ClassNode type) {
        if (isPrimitiveInt(type)) {
            return new ConstantExpression(0);
        }
        if (isPrimitiveLong(type)) {
            return new ConstantExpression(0L);
        }
        if (isPrimitiveDouble(type)) {
            return new ConstantExpression(0.0);
        }
        if (isPrimitiveFloat(type)) {
            return new ConstantExpression(0.0F);
        }
        if (isPrimitiveBoolean(type)) {
            return ConstantExpression.FALSE;
        }
        if (isPrimitiveShort(type)) {
            return new ConstantExpression((short) 0);
        }
        if (isPrimitiveByte(type)) {
            return new ConstantExpression((byte) 0);
        }
        if (isPrimitiveChar(type)) {
            return new ConstantExpression((char) 0);
        }
        return null;
    }
}
