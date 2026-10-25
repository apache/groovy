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

import groovy.contracts.Spec;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.LoopingStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;

/**
 * Handles {@link Spec} annotations. An unfilled hole (a method or loop with an empty
 * body, or a local variable declaration with no initializer) is compiled to throw
 * {@link UnsupportedOperationException} when reached. Filled holes are left unchanged.
 *
 * @since 7.0.0
 * @see Spec
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SpecASTTransformation implements ASTTransformation {

    private static final ClassNode UOE_TYPE = ClassHelper.make(UnsupportedOperationException.class);

    /**
     * Replaces unfilled {@link Spec} holes with code that throws when reached.
     *
     * @param nodes the annotated AST nodes supplied by the compiler
     * @param source the current source unit
     */
    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode)) return;
        ASTNode target = nodes[1];
        if (target instanceof MethodNode method) {
            if (method.isAbstract() || !isEmpty(method.getCode())) return;
            method.setCode(block(unfilled(describe(method), method)));
        } else if (target instanceof LoopingStatement loop) {
            if (!isEmpty(loop.getLoopBlock())) return;
            Locator locator = locate(source, (Statement) loop);
            String where = describe(locator.method) + ", loop at line " + target.getLineNumber();
            if (locator.block != null) {
                insertBefore(locator.block, (Statement) loop, unfilled(where, target));
            } else {
                loop.setLoopBlock(unfilled(where, target));
            }
        } else if (target instanceof DeclarationExpression decl) {
            if (!(decl.getRightExpression() instanceof EmptyExpression) || decl.isMultipleAssignmentDeclaration()) return;
            Locator locator = locate(source, decl);
            if (locator.block == null) return;
            String where = describe(locator.method) + ", local variable '"
                    + decl.getVariableExpression().getName() + "' at line " + target.getLineNumber();
            insertBefore(locator.block, locator.statement, unfilled(where, target));
        }
    }

    private static boolean isEmpty(final Statement code) {
        return code instanceof EmptyStatement
                || (code instanceof BlockStatement block && block.getStatements().isEmpty());
    }

    private static String describe(final MethodNode method) {
        if (method == null) return "unknown";
        return method.getDeclaringClass().getNameWithoutPackage() + "." + method.getName();
    }

    private static Statement unfilled(final String where, final ASTNode position) {
        Statement result = throwS(ctorX(UOE_TYPE, constX("unfilled @Spec: " + where)));
        result.setSourcePosition(position);
        return result;
    }

    private static void insertBefore(final BlockStatement block, final Statement existing, final Statement added) {
        List<Statement> statements = block.getStatements();
        statements.add(statements.indexOf(existing), added);
    }

    private static Locator locate(final SourceUnit source, final ASTNode target) {
        Locator locator = new Locator(target);
        for (ClassNode classNode : source.getAST().getClasses()) {
            for (MethodNode method : classNode.getAllDeclaredMethods()) {
                locator.visitIn(method);
                if (locator.method != null) return locator;
            }
            for (MethodNode ctor : classNode.getDeclaredConstructors()) {
                locator.visitIn(ctor);
                if (locator.method != null) return locator;
            }
        }
        return locator;
    }

    /**
     * Finds the method and directly enclosing block of a loop statement or local variable
     * declaration.
     */
    private static final class Locator extends ClassCodeVisitorSupport {
        private final ASTNode target;
        private MethodNode current;
        private MethodNode method;
        private BlockStatement block;
        private Statement statement;

        Locator(final ASTNode target) {
            this.target = target;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        void visitIn(final MethodNode candidate) {
            if (candidate.getDeclaringClass() == null || candidate.getCode() == null) return;
            current = candidate;
            candidate.getCode().visit(this);
        }

        @Override
        public void visitBlockStatement(final BlockStatement candidate) {
            if (method != null) return;
            for (Statement s : candidate.getStatements()) {
                if (s == target || (s instanceof ExpressionStatement es && es.getExpression() == target)) {
                    method = current;
                    block = candidate;
                    statement = s;
                    return;
                }
            }
            super.visitBlockStatement(candidate);
        }

        @Override
        protected void visitStatement(final Statement candidate) {
            // a loop that is not a direct child of a block, e.g. a braceless if branch
            if (candidate == target) method = current;
        }
    }
}
