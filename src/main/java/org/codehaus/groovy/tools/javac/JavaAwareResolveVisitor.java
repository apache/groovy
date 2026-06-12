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
package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;

import static org.apache.groovy.ast.tools.ConstructorNodeUtils.getFirstIfSpecialConstructorCall;

/**
 * Resolve visitor variant used during joint compilation to revisit special
 * constructor calls and track missing imports for Java sources.
 */
public class JavaAwareResolveVisitor extends ResolveVisitor {

    /**
     * Creates a resolve visitor for the supplied compilation unit.
     *
     * @param cu the compilation unit being resolved
     */
    public JavaAwareResolveVisitor(final CompilationUnit cu) {
        super(cu);
    }

    /**
     * Resolves a constructor and then revisits any leading special constructor
     * call expression.
     *
     * @param node the constructor being visited
     */
    @Override
    public void visitConstructor(final ConstructorNode node) {
        super.visitConstructor(node);
        Statement code = node.getCode();
        Expression cce = getFirstIfSpecialConstructorCall(code);
        if (cce != null)
            cce.visit(this);
    }

    /**
     * Suppresses class-code-container traversal so normal resolution logic can
     * handle the statements.
     *
     * @param stmt the statement container being considered
     */
    @Override
    protected void visitClassCodeContainer(final Statement stmt) {
        // do nothing here; leave it to the normal resolving
    }

    /**
     * Marks the source unit when a type resolution error indicates imports may
     * need to be reconsidered.
     *
     * @param error the error message
     * @param node the node that triggered the error
     */
    @Override
    public void addError(final String error, final ASTNode node) {
        if (error.startsWith("unable to resolve")) // GROOVY-11806
            getSourceUnit().getAST().putNodeMetaData("require.imports", Boolean.TRUE);
    }
}
