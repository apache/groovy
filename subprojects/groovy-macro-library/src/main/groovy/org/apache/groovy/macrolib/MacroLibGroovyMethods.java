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
package org.apache.groovy.macrolib;

import groovy.lang.GString;
import groovy.lang.NamedValue;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.macro.runtime.MacroContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;

public final class MacroLibGroovyMethods {
    private MacroLibGroovyMethods() {}

    private static final ClassNode NAMED_VALUE = ClassHelper.make(NamedValue.class);

    @Macro
    public static Expression SV(MacroContext ctx, final Expression... exps) {
        return new GStringExpression("", makeLabels(exps), Arrays.asList(exps));
    }

    public static GString SV(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SV(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression SVI(MacroContext ctx, final Expression... exps) {
        List<Expression> expList = Arrays.stream(exps).map(exp -> callX(exp, "inspect"))
                .collect(Collectors.toList());
        return new GStringExpression("", makeLabels(exps), expList);
    }

    public static GString SVI(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SVI(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression SVD(MacroContext ctx, final Expression... exps) {
        List<Expression> expList = Arrays.stream(exps).map(exp -> callX(exp, "dump"))
                .collect(Collectors.toList());
        return new GStringExpression("", makeLabels(exps), expList);
    }

    public static GString SVD(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SVD(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    private static List<ConstantExpression> makeLabels(Expression[] exps) {
        return IntStream
                .range(0, exps.length)
                .mapToObj(i -> constX((i > 0 ? ", " : "") + exps[i].getText() + "="))
                .collect(Collectors.toList());
    }

    @Macro
    public static Expression NV(MacroContext ctx, final Expression exp) {
        return namedValueExpr(exp);
    }

    public static <T> NamedValue<T> NV(Object self, T arg) {
        throw new IllegalStateException("MacroLibGroovyMethods.NV(Object) should never be called at runtime. Are you sure you are using it correctly?");
    }

    private static Expression namedValueExpr(Expression exp) {
        return ctorX(NAMED_VALUE, args(constX(exp.getText()), exp));
    }

    @Macro
    public static Expression NVL(MacroContext ctx, final Expression... exps) {
        return listX(Arrays.stream(exps).map(exp -> namedValueExpr(exp)).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<NamedValue<T>> NVL(Object self, T... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.NVL(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

}
