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
package groovy.text.markup;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.List;

/**
 * This transformer is responsible for adding calls to the <i>newLine</i> method
 * depending on the layout of the source code, inside builder like blocks.
 */
class AutoNewLineTransformer extends ClassCodeVisitorSupport {
    private final SourceUnit unit;
    private boolean inBuilderMethod;

    public AutoNewLineTransformer(final SourceUnit unit) {
        this.unit = unit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        boolean old = inBuilderMethod;
        inBuilderMethod = false;
        if (call.isImplicitThis() && call.getArguments() instanceof TupleExpression) {
            List<Expression> expressions = ((TupleExpression) call.getArguments()).getExpressions();
            if (!expressions.isEmpty()) {
                Expression lastArg = expressions.get(expressions.size() - 1);
                if (lastArg instanceof ClosureExpression) {
                    call.getObjectExpression().visit(this);
                    call.getMethod().visit(this);
                    for (Expression expression : expressions) {
                        inBuilderMethod =  (expression == lastArg);
                        expression.visit(this);
                    }
                }
            }
        } else {
            super.visitMethodCallExpression(call);
        }
        inBuilderMethod = old;
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        super.visitClosureExpression(expression);
        if (inBuilderMethod) {
            Statement oldCode = expression.getCode();
            BlockStatement block = oldCode instanceof BlockStatement?
                    ((BlockStatement)oldCode):
                    new BlockStatement(Collections.singletonList(oldCode), new VariableScope());
            List<Statement> statements = block.getStatements();
            if (!statements.isEmpty()) {
                Statement first = statements.get(0);
                Statement last = statements.get(statements.size()-1);
                if (expression.getLineNumber()<first.getLineNumber()) {
                    // there's a new line between { -> ... and the first statement
                    statements.add(0,createNewLine(expression));
                }
                if (expression.getLastLineNumber()>last.getLastLineNumber()) {
                    // there's a new line between { -> ... and the first statement
                    statements.add(createNewLine(expression));
                }
            }
            expression.setCode(block);
        }
    }

    private Statement createNewLine(final ASTNode node) {
        MethodCallExpression mce = new MethodCallExpression(
                new VariableExpression("this"),
                "newLine",
                ArgumentListExpression.EMPTY_ARGUMENTS
        );
        mce.setImplicitThis(true);
        mce.setSourcePosition(node);
        ExpressionStatement stmt = new ExpressionStatement(mce);
        stmt.setSourcePosition(node);
        return stmt;
    }
}
