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
package org.codehaus.groovy.macro.methods;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.ClosureUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.macro.runtime.MacroBuilder;
import org.codehaus.groovy.macro.runtime.MacroContext;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.Iterator;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;

public class MacroGroovyMethods {

    public static final String DOLLAR_VALUE = "$v";

    public static class MacroValuePlaceholder {
        public static Object $v(Closure cl) {
            // replaced with AST transformations
            return null;
        }
    }

    public static <T> T macro(Object self, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        throw new IllegalStateException("MacroGroovyMethods.macro(Closure) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression macro(MacroContext macroContext, ClosureExpression closureExpression) {
        return macro(macroContext, new ConstantExpression(false, true), closureExpression);
    }

    public static <T> T macro(Object self, boolean asIs, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        throw new IllegalStateException("MacroGroovyMethods.macro(boolean, Closure) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression macro(MacroContext macroContext, ConstantExpression asIsConstantExpression, ClosureExpression closureExpression) {
        return macro(macroContext, null, asIsConstantExpression, closureExpression);
    }

    public static <T> T macro(Object self, CompilePhase compilePhase, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        throw new IllegalStateException("MacroGroovyMethods.macro(CompilePhase, Closure) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression macro(MacroContext macroContext, PropertyExpression phaseExpression, ClosureExpression closureExpression) {
        return macro(macroContext, phaseExpression, new ConstantExpression(false, true), closureExpression);
    }

    public static <T> T macro(Object self, CompilePhase compilePhase, boolean asIs, @DelegatesTo(MacroValuePlaceholder.class) Closure cl) {
        throw new IllegalStateException("MacroGroovyMethods.macro(CompilePhase, boolean, Closure) should never be called at runtime. Are you sure you are using it correctly?");
    }

    @Macro
    public static Expression macro(MacroContext macroContext, PropertyExpression phaseExpression, ConstantExpression asIsConstantExpression, ClosureExpression closureExpression) {
        if (closureExpression.getParameters() != null && closureExpression.getParameters().length > 0) {
            macroContext.getSourceUnit().addError(new SyntaxException("Macro closure arguments are not allowed" + '\n', closureExpression));
            return macroContext.getCall();
        }

        final String source;
        try {
            source = ClosureUtils.convertClosureToSource(macroContext.getSourceUnit().getSource(), closureExpression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BlockStatement closureBlock = (BlockStatement) closureExpression.getCode();

        Boolean asIs = (Boolean) asIsConstantExpression.getValue();

        return callX(
                propX(classX(ClassHelper.makeWithoutCaching(MacroBuilder.class, false)), "INSTANCE"),
                "macro",
                args(
                        phaseExpression != null ? phaseExpression : constX(null),
                        asIsConstantExpression,
                        constX(source),
                        buildSubstitutions(macroContext.getSourceUnit(), closureExpression),
                        classX(ClassHelper.makeWithoutCaching(MacroBuilder.getMacroValue(closureBlock, asIs).getClass(), false))
                )
        );
    }

    public static ListExpression buildSubstitutions(final SourceUnit source, final ASTNode expr) {
        final ListExpression listExpression = new ListExpression();

        ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            @Override
            public void visitClass(final ClassNode node) {
                super.visitClass(node);
                Iterator<InnerClassNode> it = node.getInnerClasses();
                while (it.hasNext()) {
                    InnerClassNode next = it.next();
                    visitClass(next);
                }
            }

            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call);

                if (DOLLAR_VALUE.equals(call.getMethodAsString())) {
                    ClosureExpression substitutionClosureExpression = getClosureArgument(source, call);

                    if (substitutionClosureExpression == null) {
                        return;
                    }

                    Statement code = substitutionClosureExpression.getCode();
                    if (code instanceof BlockStatement) {
                        ((BlockStatement) code).setVariableScope(null);
                    }

                    listExpression.addExpression(substitutionClosureExpression);
                }
            }
        };
        if (expr instanceof ClassNode) {
            visitor.visitClass((ClassNode) expr);
        } else {
            expr.accept(visitor);
        }
        return listExpression;
    }

    protected static TupleExpression getMacroArguments(SourceUnit source, MethodCallExpression call) {
        Expression macroCallArguments = call.getArguments();
        if (macroCallArguments == null) {
            source.addError(new SyntaxException("Call should have arguments" + '\n', call));
            return null;
        }

        if (!(macroCallArguments instanceof TupleExpression)) {
            source.addError(new SyntaxException("Call should have TupleExpression as arguments" + '\n', macroCallArguments));
            return null;
        }

        TupleExpression tupleArguments = (TupleExpression) macroCallArguments;

        if (tupleArguments.getExpressions() == null) {
            source.addError(new SyntaxException("Call arguments should have expressions" + '\n', tupleArguments));
            return null;
        }

        return tupleArguments;
    }

    protected static ClosureExpression getClosureArgument(SourceUnit source, MethodCallExpression call) {
        TupleExpression tupleArguments = getMacroArguments(source, call);

        int size = tupleArguments == null ? -1 : tupleArguments.getExpressions().size();
        if (size < 1) {
            source.addError(new SyntaxException("Call arguments should have at least one argument" + '\n', tupleArguments));
            return null;
        }

        Expression result = tupleArguments.getExpression(size - 1);
        if (!(result instanceof ClosureExpression)) {
            source.addError(new SyntaxException("Last call argument should be a closure" + '\n', result));
            return null;
        }

        return (ClosureExpression) result;
    }
}
