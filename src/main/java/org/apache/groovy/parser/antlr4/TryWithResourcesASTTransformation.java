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
package org.apache.groovy.parser.antlr4;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.tools.GeneralUtils.ASSIGN;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.catchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.tryCatchS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * Transform try-with-resources to try-catch-finally
 * Reference JLS "14.20.3. try-with-resources"(https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html)
 */
public class TryWithResourcesASTTransformation {

    public TryWithResourcesASTTransformation(final AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }
    private AstBuilder astBuilder;

    private int resourceCount;
    private String nextResourceName() {
        return "__$$resource" + resourceCount++;
    }

    private int throwableCount;
    private String nextThrowableName() {
        return "__$$t" + throwableCount++;
    }

    private int primaryExCount;
    private String nextPrimaryExName() {
        return "__$$primaryExc" + primaryExCount++;
    }

    private int suppressedExCount;
    private String nextSuppressedExName() {
        return "__$$suppressedExc" + suppressedExCount++;
    }

    //--------------------------------------------------------------------------

    /**
     * @param tryCatchStatement the try-with-resources statement to transform
     * @return try-catch-finally statement which contains no resources clause
     */
    public Statement transform(final TryCatchStatement tryCatchStatement) {
        if (!asBoolean(tryCatchStatement.getResourceStatements())) {
            return tryCatchStatement;
        }
        if (isBasicTryWithResourcesStatement(tryCatchStatement)) {
            return transformBasicTryWithResourcesStatement(tryCatchStatement);
        } else {
            return transformExtendedTryWithResourcesStatement(tryCatchStatement);
        }
    }

    private boolean isBasicTryWithResourcesStatement(final TryCatchStatement tryCatchStatement) {
        return (!asBoolean(tryCatchStatement.getCatchStatements())
            && tryCatchStatement.getFinallyStatement() instanceof EmptyStatement);
    }

    private Statement makeVariableDeclarationFinal(final ExpressionStatement variableDeclaration) {
        if (!asBoolean(variableDeclaration)) {
            return variableDeclaration;
        }

        if (!(variableDeclaration.getExpression() instanceof DeclarationExpression)) {
            throw new IllegalArgumentException("variableDeclaration is not a declaration statement");
        }

        Expression targetExpression = ((DeclarationExpression) variableDeclaration.getExpression()).getLeftExpression();
        if (!(targetExpression instanceof VariableExpression)) {
            throw astBuilder.createParsingFailedException("The expression statement is not a variable delcaration statement", variableDeclaration);
        }

        VariableExpression variableExpression = (VariableExpression) targetExpression;
        variableExpression.setModifiers(variableExpression.getModifiers() | Opcodes.ACC_FINAL);

        return variableDeclaration;
    }

    /**
     * Transforms:
     * <pre>
     * try ResourceSpecification
     *     Block
     * Catchesopt
     * Finallyopt
     * </pre>
     * into:
     * <pre>
     * try {
     *     try ResourceSpecification
     *         Block
     * }
     * Catchesopt
     * Finallyopt
     * </pre>
     */
    private Statement transformExtendedTryWithResourcesStatement(final TryCatchStatement tryCatchFinally) {
        /*
         * try ResourceSpecification
         *     Block
         */
        TryCatchStatement newTryWithResources = tryCatchS(tryCatchFinally.getTryStatement());
        tryCatchFinally.getResourceStatements().forEach(newTryWithResources::addResource);

        /*
         * try {
         *     << the following try-with-resources has been transformed >>
         *     try ResourceSpecification
         *         Block
         * }
         * Catchesopt
         * Finallyopt
         */
        TryCatchStatement newTryCatchFinally = tryCatchS(
                block(transform(newTryWithResources)),
                tryCatchFinally.getFinallyStatement()
        );
        tryCatchFinally.getCatchStatements().forEach(newTryCatchFinally::addCatch);
        return newTryCatchFinally;
    }

    /**
     * Transforms:
     * <pre>
     * try (VariableModifiersopt R Identifier = Expression ...)
     *    Block
     * </pre>
     * into:
     * <pre>
     * {
     *     final VariableModifiers_minus_final R resourceIdentifier = Expression;
     *     Throwable #primaryException = null;
     *     try ResourceSpecification_tail
     *         Block
     *     catch (Throwable #t) {
     *         #primaryException = #t;
     *         throw #t;
     *     } finally {
     *         if (resourceIdentifier != null) {
     *             if (#primaryException != null) {
     *                 try {
     *                     resourceIdentifier?.close();
     *                 } catch (Throwable #suppressedException) {
     *                     #primaryException.addSuppressed(#suppressedException);
     *                 }
     *             } else {
     *                 resourceIdentifier?.close();
     *             }
     *         }
     *     }
     * }
     * </pre>
     */
    private Statement transformBasicTryWithResourcesStatement(final TryCatchStatement tryWithResources) {
        BlockStatement blockStatement = new BlockStatement();

        // final VariableModifiers_minus_final R Identifier = Expression;
        ExpressionStatement firstResourceDeclaration = tryWithResources.getResourceStatement(0);
        blockStatement.addStatement(makeVariableDeclarationFinal(firstResourceDeclaration));

        // Throwable #primaryException = null;
        String primaryExceptionName = nextPrimaryExName();
        blockStatement.addStatement(declS(
                localVarX(primaryExceptionName, ClassHelper.THROWABLE_TYPE.getPlainNodeReference()),
                nullX()
        ));

        String firstResourceIdentifier = ((DeclarationExpression) firstResourceDeclaration.getExpression()).getLeftExpression().getText();

        TryCatchStatement newTryCatchFinally = tryCatchS(
                tryWithResources.getTryStatement(), // Block
                createFinallyBlockForNewTryCatchStatement(primaryExceptionName, firstResourceIdentifier) // close resource and propagate throwable(s)
        );
        // 2nd, 3rd, ..., n'th resources declared in resources
        tryWithResources.getResourceStatements().stream().skip(1).forEach(newTryCatchFinally::addResource);
        newTryCatchFinally.addCatch(createCatchBlockForOuterNewTryCatchStatement(primaryExceptionName));

        blockStatement.addStatement(transform(newTryCatchFinally)); // transform remaining resources

        return blockStatement;
    }

    private CatchStatement createCatchBlockForOuterNewTryCatchStatement(final String primaryExceptionName) {
        String throwableName = nextThrowableName();
        Parameter catchParameter = new Parameter(ClassHelper.THROWABLE_TYPE.getPlainNodeReference(), throwableName);

        return catchS(catchParameter, block( // catch (Throwable #t) {
                assignS(varX(primaryExceptionName), varX(throwableName)), // #primaryException = #t;
                throwS(varX(throwableName)) // throw #t;
        )); // }
    }

    /**
     * Transforms:
     * <pre>
     * finally {
     *     if (#resourceIdentifier != null) {
     *         if (#primaryException != null) {
     *            try {
     *                #resourceIdentifier?.close();
     *            } catch (Throwable #suppressedException) {
     *                #primaryException.addSuppressed(#suppressedException);
     *            }
     *         } else {
     *             #resourceIdentifier?.close();
     *         }
     *     }
     * }
     * </pre>
     * into:
     * <pre>
     * finally {
     *    if (#primaryException != null)
     *       try {
     *           #resourceIdentifier?.close();
     *       } catch (Throwable #suppressedException) {
     *           #primaryException.addSuppressed(#suppressedException);
     *       }
     *    else
     *        #resourceIdentifier?.close();
     * }
     * </pre>
     */
    private Statement createFinallyBlockForNewTryCatchStatement(final String primaryExceptionName, final String firstResourceIdentifierName) {
        String suppressedExceptionName = nextSuppressedExName();
        TryCatchStatement newTryCatch = tryCatchS(block(createCloseResourceStatement(firstResourceIdentifierName)));
        newTryCatch.addCatch(catchS(
                new Parameter(ClassHelper.THROWABLE_TYPE.getPlainNodeReference(), suppressedExceptionName),
                block(createAddSuppressedStatement(primaryExceptionName, suppressedExceptionName))
        ));

        return block(ifElseS(notNullX(varX(primaryExceptionName)), // if (#primaryException != null)
                newTryCatch, // try { #resource?.close() } catch (Throwable #suppressed) { #primary.addSuppressed(#suppressed) }
                createCloseResourceStatement(firstResourceIdentifierName) // else #resource?.close()
        ));
    }

    private static Statement createCloseResourceStatement(final String resourceIdentifierName) {
        MethodCallExpression closeMethodCallExpression = callX(varX(resourceIdentifierName), "close");
        closeMethodCallExpression.setImplicitThis(false);
        closeMethodCallExpression.setSafe(true);
        return stmt(closeMethodCallExpression);
    }

    private static Statement createAddSuppressedStatement(final String primaryException, final String suppressedException) {
        MethodCallExpression addSuppressedMethodCallExpression = callX(varX(primaryException), "addSuppressed", varX(suppressedException));
        addSuppressedMethodCallExpression.setImplicitThis(false);
        addSuppressedMethodCallExpression.setSafe(true);
        return stmt(addSuppressedMethodCallExpression);
    }

    /**
     * See https://docs.oracle.com/javase/specs/jls/se9/html/jls-14.html
     * 14.20.3.1. Basic try-with-resources
     *
     * If a basic try-with-resource statement is of the form:
     * try (VariableAccess ...)
     *      Block
     *
     * then the resource is first converted to a local variable declaration by the following translation:
     * try (T #r = VariableAccess ...) {
     *      Block
     * }
     */
    public BinaryExpression transformResourceAccess(final Expression variableExpression) {
        return binX(varX(nextResourceName()), ASSIGN, variableExpression);
    }
}
