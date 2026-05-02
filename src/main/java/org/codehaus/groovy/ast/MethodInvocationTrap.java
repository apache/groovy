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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.tools.ClosureUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Abstract base class for AST visitors that trap and intercept specific method invocations.
 * Primarily used to detect AstBuilder "from code" invocations and convert them to the equivalent
 * "from string" approach during compilation.
 *
 * <p>Subclasses implement {@code isBuildInvocation()} to identify target method calls and
 * {@code handleTargetMethodCallExpression()} to transform them. The base class provides utility methods
 * for error reporting and closure-to-source conversion.
 *
 * @see org.codehaus.groovy.control.SourceUnit
 * @see org.codehaus.groovy.control.io.ReaderSource
 * @see MethodCallExpression
 * @see ClosureExpression
 */
public abstract class MethodInvocationTrap extends CodeVisitorSupport {

    /**
     * The source reader for extracting closure source code.
     */
    protected final ReaderSource source;

    /**
     * The source unit for reporting errors and accessing compilation context.
     */
    protected final SourceUnit sourceUnit;

    /**
     * Creates a method invocation trap with the given source and source unit.
     * Both arguments are required; {@code null} values will raise {@link IllegalArgumentException}.
     *
     * @param source the {@link ReaderSource} for extracting source code from closures, cannot be {@code null}
     * @param sourceUnit the {@link SourceUnit} for error reporting, cannot be {@code null}
     * @throws IllegalArgumentException if either parameter is {@code null}
     */
    public MethodInvocationTrap(ReaderSource source, SourceUnit sourceUnit) {
        if (source == null) throw new IllegalArgumentException("Null: source");
        if (sourceUnit == null) throw new IllegalArgumentException("Null: sourceUnit");
        this.source = source;
        this.sourceUnit = sourceUnit;
    }

    /**
     * Visits a method call expression, checking if it is a target build invocation.
     * If identified as a target invocation, calls {@code handleTargetMethodCallExpression()}.
     * If that method returns {@code true}, resumes normal tree walking; otherwise stops.
     * For non-target method calls, continues normal tree walking regardless.
     *
     * @param call the method call expression that may or may not be an AstBuilder 'from code' invocation
     */
    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        boolean shouldContinueWalking = true;

        if (isBuildInvocation(call)) {
            shouldContinueWalking = handleTargetMethodCallExpression(call);
        }

        if (shouldContinueWalking) {
            // continue normal tree walking
            call.getObjectExpression().visit(this);
            call.getMethod().visit(this);
            call.getArguments().visit(this);
        }
    }

    /**
     * Reports an error back to the source unit with line and column information.
     * The error is collected and processing continues.
     *
     * @param msg the error message
     * @param expr the expression that caused the error
     */
    protected void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
        );
    }

    /**
     * Converts a closure expression into its source code equivalent using the source reader.
     * If an error occurs during conversion, adds an error to the source unit and returns {@code null}.
     *
     * @param expression the closure expression to convert
     * @return the source code string of the closure, or {@code null} if conversion fails
     */
    protected String convertClosureToSource(ClosureExpression expression) {
        try {
            return ClosureUtils.convertClosureToSource(source, expression);
        } catch (Exception e) {
            addError(e.getMessage(), expression);
        }
        return null;
    }

    /**
     * Determines whether a method call expression is a target invocation for special handling.
     * Subclasses override to identify invocations matching their specific criteria.
     *
     * @param call the method call expression to check
     * @return {@code true} if this call is a target invocation requiring special handling
     */
    protected abstract boolean isBuildInvocation(MethodCallExpression call);

    /**
     * Handles a target method call expression by transforming or validating it.
     * Subclasses override to implement the transformation logic.
     *
     * @param call the target method call expression
     * @return {@code true} to continue tree walking, {@code false} to stop
     */
    protected abstract boolean handleTargetMethodCallExpression(MethodCallExpression call);
}
