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

public abstract class MethodInvocationTrap extends CodeVisitorSupport {

    protected final ReaderSource source;
    protected final SourceUnit sourceUnit;

    public MethodInvocationTrap(ReaderSource source, SourceUnit sourceUnit) {
        if (source == null) throw new IllegalArgumentException("Null: source");
        if (sourceUnit == null) throw new IllegalArgumentException("Null: sourceUnit");
        this.source = source;
        this.sourceUnit = sourceUnit;
    }

    /**
     * Attempts to find AstBuilder 'from code' invocations. When found, converts them into calls
     * to the 'from string' approach.
     *
     * @param call the method call expression that may or may not be an AstBuilder 'from code' invocation.
     */
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
     * Reports an error back to the source unit.
     *
     * @param msg  the error message
     * @param expr the expression that caused the error message.
     */
    protected void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
        );
    }

    /**
     * Converts a ClosureExpression into the String source.
     *
     * @param expression a closure
     * @return the source the closure was created from
     */
    protected String convertClosureToSource(ClosureExpression expression) {
        try {
            return ClosureUtils.convertClosureToSource(source, expression);
        } catch (Exception e) {
            addError(e.getMessage(), expression);
        }
        return null;
    }

    protected abstract boolean handleTargetMethodCallExpression(MethodCallExpression call);

    protected abstract boolean isBuildInvocation(MethodCallExpression call);
}
