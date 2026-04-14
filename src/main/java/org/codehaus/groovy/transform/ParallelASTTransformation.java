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
package org.codehaus.groovy.transform;

import groovy.transform.Parallel;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * Local AST transform for {@link Parallel}: rewrites annotated
 * {@code for-in} loops into {@code collection.eachParallel { item -> body }}.
 * <p>
 * The transform renames references to the loop variable in the body to a
 * synthetic name (e.g. {@code $parallel_item}) to avoid scope conflicts
 * with the for loop's own variable declaration, then re-resolves variable
 * scopes to ensure the closure captures enclosing variables correctly.
 */
@Incubating
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ParallelASTTransformation implements ASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes == null || nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        if (!Parallel.class.getName().equals(annotation.getClassNode().getName())) return;

        if (!(nodes[1] instanceof ForStatement forStatement)) {
            source.getErrorCollector().addError(new SyntaxErrorMessage(
                    new SyntaxException("@Parallel may only be applied to a for loop statement",
                            annotation.getLineNumber(), annotation.getColumnNumber()), source));
            return;
        }

        if (forStatement.getValueVariable() == null) {
            source.getErrorCollector().addError(new SyntaxErrorMessage(
                    new SyntaxException("@Parallel currently supports only for-in loops",
                            annotation.getLineNumber(), annotation.getColumnNumber()), source));
            return;
        }

        rewriteForLoop(forStatement, annotation, source);
        // Re-resolve variable scopes after AST rewrite
        for (ClassNode classNode : source.getAST().getClasses()) {
            new VariableScopeVisitor(source).visitClass(classNode);
        }
    }

    private static void rewriteForLoop(final ForStatement forLoop, final AnnotationNode annotation, final SourceUnit source) {
        Statement originalBody = forLoop.getLoopBlock();
        Parameter loopParameter = forLoop.getValueVariable();
        Expression originalCollection = forLoop.getCollectionExpression();

        // Rename references to the loop variable in the body to a synthetic
        // name, avoiding a clash with the for loop's own parameter declaration.
        String originalName = loopParameter.getName();
        String internalName = "$parallel_" + originalName;

        // Rename all VariableExpressions referencing the loop variable
        new VariableRenamer(source, originalName, internalName).rename(originalBody);

        // Build closure: { $parallel_item -> originalBody }
        Parameter closureParam = param(loopParameter.getOriginType(), internalName);
        BlockStatement closureBody = block();
        if (originalBody instanceof BlockStatement bodyBlock) {
            bodyBlock.getStatements().forEach(closureBody::addStatement);
        } else {
            closureBody.addStatement(originalBody);
        }
        ClosureExpression closure = closureX(params(closureParam), closureBody);
        closure.setSourcePosition(annotation);

        // originalCollection.eachParallel(closure)
        Expression eachParallelCall = callX(originalCollection, "eachParallel", args(closure));

        Statement callStatement = stmt(eachParallelCall);
        callStatement.setSourcePosition(annotation);

        // Rewrite the for loop to iterate over [null] once,
        // executing the eachParallel call
        ListExpression singleRun = new ListExpression();
        singleRun.addExpression(ConstantExpression.NULL);
        forLoop.setCollectionExpression(singleRun);
        forLoop.setLoopBlock(block(callStatement));
    }

    /**
     * Renames {@link VariableExpression}s matching a given name within a
     * statement, but skips renames inside nested closures that declare a
     * parameter shadowing the loop variable.
     */
    private static class VariableRenamer extends ClassCodeExpressionTransformer {
        private final SourceUnit source;
        private final String from;
        private final String to;
        private int shadowDepth;

        VariableRenamer(SourceUnit source, String from, String to) {
            this.source = source;
            this.from = from;
            this.to = to;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }

        @Override
        public Expression transform(Expression expression) {
            if (expression instanceof ClosureExpression closure) {
                boolean shadows = shadowsLoopVar(closure);
                if (shadows) shadowDepth++;
                try {
                    return super.transform(expression);
                } finally {
                    if (shadows) shadowDepth--;
                }
            }
            if (shadowDepth == 0
                    && expression instanceof VariableExpression ve
                    && from.equals(ve.getName())) {
                return new VariableExpression(to, ve.getOriginType());
            }
            return super.transform(expression);
        }

        private boolean shadowsLoopVar(ClosureExpression closure) {
            Parameter[] params = closure.getParameters();
            if (params == null) {
                // null parameter list means implicit 'it'
                return "it".equals(from);
            }
            for (Parameter p : params) {
                if (from.equals(p.getName())) return true;
            }
            return false;
        }

        void rename(Statement statement) {
            statement.visit(this);
        }
    }
}
