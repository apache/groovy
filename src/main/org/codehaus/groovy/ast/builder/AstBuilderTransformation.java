/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.builder;

import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation to capture ASTBuilder from code statements.
 * <p>
 * The AstBuilder "from code" approach is used with a single Closure
 * parameter. This transformation converts the ClosureExpression back
 * into source code and rewrites the AST so that the "from string"
 * builder is invoked on the source. In order for this to work, the
 * closure source must be given a goto label. It is the "from string"
 * approach's responsibility to remove the BlockStatement created
 * by the label.
 *
 * @author Hamlet D'Arcy
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AstBuilderTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        // todo : are there other import types that can be specified?
        AstBuilderInvocationTrap transformer = new AstBuilderInvocationTrap(
                sourceUnit.getAST().getImports(),
                sourceUnit.getAST().getStarImports(),
                sourceUnit.getSource(),
                sourceUnit
        );
        if (nodes != null) {
            for (ASTNode it : nodes) {
                if (!(it instanceof AnnotationNode) && !(it instanceof ClassNode)) {
                    it.visit(transformer);
                }
            }
        }
        if (sourceUnit.getAST() != null) {
            sourceUnit.getAST().visit(transformer);
            if (sourceUnit.getAST().getStatementBlock() != null) {
                sourceUnit.getAST().getStatementBlock().visit(transformer);
            }
            if (sourceUnit.getAST().getClasses() != null) {
                for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
                    if (classNode.getMethods() != null) {
                        for (MethodNode node : classNode.getMethods()) {
                            if (node != null && node.getCode() != null) {
                                node.getCode().visit(transformer);
                            }
                        }
                    }

                    try {
                        if (classNode.getDeclaredConstructors() != null) {
                            for (MethodNode node : classNode.getDeclaredConstructors()) {
                                if (node != null && node.getCode() != null) {
                                    node.getCode().visit(transformer);
                                }
                            }
                        }
                    } catch (MissingPropertyException ignored) {
                        // todo: inner class nodes don't have a constructors field available
                    }

                    // all properties are also always fields
                    if (classNode.getFields() != null) {
                        for (FieldNode node : classNode.getFields()) {
                            if (node.getInitialValueExpression() != null) {
                                node.getInitialValueExpression().visit(transformer);
                            }
                        }
                    }

                    try {
                        if (classNode.getObjectInitializerStatements() != null) {
                            for (Statement node : classNode.getObjectInitializerStatements()) {
                                if (node != null) {
                                    node.visit(transformer);
                                }
                            }
                        }
                    } catch (MissingPropertyException ignored) {
                        // todo: inner class nodes don't have a objectInitializers field available
                    }

                    // todo: is there anything to do with the module ???
                }
            }
            if (sourceUnit.getAST().getMethods() != null) {
                for (MethodNode node : sourceUnit.getAST().getMethods()) {
                    if (node != null) {
                        if (node.getParameters() != null) {
                            for (Parameter parameter : node.getParameters()) {
                                if (parameter != null && parameter.getInitialExpression() != null) {
                                    parameter.getInitialExpression().visit(transformer);
                                }
                            }
                        }
                        if (node.getCode() != null) {
                            node.getCode().visit(transformer);
                        }
                    }
                }
            }
        }
    }

    /**
     * This class traps invocations of AstBuilder.build(CompilePhase, boolean, Closure) and converts
     * the contents of the closure into expressions by reading the source of the Closure and sending
     * that as a String to AstBuilder.build(String, CompilePhase, boolean) at runtime.
     */
    private static class AstBuilderInvocationTrap extends CodeVisitorSupport {

        private final List<String> factoryTargets = new ArrayList<String>();
        private final ReaderSource source;
        private final SourceUnit sourceUnit;

        /**
         * Creates the trap and captures all the ways in which a class may be referenced via imports.
         *
         * @param imports        all the imports from the source
         * @param importPackages all the imported packages from the source
         * @param source         the reader source that contains source for the SourceUnit
         * @param sourceUnit     the source unit being compiled. Used for error messages.
         */
        AstBuilderInvocationTrap(List<ImportNode> imports, List<ImportNode> importPackages, ReaderSource source, SourceUnit sourceUnit) {
            if (source == null) throw new IllegalArgumentException("Null: source");
            if (sourceUnit == null) throw new IllegalArgumentException("Null: sourceUnit");
            this.source = source;
            this.sourceUnit = sourceUnit;

            // factory type may be references as fully qualified, an import, or an alias
            factoryTargets.add("org.codehaus.groovy.ast.builder.AstBuilder");//default package

            if (imports != null) {
                for (ImportNode importStatement : imports) {
                    if ("org.codehaus.groovy.ast.builder.AstBuilder".equals(importStatement.getType().getName())) {
                        factoryTargets.add(importStatement.getAlias());
                    }
                }
            }

            if (importPackages != null) {
                for (ImportNode importPackage : importPackages) {
                    if ("org.codehaus.groovy.ast.builder.".equals(importPackage.getPackageName())) {
                        factoryTargets.add("AstBuilder");
                        break;
                    }
                }
            }
        }

        /**
         * Reports an error back to the source unit.
         *
         * @param msg  the error message
         * @param expr the expression that caused the error message.
         */
        private void addError(String msg, ASTNode expr) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                    new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
            );
        }


        /**
         * Attempts to find AstBuilder 'from code' invocations. When found, converts them into calls
         * to the 'from string' approach.
         *
         * @param call the method call expression that may or may not be an AstBuilder 'from code' invocation.
         */
        public void visitMethodCallExpression(MethodCallExpression call) {

            if (isBuildInvocation(call)) {

                ClosureExpression closureExpression = getClosureArgument(call);
                List<Expression> otherArgs = getNonClosureArguments(call);
                String source = convertClosureToSource(closureExpression);

                // parameter order is build(CompilePhase, boolean, String)
                otherArgs.add(new ConstantExpression(source));
                call.setArguments(new ArgumentListExpression(otherArgs));
                call.setMethod(new ConstantExpression("buildFromBlock"));
                call.setSpreadSafe(false);
                call.setSafe(false);
                call.setImplicitThis(false);
            } else {
                // continue normal tree walking
                call.getObjectExpression().visit(this);
                call.getMethod().visit(this);
                call.getArguments().visit(this);
            }
        }

        private List<Expression> getNonClosureArguments(MethodCallExpression call) {
            List<Expression> result = new ArrayList<Expression>();
            if (call.getArguments() instanceof TupleExpression) {
                for (ASTNode node : ((TupleExpression) call.getArguments()).getExpressions()) {
                    if (!(node instanceof ClosureExpression)) {
                        result.add((Expression) node);
                    }
                }
            }
            return result;
        }

        private ClosureExpression getClosureArgument(MethodCallExpression call) {

            if (call.getArguments() instanceof TupleExpression) {
                for (ASTNode node : ((TupleExpression) call.getArguments()).getExpressions()) {
                    if (node instanceof ClosureExpression) {
                        return (ClosureExpression) node;
                    }
                }
            }
            return null;
        }

        /**
         * Looks for method calls on the AstBuilder class called build that take
         * a Closure as parameter. This is all needed b/c build is overloaded.
         *
         * @param call the method call expression, may not be null
         */
        private boolean isBuildInvocation(MethodCallExpression call) {
            if (call == null) throw new IllegalArgumentException("Null: call");

            // is method name correct?
            if (call.getMethod() instanceof ConstantExpression && "buildFromCode".equals(((ConstantExpression) call.getMethod()).getValue())) {

                // is method object correct type?
                if (call.getObjectExpression() != null && call.getObjectExpression().getType() != null) {
                    String name = call.getObjectExpression().getType().getName();
                    if (name != null && !"".equals(name) && factoryTargets.contains(name)) {

                        // is one of the arguments a closure?
                        if (call.getArguments() != null && call.getArguments() instanceof TupleExpression) {
                            if (((TupleExpression) call.getArguments()).getExpressions() != null) {
                                for (ASTNode node : ((TupleExpression) call.getArguments()).getExpressions()) {
                                    if (node instanceof ClosureExpression) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Converts a ClosureExpression into the String source.
         *
         * @param expression a closure
         * @return the source the closure was created from
         */
        private String convertClosureToSource(ClosureExpression expression) {
            if (expression == null) throw new IllegalArgumentException("Null: expression");

            StringBuilder result = new StringBuilder();
            for (int x = expression.getLineNumber(); x <= expression.getLastLineNumber(); x++) {
                String line = source.getLine(x, null);
                if (line == null) {
                    addError(
                            "Error calculating source code for expression. Trying to read line " + x + " from " + source.getClass(),
                            expression
                    );
                }
                if (x == expression.getLastLineNumber()) {
                    line = line.substring(0, expression.getLastColumnNumber() - 1);
                }
                if (x == expression.getLineNumber()) {
                    line = line.substring(expression.getColumnNumber() - 1);
                }
                //restoring line breaks is important b/c of lack of semicolons
                result.append(line).append('\n');
            }


            String source = result.toString().trim();
            if (!source.startsWith("{")) {
                addError(
                        "Error converting ClosureExpression into source code. Closures must start with {. Found: " + source,
                        expression
                );
            }

            return source;
        }
    }
}


