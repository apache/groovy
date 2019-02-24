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
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;
import static org.codehaus.groovy.syntax.Token.newSymbol;

/**
 * Transform try-with-resources to try-catch-finally
 * Reference JLS "14.20.3. try-with-resources"(https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html)
 */
public class TryWithResourcesASTTransformation {
    private AstBuilder astBuilder;

    public TryWithResourcesASTTransformation(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }

    /**
     * Reference JLS "14.20.3. try-with-resources"(https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html)
     *
     * @param tryCatchStatement the try-with-resources statement to transform
     * @return try-catch-finally statement, which contains no resources clause
     */
    public Statement transform(TryCatchStatement tryCatchStatement) {
        if (!asBoolean(tryCatchStatement.getResourceStatements())) {
            return tryCatchStatement;
        }

        if (this.isBasicTryWithResourcesStatement(tryCatchStatement)) {
            return this.transformBasicTryWithResourcesStatement(tryCatchStatement);
        } else {
            return this.transformExtendedTryWithResourcesStatement(tryCatchStatement);
        }
    }

    private boolean isBasicTryWithResourcesStatement(TryCatchStatement tryCatchStatement) {
        if (EmptyStatement.INSTANCE.equals(tryCatchStatement.getFinallyStatement())
                && !asBoolean(tryCatchStatement.getCatchStatements())) {
            return true;
        }

        return false;
    }

    private ExpressionStatement makeVariableDeclarationFinal(ExpressionStatement variableDeclaration) {
        if (!asBoolean(variableDeclaration)) {
            return variableDeclaration;
        }

        if (!(variableDeclaration.getExpression() instanceof DeclarationExpression)) {
            throw new IllegalArgumentException("variableDeclaration is not a declaration statement");
        }

        DeclarationExpression declarationExpression = (DeclarationExpression) variableDeclaration.getExpression();
        if (!(declarationExpression.getLeftExpression() instanceof VariableExpression)) {
            throw astBuilder.createParsingFailedException("The expression statement is not a variable delcaration statement", variableDeclaration);
        }

        VariableExpression variableExpression = (VariableExpression) declarationExpression.getLeftExpression();
        variableExpression.setModifiers(variableExpression.getModifiers() | Opcodes.ACC_FINAL);

        return variableDeclaration;
    }

    private int primaryExcCnt = 0;
    private String genPrimaryExcName() {
        return "__$$primaryExc" + primaryExcCnt++;
    }

    /*
     *   try ResourceSpecification
     *       Block
     *   Catchesopt
     *   Finallyopt
     *
     *   **The above AST should be transformed to the following AST**
     *
     *   try {
     *       try ResourceSpecification
     *           Block
     *   }
     *   Catchesopt
     *   Finallyopt
     */
    private Statement transformExtendedTryWithResourcesStatement(TryCatchStatement tryCatchStatement) {
        /*
         *  try ResourceSpecification
         *      Block
         */
        TryCatchStatement newTryWithResourcesStatement =
                new TryCatchStatement(
                        tryCatchStatement.getTryStatement(),
                        EmptyStatement.INSTANCE);
        tryCatchStatement.getResourceStatements().forEach(newTryWithResourcesStatement::addResource);


        /*
         *   try {
         *       << the following try-with-resources has been transformed >>
         *       try ResourceSpecification
         *           Block
         *   }
         *   Catchesopt
         *   Finallyopt
         */
        TryCatchStatement newTryCatchStatement =
                new TryCatchStatement(
                        astBuilder.createBlockStatement(this.transform(newTryWithResourcesStatement)),
                        tryCatchStatement.getFinallyStatement());

        tryCatchStatement.getCatchStatements().forEach(newTryCatchStatement::addCatch);

        return newTryCatchStatement;
    }

    /*
     *   try (VariableModifiersopt R Identifier = Expression ...)
     *      Block
     *
     *  **The above AST should be transformed to the following AST**
     *
     *   {
     *       final VariableModifiers_minus_final R Identifier = Expression;
     *       Throwable #primaryExc = null;
     *
     *       try ResourceSpecification_tail
     *           Block
     *       catch (Throwable #t) {
     *           #primaryExc = #t;
     *           throw #t;
     *       } finally {
     *           if (Identifier != null) {
     *               if (#primaryExc != null) {
     *                   try {
     *                       Identifier.close();
     *                   } catch (Throwable #suppressedExc) {
     *                       #primaryExc.addSuppressed(#suppressedExc);
     *                   }
     *               } else {
     *                   Identifier.close();
     *               }
     *           }
     *       }
     *   }
     *
     */
    private Statement transformBasicTryWithResourcesStatement(TryCatchStatement tryCatchStatement) {
        // { ... }
        BlockStatement blockStatement = new BlockStatement();

        // final VariableModifiers_minus_final R Identifier = Expression;
        ExpressionStatement firstResourceStatement =
                this.makeVariableDeclarationFinal(
                        tryCatchStatement.getResourceStatement(0));
        astBuilder.appendStatementsToBlockStatement(blockStatement, firstResourceStatement);

        // Throwable #primaryExc = null;
        String primaryExcName = this.genPrimaryExcName();
        VariableExpression primaryExcX = localVarX(primaryExcName, ClassHelper.make(Throwable.class));
        ExpressionStatement primaryExcDeclarationStatement =
                new ExpressionStatement(
                        new DeclarationExpression(
                                primaryExcX,
                                newSymbol(Types.ASSIGN, -1, -1),
                                new ConstantExpression(null)
                        )
                );
        astBuilder.appendStatementsToBlockStatement(blockStatement, primaryExcDeclarationStatement);


        // The generated try-catch-finally statement
        String firstResourceIdentifierName =
                ((DeclarationExpression) tryCatchStatement.getResourceStatement(0).getExpression()).getLeftExpression().getText();

        TryCatchStatement newTryCatchStatement =
                new TryCatchStatement(
                        tryCatchStatement.getTryStatement(),
                        this.createFinallyBlockForNewTryCatchStatement(primaryExcName, firstResourceIdentifierName));

        List<ExpressionStatement> resourceStatements = tryCatchStatement.getResourceStatements();
        // 2nd, 3rd, ..., n'th resources declared in resources
        List<ExpressionStatement> tailResourceStatements = resourceStatements.subList(1, resourceStatements.size());
        tailResourceStatements.stream().forEach(newTryCatchStatement::addResource);

        newTryCatchStatement.addCatch(this.createCatchBlockForOuterNewTryCatchStatement(primaryExcName));
        astBuilder.appendStatementsToBlockStatement(blockStatement, this.transform(newTryCatchStatement));

        return blockStatement;
    }

    /*
     *   catch (Throwable #t) {
     *       #primaryExc = #t;
     *       throw #t;
     *   }
     *
     */
    private CatchStatement createCatchBlockForOuterNewTryCatchStatement(String primaryExcName) {
        // { ... }
        BlockStatement blockStatement = new BlockStatement();
        String tExcName = this.genTExcName();

        // #primaryExc = #t;
        ExpressionStatement primaryExcAssignStatement =
                new ExpressionStatement(
                        new BinaryExpression(
                                new VariableExpression(primaryExcName),
                                newSymbol(Types.ASSIGN, -1, -1),
                                new VariableExpression(tExcName)));
        astBuilder.appendStatementsToBlockStatement(blockStatement, primaryExcAssignStatement);

        // throw #t;
        ThrowStatement throwTExcStatement = new ThrowStatement(new VariableExpression(tExcName));
        astBuilder.appendStatementsToBlockStatement(blockStatement, throwTExcStatement);

        // Throwable #t
        Parameter tExcParameter = new Parameter(ClassHelper.make(Throwable.class), tExcName);

        return new CatchStatement(tExcParameter, blockStatement);
    }

    private int tExcCnt = 0;
    private String genTExcName() {
        return "__$$t" + tExcCnt++;
    }

    /*
     *   finally {
     *       if (Identifier != null) {
     *           if (#primaryExc != null) {
     *              try {
     *                  Identifier.close();
     *              } catch (Throwable #suppressedExc) {
     *                  #primaryExc.addSuppressed(#suppressedExc);
     *              }
     *           } else {
     *               Identifier.close();
     *           }
     *       }
     *   }
     *
     * We can simplify the above code to a Groovy version as follows:
     *
     *   finally {
     *      if (#primaryExc != null)
     *         try {
     *             Identifier?.close();
     *         } catch (Throwable #suppressedExc) {
     *             #primaryExc.addSuppressed(#suppressedExc);
     *         }
     *      else
     *          Identifier?.close();
     *
     *   }
     *
     */
    private BlockStatement createFinallyBlockForNewTryCatchStatement(String primaryExcName, String firstResourceIdentifierName) {
        BlockStatement finallyBlock = new BlockStatement();

        // primaryExc != null
        BooleanExpression conditionExpression =
                new BooleanExpression(
                        new BinaryExpression(
                                new VariableExpression(primaryExcName),
                                newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1),
                                new ConstantExpression(null)));

        // try-catch statement
        TryCatchStatement newTryCatchStatement =
                new TryCatchStatement(
                        astBuilder.createBlockStatement(this.createCloseResourceStatement(firstResourceIdentifierName)), // { Identifier?.close(); }
                        EmptyStatement.INSTANCE);


        String suppressedExcName = this.genSuppressedExcName();
        newTryCatchStatement.addCatch(
                // catch (Throwable #suppressedExc) { .. }
                new CatchStatement(
                        new Parameter(ClassHelper.make(Throwable.class), suppressedExcName),
                        astBuilder.createBlockStatement(this.createAddSuppressedStatement(primaryExcName, suppressedExcName)) // #primaryExc.addSuppressed(#suppressedExc);
                )
        );

        // if (#primaryExc != null) { ... }
        IfStatement ifStatement =
                new IfStatement(
                        conditionExpression,
                        newTryCatchStatement,
                        this.createCloseResourceStatement(firstResourceIdentifierName) // Identifier?.close();
                );
        astBuilder.appendStatementsToBlockStatement(finallyBlock, ifStatement);

        return astBuilder.createBlockStatement(finallyBlock);
    }

    private int suppressedExcCnt = 0;
    private String genSuppressedExcName() {
        return "__$$suppressedExc" + suppressedExcCnt++;
    }

    /*
     *  Identifier?.close();
     */
    private ExpressionStatement createCloseResourceStatement(String firstResourceIdentifierName) {
        MethodCallExpression closeMethodCallExpression =
                new MethodCallExpression(new VariableExpression(firstResourceIdentifierName), "close", new ArgumentListExpression());

        closeMethodCallExpression.setImplicitThis(false);
        closeMethodCallExpression.setSafe(true);

        return new ExpressionStatement(closeMethodCallExpression);
    }

    /*
     *  #primaryExc.addSuppressed(#suppressedExc);
     */
    private ExpressionStatement createAddSuppressedStatement(String primaryExcName, String suppressedExcName) {
        MethodCallExpression addSuppressedMethodCallExpression =
                new MethodCallExpression(
                        new VariableExpression(primaryExcName),
                        "addSuppressed",
                        new ArgumentListExpression(Collections.singletonList(new VariableExpression(suppressedExcName))));
        addSuppressedMethodCallExpression.setImplicitThis(false);
        addSuppressedMethodCallExpression.setSafe(true);

        return new ExpressionStatement(addSuppressedMethodCallExpression);
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
    public BinaryExpression transformResourceAccess(Expression variableAccessExpression) {
        return new BinaryExpression(
                new VariableExpression(genResourceName()),
                newSymbol(Types.ASSIGN, -1, -1),
                variableAccessExpression
        );
    }

    private int resourceCnt = 0;
    private String genResourceName() {
        return "__$$resource" + resourceCnt++;
    }

}
