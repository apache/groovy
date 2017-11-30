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
package org.codehaus.groovy.macro;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.macro.runtime.MacroContext;

import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;

public class ExampleMacroMethods {

    @Macro
    public static Expression safe(MacroContext macroContext, MethodCallExpression callExpression) {
        return ternaryX(
                notNullX(callExpression.getObjectExpression()),
                callExpression,
                constX(null)
        );
    }

    @Macro
    public static ConstantExpression methodName(MacroContext macroContext, MethodCallExpression callExpression) {
        return constX(callExpression.getMethodAsString());
    }

    @Macro
    public static ConstantExpression methodName(MacroContext macroContext, MethodPointerExpression methodPointerExpression) {
        return constX(methodPointerExpression.getMethodName().getText());
    }

    @Macro
    public static ConstantExpression propertyName(MacroContext macroContext, PropertyExpression propertyExpression) {
        return constX(propertyExpression.getPropertyAsString());
    }
}
