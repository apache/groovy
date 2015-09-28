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
import org.codehaus.groovy.macro.transform.MacroInvocationTrap;
import org.codehaus.groovy.macro.transform.MacroTransformation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public enum MacroBuilder {
    INSTANCE;

    public <T> T macro(String source, final Map<MacroSubstitutionKey, Closure<Expression>> context, Class<T> resultClass) {
        return macro(false, source, context, resultClass);
    }

    public <T> T macro(boolean asIs, String source, final Map<MacroSubstitutionKey, Closure<Expression>> context, Class<T> resultClass) {
        return macro(CompilePhase.CONVERSION, asIs, source, context, resultClass);
    }

    public <T> T macro(CompilePhase compilePhase, String source, final Map<MacroSubstitutionKey, Closure<Expression>> context, Class<T> resultClass) {
        return macro(compilePhase, false, source, context, resultClass);
    }

    private final static AtomicInteger COUNTER = new AtomicInteger();

    @SuppressWarnings("unchecked")
    public <T> T macro(CompilePhase compilePhase, boolean asIs, String source, final Map<MacroSubstitutionKey, Closure<Expression>> context, Class<T> resultClass) {
        boolean isClosure = source.startsWith("{");
        final String label = isClosure ?"__synthesized__label__" + COUNTER.incrementAndGet() + "__:":"";
        final String labelledSource = label + source;
        final int linesOffset = 1;
        final int columnsOffset = label.length() + (isClosure?1:0); // +1 because of {

        List<ASTNode> nodes = (new AstBuilder()).buildFromString(compilePhase, true, labelledSource);

        for(ASTNode node : nodes) {
            if (node instanceof BlockStatement) {

                List<Statement> statements = ((BlockStatement) node).getStatements();
                if (!statements.isEmpty()) {
                    BlockStatement closureBlock = (BlockStatement) statements.get(0);

                    performSubstitutions(linesOffset, columnsOffset, context, closureBlock);


                    return (T) getMacroValue(closureBlock, asIs);
                }
            }
            if (node instanceof ClassNode) {
                performSubstitutions(linesOffset, columnsOffset, context, node);
                return (T) node;
            }
        }
        return null;
    }

    private void performSubstitutions(final int linesOffset, final int columnsOffset, final Map<MacroSubstitutionKey, Closure<Expression>> context, final ASTNode astNode) {
        ClassCodeExpressionTransformer trn = new ClassCodeExpressionTransformer() {
            public Expression transform(Expression expression) {
                if (!(expression instanceof MethodCallExpression)) {
                    return super.transform(expression);
                }

                MethodCallExpression call = (MethodCallExpression) expression;

                if (!MacroInvocationTrap.isBuildInvocation(call, MacroTransformation.DOLLAR_VALUE)) {
                    return super.transform(expression);
                }

                MacroSubstitutionKey key = new MacroSubstitutionKey(call, linesOffset, columnsOffset);

                return context.get(key).call();
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
