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
package org.apache.groovy.contracts.ast;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Shared helper for the loop-level contract transforms ({@link LoopInvariantASTTransformation}
 * and {@link LoopVariantASTTransformation}).
 * <p>
 * Both transforms lift expressions out of an {@code @Invariant}/{@code @Decreases} annotation
 * closure and inline them into the loop body. Because those expressions originally lived inside
 * an annotation member, the compiler's {@link VariableScopeVisitor} never descended into them, so
 * their {@link org.codehaus.groovy.ast.expr.VariableExpression}s reference unresolved
 * {@link org.codehaus.groovy.ast.DynamicVariable}s. Under dynamic Groovy this resolves at runtime,
 * but {@code @TypeChecked}/{@code @CompileStatic} (which run later) then see such references as
 * {@code java.lang.Object} and fail type checking.
 * <p>
 * Re-running variable scope analysis once the expressions are real loop-body statements links each
 * reference to its enclosing declaration. This mirrors the compiler's own idiom of running a fresh
 * {@link VariableScopeVisitor} per class (see {@code ResolveVisitor}).
 */
final class LoopContractSupport {

    private LoopContractSupport() {
    }

    /**
     * Re-resolves variable scopes for the classes of the given source so that variable references
     * inlined out of a loop-contract annotation closure are bound to their enclosing declarations.
     *
     * @param source the current source unit
     */
    static void resolveVariableScopes(final SourceUnit source) {
        if (source == null || source.getAST() == null) return;
        for (ClassNode classNode : source.getAST().getClasses()) {
            new VariableScopeVisitor(source).visitClass(classNode);
        }
    }

    /**
     * Find the name of the class enclosing a loop statement, so its variant check can be gated by
     * the {@code -ea}/{@code -da} configuration for that class. Returns {@code null} (the global
     * default) if the enclosing class cannot be determined.
     *
     * @param source the current source unit
     * @param target the loop statement to locate
     * @return the enclosing class name, or {@code null}
     */
    static String enclosingClassName(final SourceUnit source, final ASTNode target) {
        if (source == null || source.getAST() == null || target == null) return null;
        for (ClassNode classNode : source.getAST().getClasses()) {
            EnclosingFinder finder = new EnclosingFinder(target);
            finder.visitClass(classNode);
            if (finder.found) return classNode.getName();
        }
        return null;
    }

    private static final class EnclosingFinder extends ClassCodeVisitorSupport {
        private final ASTNode target;
        private boolean found;

        EnclosingFinder(final ASTNode target) {
            this.target = target;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitWhileLoop(final WhileStatement loop) {
            if (loop == target) found = true;
            super.visitWhileLoop(loop);
        }

        @Override
        public void visitForLoop(final ForStatement loop) {
            if (loop == target) found = true;
            super.visitForLoop(loop);
        }

        @Override
        public void visitDoWhileLoop(final DoWhileStatement loop) {
            if (loop == target) found = true;
            super.visitDoWhileLoop(loop);
        }
    }
}
