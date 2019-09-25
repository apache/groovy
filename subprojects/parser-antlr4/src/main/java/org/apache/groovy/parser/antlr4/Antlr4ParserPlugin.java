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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

import java.io.IOException;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * A parser plugin for the new parser
 */
public class Antlr4ParserPlugin implements ParserPlugin {
    private ReaderSource readerSource;
    private CompilerConfiguration compilerConfiguration;

    public Antlr4ParserPlugin(CompilerConfiguration compilerConfiguration) {
        this.compilerConfiguration = compilerConfiguration;
    }

    @Override
    public Reduction parseCST(SourceUnit sourceUnit, java.io.Reader reader) throws CompilationFailedException {
        ReaderSource readerSource = sourceUnit.getSource();

        try (Reader sourceReader = null != readerSource ? readerSource.getReader() : null) {
            if (null != readerSource && null != sourceReader) {
                this.readerSource = readerSource;
            } else {
                this.readerSource = new StringReaderSource(IOGroovyMethods.getText(reader), sourceUnit.getConfiguration());
            }
        } catch (IOException e) {
            throw new GroovyBugError("Failed to create StringReaderSource instance", e);
        }

        return null;
    }

    @Override
    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        ReaderSource readerSource = sourceUnit.getSource();

        try (Reader sourceReader = null != readerSource ? readerSource.getReader() : null) {
            if (null == readerSource || null == sourceReader) {
                sourceUnit.setSource(this.readerSource);
            }
        } catch (IOException e) {
            sourceUnit.setSource(this.readerSource);
        }

        AstBuilder builder = new AstBuilder(sourceUnit, compilerConfiguration);

        return builder.buildAST();
    }

    /**
     * Create ClassNode instance for type string
     *
     * @param typeStr type string, e.g. List
     * @return a {@link ClassNode} instance
     * @since 3.0.0
     */
    public ClassNode makeType(String typeStr) {
        SourceUnit sourceUnit =
                new SourceUnit(
                        "Script" + System.nanoTime(),
                        typeStr + " v",
                        compilerConfiguration,
                        AccessController.doPrivileged(
                                new PrivilegedAction<GroovyClassLoader>() {
                                    @Override
                                    public GroovyClassLoader run() {
                                        return new GroovyClassLoader();
                                    }
                                }),
                        new ErrorCollector(compilerConfiguration)
                );
        AstBuilder builder = new AstBuilder(sourceUnit, compilerConfiguration);
        ModuleNode moduleNode = builder.buildAST();

        List<Statement> statementList = moduleNode.getStatementBlock().getStatements();

        Statement statement;
        try {
            statement = statementList.get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new GroovyBugError(statementList + " is empty");
        }

        if (!(statement instanceof ExpressionStatement)) {
            throw new GroovyBugError(statement + " is not an instance of ExpressionStatement");
        }

        ExpressionStatement expressionStatement = (ExpressionStatement) statement;
        Expression expression = expressionStatement.getExpression();

        if (!(expression instanceof DeclarationExpression)) {
            throw new GroovyBugError(expression + " is not an instance of DeclarationExpression");
        }

        DeclarationExpression declarationExpression = (DeclarationExpression) expression;
        Expression leftExpression = declarationExpression.getLeftExpression();

        if (!(leftExpression instanceof VariableExpression)) {
            throw new GroovyBugError(leftExpression + " is not an instance of VariableExpression");
        }

        VariableExpression variableExpression = (VariableExpression) leftExpression;

        return variableExpression.getType();
    }
}
