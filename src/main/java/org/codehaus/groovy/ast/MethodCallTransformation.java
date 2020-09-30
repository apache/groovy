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

import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;

public abstract class MethodCallTransformation implements ASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        GroovyCodeVisitor transformer = getTransformer(nodes, sourceUnit);

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
                        // todo: inner class nodes don't have an objectInitializers field available
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

    protected abstract GroovyCodeVisitor getTransformer(ASTNode[] nodes, SourceUnit sourceUnit);
}
