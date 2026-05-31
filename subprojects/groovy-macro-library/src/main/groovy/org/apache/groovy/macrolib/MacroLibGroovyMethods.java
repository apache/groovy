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
import org.apache.groovy.runtime.Comprehensions;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.macro.runtime.MacroContext;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * Macro library helpers for string and named-value expansion.
 *
 * @since 4.0.0
 */
public final class MacroLibGroovyMethods {
    private MacroLibGroovyMethods() {}

    private static final ClassNode NAMED_VALUE = ClassHelper.make(NamedValue.class);
    private static final ClassNode COMPREHENSIONS = ClassHelper.make(Comprehensions.class);

    /**
     * Builds a GString expression that labels each supplied expression with its source text.
     *
     * @param ctx the current macro context
     * @param exps the expressions to interpolate
     * @return the labeled GString expression
     */
    @Macro
    public static Expression SV(MacroContext ctx, final Expression... exps) {
        return new GStringExpression("", makeLabels(exps), Arrays.asList(exps));
    }

    /**
     * Runtime stub for {@link #SV(MacroContext, Expression...)}.
     *
     * @param self the receiver
     * @param args the interpolated values
     * @return never returns normally
     */
    public static GString SV(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SV(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    /**
     * Builds a GString expression that labels each supplied expression with its inspected value.
     *
     * @param ctx the current macro context
     * @param exps the expressions to inspect
     * @return the labeled GString expression
     */
    @Macro
    public static Expression SVI(MacroContext ctx, final Expression... exps) {
        List<Expression> expList = Arrays.stream(exps).map(exp -> callX(exp, "inspect"))
                .collect(Collectors.toList());
        return new GStringExpression("", makeLabels(exps), expList);
    }

    /**
     * Runtime stub for {@link #SVI(MacroContext, Expression...)}.
     *
     * @param self the receiver
     * @param args the interpolated values
     * @return never returns normally
     */
    public static GString SVI(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SVI(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    /**
     * Builds a GString expression that labels each supplied expression with its dumped value.
     *
     * @param ctx the current macro context
     * @param exps the expressions to dump
     * @return the labeled GString expression
     */
    @Macro
    public static Expression SVD(MacroContext ctx, final Expression... exps) {
        List<Expression> expList = Arrays.stream(exps).map(exp -> callX(exp, "dump"))
                .collect(Collectors.toList());
        return new GStringExpression("", makeLabels(exps), expList);
    }

    /**
     * Runtime stub for {@link #SVD(MacroContext, Expression...)}.
     *
     * @param self the receiver
     * @param args the interpolated values
     * @return never returns normally
     */
    public static GString SVD(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.SVD(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    private static List<ConstantExpression> makeLabels(Expression[] exps) {
        return IntStream
                .range(0, exps.length)
                .mapToObj(i -> constX((i > 0 ? ", " : "") + exps[i].getText() + "="))
                .collect(Collectors.toList());
    }

    /**
     * Builds a {@link NamedValue} expression from the supplied expression.
     *
     * @param ctx the current macro context
     * @param exp the expression to wrap
     * @return the named-value expression
     */
    @Macro
    public static Expression NV(MacroContext ctx, final Expression exp) {
        return namedValueExpr(exp);
    }

    /**
     * Runtime stub for {@link #NV(MacroContext, Expression)}.
     *
     * @param self the receiver
     * @param arg the runtime value
     * @param <T> the value type
     * @return never returns normally
     */
    public static <T> NamedValue<T> NV(Object self, T arg) {
        throw new IllegalStateException("MacroLibGroovyMethods.NV(Object) should never be called at runtime. Are you sure you are using it correctly?");
    }

    private static Expression namedValueExpr(Expression exp) {
        return ctorX(NAMED_VALUE, args(constX(exp.getText()), exp));
    }

    /**
     * Builds a list of {@link NamedValue} expressions from the supplied expressions.
     *
     * @param ctx the current macro context
     * @param exps the expressions to wrap
     * @return the list expression
     */
    @Macro
    public static Expression NVL(MacroContext ctx, final Expression... exps) {
        return listX(Arrays.stream(exps).map(exp -> namedValueExpr(exp)).collect(Collectors.toList()));
    }

    /**
     * Runtime stub for {@link #NVL(MacroContext, Expression...)}.
     *
     * @param self the receiver
     * @param args the runtime values
     * @param <T> the value type
     * @return never returns normally
     */
    @SuppressWarnings("unchecked")
    public static <T> List<NamedValue<T>> NVL(Object self, T... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.NVL(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    /**
     * Monadic comprehension macro ({@code DO}). Rewrites a comma-separated list of
     * {@code name in expression} generators followed by a body closure into a nested
     * chain of {@link Comprehensions#bind} calls &mdash; the do-notation desugaring:
     * <pre>
     *   DO(x in m1, y in f(x)) { body }
     *   ==&gt;
     *   Comprehensions.bind(m1) { x -&gt; Comprehensions.bind(f(x)) { y -&gt; body } }
     * </pre>
     * Every generator becomes a bind; the body is the innermost closure body and
     * must itself yield a carrier value (the do-notation rule &mdash; no implicit
     * lifting). Carrier-specific bind dispatch is deferred to runtime
     * ({@link Comprehensions}) because macros expand before type checking.
     *
     * @param ctx the current macro context
     * @param exps the generators (each {@code name in expression}) followed by the body closure
     * @return the nested bind-chain expression
     * @since 6.0.0
     */
    @Macro
    public static Expression DO(MacroContext ctx, final Expression... exps) {
        if (exps == null || exps.length < 2) {
            return error(ctx, ctx.getCall(),
                "DO requires at least one 'name in expression' generator and a trailing closure body");
        }
        Expression last = exps[exps.length - 1];
        if (!(last instanceof ClosureExpression body)) {
            return error(ctx, last, "DO requires a trailing closure body, e.g. DO(x in m1) { ... }");
        }
        if (body.getParameters() != null && body.getParameters().length > 0) {
            return error(ctx, body,
                "DO body closure must not declare parameters; generator names are already in scope");
        }

        int genCount = exps.length - 1;
        List<String> names = new ArrayList<String>(genCount);
        List<Expression> sources = new ArrayList<Expression>(genCount);
        for (int i = 0; i < genCount; i++) {
            Expression g = exps[i];
            if (!(g instanceof BinaryExpression bin)
                    || ((BinaryExpression) g).getOperation().getType() != Types.KEYWORD_IN) {
                return error(ctx, g, "DO generator must have the form 'name in expression'");
            }
            if (!(bin.getLeftExpression() instanceof VariableExpression)) {
                return error(ctx, bin.getLeftExpression(),
                    "DO generator binding must be a simple name, e.g. x in m1");
            }
            names.add(((VariableExpression) bin.getLeftExpression()).getName());
            sources.add(bin.getRightExpression());
        }

        // Build innermost-outward: the last generator's closure carries the body.
        // Copy source positions from the originating user nodes onto every
        // synthetic AST node we create — fresh AST nodes default to line/col -1,
        // and several downstream code paths (notably
        // {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#addStaticTypeError})
        // silently drop diagnostics on positionless nodes. Anchoring everything
        // back to the user's {@code name in expr} clause keeps both error
        // attribution and IDE navigation pointing at real source.
        Expression chain = null;
        for (int i = genCount - 1; i >= 0; i--) {
            Expression g = exps[i];
            Expression nameExp = ((BinaryExpression) g).getLeftExpression();
            Statement closureBody = (i == genCount - 1) ? body.getCode() : block(stmt(chain));
            Parameter p = param(ClassHelper.dynamicType(), names.get(i));
            p.setSourcePosition(nameExp);
            ClosureExpression lambda = closureX(new Parameter[]{p}, closureBody);
            lambda.setVariableScope(new VariableScope());
            // innermost lambda mirrors the user's body closure; outer lambdas the generator
            lambda.setSourcePosition(i == genCount - 1 ? body : g);
            Expression receiver = classX(COMPREHENSIONS);
            receiver.setSourcePosition(g);
            Expression argList = args(sources.get(i), lambda);
            argList.setSourcePosition(g);
            chain = callX(receiver, "bind", argList);
            chain.setSourcePosition(g);
        }
        return chain;
    }

    /**
     * Runtime stub for {@link #DO(MacroContext, Expression...)}.
     *
     * @param self the receiver
     * @param args the runtime values
     * @return never returns normally
     * @since 6.0.0
     */
    public static Object DO(Object self, Object... args) {
        throw new IllegalStateException("MacroLibGroovyMethods.DO(Object...) should never be called at runtime. Are you sure you are using it correctly?");
    }

    private static Expression error(MacroContext ctx, Expression node, String message) {
        ctx.getSourceUnit().addError(new SyntaxException(message + '\n', node));
        // Return a non-macro expression: the error fails compilation, and returning
        // the original DO(...) call would have it re-expanded ad infinitum.
        return constX(null);
    }

}
