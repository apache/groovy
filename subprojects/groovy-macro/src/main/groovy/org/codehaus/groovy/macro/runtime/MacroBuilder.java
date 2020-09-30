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
package org.codehaus.groovy.macro.runtime;

import groovy.lang.Closure;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.codehaus.groovy.macro.methods.MacroGroovyMethods.DOLLAR_VALUE;

/**
 * Runtime support for <pre>{@code macro {} }</pre> method.
 *
 * @since 2.5.0
 */

@Incubating
public enum MacroBuilder {
    INSTANCE;

    public <T> T macro(String source, final List<Closure<Expression>> context, Class<T> resultClass) {
        return macro(false, source, context, resultClass);
    }

    public <T> T macro(boolean asIs, String source, final List<Closure<Expression>> context, Class<T> resultClass) {
        return macro(null, asIs, source, context, resultClass);
    }

    public <T> T macro(CompilePhase compilePhase, String source, final List<Closure<Expression>> context, Class<T> resultClass) {
        return macro(compilePhase, false, source, context, resultClass);
    }

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @SuppressWarnings("unchecked")
    public <T> T macro(CompilePhase compilePhase, boolean asIs, String source, final List<Closure<Expression>> context, Class<T> resultClass) {
        boolean isClosure = source.startsWith("{");
        final String label = isClosure ? "__synthesized__label__" + COUNTER.incrementAndGet() + "__:" : "";
        final String labelledSource = label + source;

        if (compilePhase == null) {
            compilePhase = CompilePhase.CONVERSION;
        }

        List<ASTNode> nodes = (new AstBuilder()).buildFromString(compilePhase, true, labelledSource);

        for (ASTNode node : nodes) {
            if (node instanceof BlockStatement) {

                List<Statement> statements = ((BlockStatement) node).getStatements();
                if (!statements.isEmpty()) {
                    BlockStatement closureBlock = (BlockStatement) statements.get(0);

                    performSubstitutions(context, closureBlock);

                    return (T) getMacroValue(closureBlock, asIs);
                }
            }
            if (node instanceof ClassNode) {
                performSubstitutions(context, node);
                return (T) node;
            }
        }
        return null;
    }

    private static void performSubstitutions(final List<Closure<Expression>> context, final ASTNode astNode) {
        final Iterator<Closure<Expression>> iterator = context.iterator();
        ClassCodeExpressionTransformer trn = new ClassCodeExpressionTransformer() {
            @Override
            public Expression transform(Expression expression) {
                if (!(expression instanceof MethodCallExpression)) {
                    return super.transform(expression);
                }

                MethodCallExpression call = (MethodCallExpression) expression;

                if (!DOLLAR_VALUE.equals(call.getMethodAsString())) {
                    return super.transform(expression);
                }

                return iterator.next().call();
            }

            @Override
            protected SourceUnit getSourceUnit() {
                // Could be null if there are no errors
                return null;
            }
        };
        if (astNode instanceof BlockStatement) {
            trn.visitBlockStatement((BlockStatement) astNode);
        } else if (astNode instanceof ClassNode) {
            trn.visitClass((ClassNode) astNode);
        }
    }

    public static ASTNode getMacroValue(BlockStatement closureBlock, boolean asIs) {
        if(!asIs && closureBlock.getStatements().size() == 1) {
            Statement result = closureBlock.getStatements().get(0);
            if(result instanceof ExpressionStatement) {
                return ((ExpressionStatement) result).getExpression();
            } else {
                return result;
            }
        }
        return closureBlock;
    }
}
