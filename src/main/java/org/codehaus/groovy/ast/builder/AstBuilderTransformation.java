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
package org.codehaus.groovy.ast.builder;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodCallTransformation;
import org.codehaus.groovy.ast.MethodInvocationTrap;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
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
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AstBuilderTransformation extends MethodCallTransformation {

    @Override
    protected GroovyCodeVisitor getTransformer(ASTNode[] nodes, SourceUnit sourceUnit) {
        // todo : are there other import types that can be specified?
        return new AstBuilderInvocationTrap(
            sourceUnit.getAST().getImports(),
            sourceUnit.getAST().getStarImports(),
            sourceUnit.getSource(),
            sourceUnit
        );
    }

    /**
     * This class traps invocations of AstBuilder.build(CompilePhase, boolean, Closure) and converts
     * the contents of the closure into expressions by reading the source of the Closure and sending
     * that as a String to AstBuilder.build(String, CompilePhase, boolean) at runtime.
     */
    private static class AstBuilderInvocationTrap extends MethodInvocationTrap {

        private final List<String> factoryTargets = new ArrayList<>();

        /**
         * Creates the trap and captures all the ways in which a class may be referenced via imports.
         *
         * @param imports        all the imports from the source
         * @param importPackages all the imported packages from the source
         * @param source         the reader source that contains source for the SourceUnit
         * @param sourceUnit     the source unit being compiled. Used for error messages.
         */
        AstBuilderInvocationTrap(List<ImportNode> imports, List<ImportNode> importPackages, ReaderSource source, SourceUnit sourceUnit) {
            super(source, sourceUnit);

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
        
        @Override
        protected boolean handleTargetMethodCallExpression(MethodCallExpression call) {
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
            
            return false;
        }

        private static List<Expression> getNonClosureArguments(MethodCallExpression call) {
            List<Expression> result = new ArrayList<>();
            if (call.getArguments() instanceof TupleExpression) {
                for (ASTNode node : ((TupleExpression) call.getArguments()).getExpressions()) {
                    if (!(node instanceof ClosureExpression)) {
                        result.add((Expression) node);
                    }
                }
            }
            return result;
        }

        private static ClosureExpression getClosureArgument(MethodCallExpression call) {

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
        @Override
        protected boolean isBuildInvocation(MethodCallExpression call) {
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
    }
}


