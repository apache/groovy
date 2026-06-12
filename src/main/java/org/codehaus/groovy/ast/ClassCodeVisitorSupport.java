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
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ErrorCollecting;

/**
 * Abstract base class for visitors that process class nodes and their members, extending the
 * default visitor traversal with class-specific and annotation processing.
 *
 * <p>This visitor combines {@link CodeVisitorSupport} with {@link GroovyClassVisitor} to provide
 * comprehensive traversal of class structures including:
 * <ul>
 *   <li>Class-level annotations and package/import declarations</li>
 *   <li>Class contents (fields, methods, properties) with member-level annotations</li>
 *   <li>Method parameters and their annotations</li>
 *   <li>Object initializer statements</li>
 * </ul>
 *
 * <p>Subclasses typically override specific visit methods to perform custom processing while
 * inheriting default traversal behavior. Common extension points include:
 * <ul>
 *   <li>{@link #visitStatement(Statement)} - hook for statement-level processing</li>
 *   <li>{@link #visitStatementAnnotations(Statement)} - hook for statement annotations in loops</li>
 *   <li>{@link #visitAnnotation(AnnotationNode)} - override to process annotations</li>
 * </ul>
 *
 * <p>Implementations must provide {@link #getSourceUnit()} to support error reporting.
 * The {@link ErrorCollecting} interface enables error accumulation during traversal.
 *
 * @see CodeVisitorSupport for expression and basic statement processing
 * @see GroovyClassVisitor for class-level visiting interface
 * @see ErrorCollecting for error handling
 */
public abstract class ClassCodeVisitorSupport extends CodeVisitorSupport implements ErrorCollecting, GroovyClassVisitor {

    /**
     * Visits a {@link ClassNode}, processing its annotations, package, imports, contents, and object initializers.
     *
     * @param node the class node to visit
     */
    @Override
    public void visitClass(ClassNode node) {
        visitAnnotations(node);
        visitPackage(node.getPackage());
        visitImports(node.getModule());
        node.visitContents(this);
        visitObjectInitializerStatements(node);
    }

    /**
     * Visits annotations on an {@link AnnotatedNode}, delegating to {@link #visitAnnotation(AnnotationNode)}.
     *
     * @param node the annotated node containing annotations to visit
     */
    public void visitAnnotations(AnnotatedNode node) {
        visitAnnotations(node.getAnnotations());
    }

    /**
     * Visits a collection of {@link AnnotationNode}s by processing each annotation individually.
     *
     * @param nodes iterable collection of annotation nodes to visit
     */
    protected final void visitAnnotations(Iterable<AnnotationNode> nodes) {
        for (AnnotationNode node : nodes) {
            visitAnnotation(node);
        }
    }

    /**
     * Visits an individual {@link AnnotationNode}, traversing all member expression values.
     * Subclasses may override to perform custom annotation processing.
     *
     * @param node the annotation node to visit
     */
    protected void visitAnnotation(AnnotationNode node) {
        for (Expression expr : node.getMembers().values()) {
            expr.visit(this);
        }
    }

    /**
     * Visits a {@link PackageNode} if present, processing its annotations.
     *
     * @param node the package node, may be null
     */
    public void visitPackage(PackageNode node) {
        if (node != null) {
            visitAnnotations(node);
            node.visit(this);
        }
    }

    /**
     * Visits all import declarations from a {@link ModuleNode}, including regular imports, star imports,
     * static imports, and static star imports, processing their annotations.
     *
     * @param node the module node containing imports, may be null
     */
    public void visitImports(ModuleNode node) {
        if (node != null) {
            for (ImportNode importNode : node.getImports()) {
                visitAnnotations(importNode);
                importNode.visit(this);
            }
            for (ImportNode importStarNode : node.getStarImports()) {
                visitAnnotations(importStarNode);
                importStarNode.visit(this);
            }
            for (ImportNode importStaticNode : node.getStaticImports().values()) {
                visitAnnotations(importStaticNode);
                importStaticNode.visit(this);
            }
            for (ImportNode importStaticStarNode : node.getStaticStarImports().values()) {
                visitAnnotations(importStaticStarNode);
                importStaticStarNode.visit(this);
            }
        }
    }

    /**
     * Visits a {@link ConstructorNode}, processing its annotations, parameter annotations, and code block.
     *
     * @param node the constructor node to visit
     */
    @Override
    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node, true);
    }

    /**
     * Visits a {@link MethodNode}, processing its annotations, parameter annotations, and code block.
     *
     * @param node the method node to visit
     */
    @Override
    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node, false);
    }

    /**
     * Visits a constructor or method node (implementation detail for both visit methods).
     * Processes the node's annotations, all parameter annotations, and code block.
     *
     * @param node the method or constructor node
     * @param isConstructor true if node is a constructor, false if it is a method
     */
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node);
        for (Parameter parameter : node.getParameters()) {
            visitAnnotations(parameter);
        }
        visitClassCodeContainer(node.getCode());
    }

    /**
     * Visits a {@link FieldNode}, processing its annotations and initial value expression if present.
     *
     * @param node the field node to visit
     */
    @Override
    public void visitField(FieldNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        if (init != null) init.visit(this);
    }

    /**
     * Visits a {@link PropertyNode}, processing its annotations, initial value expression,
     * and getter/setter blocks if present.
     *
     * @param node the property node to visit
     */
    @Override
    public void visitProperty(PropertyNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        if (init != null) init.visit(this);

        visitClassCodeContainer(node.getGetterBlock());
        visitClassCodeContainer(node.getSetterBlock());
    }

    /**
     * Visits a code statement container, traversing it if present.
     *
     * @param code the statement to visit, may be null
     */
    protected void visitClassCodeContainer(Statement code) {
        if (code != null) code.visit(this);
    }

    /**
     * Visits all object initializer statements in a class, typically static or instance initialization blocks.
     *
     * @param node the class node containing initializer statements
     */
    protected void visitObjectInitializerStatements(ClassNode node) {
        for (Statement statement : node.getObjectInitializerStatements()) {
            statement.visit(this);
        }
    }

    /**
     * Visits a {@link ClosureExpression} with annotation processing, traversing parameter annotations
     * before delegating to parent traversal.
     *
     * @param expression the closure expression to visit
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified()) {
            for (Parameter parameter : expression.getParameters()) {
                visitAnnotations(parameter);
            }
        }
        super.visitClosureExpression(expression);
    }

    /**
     * Visits a {@link DeclarationExpression} with annotation processing, traversing expression annotations
     * before delegating to parent traversal.
     *
     * @param expression the declaration expression to visit
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitAnnotations(expression);
        super.visitDeclarationExpression(expression);
    }

    /**
     * Visits an {@link AssertStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the assert statement to visit
     */
    @Override
    public void visitAssertStatement(AssertStatement statement) {
        visitStatement(statement);
        super.visitAssertStatement(statement);
    }

    /**
     * Visits a {@link BlockStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the block statement to visit
     */
    @Override
    public void visitBlockStatement(BlockStatement statement) {
        visitStatement(statement);
        super.visitBlockStatement(statement);
    }

    /**
     * Visits a {@link BreakStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the break statement to visit
     */
    @Override
    public void visitBreakStatement(BreakStatement statement) {
        visitStatement(statement);
        super.visitBreakStatement(statement);
    }

    /**
     * Visits a {@link CaseStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the case statement to visit
     */
    @Override
    public void visitCaseStatement(CaseStatement statement) {
        visitStatement(statement);
        super.visitCaseStatement(statement);
    }

    /**
     * Visits a {@link CatchStatement}, processing variable annotations and invoking the statement hook.
     *
     * @param statement the catch statement to visit
     */
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        visitStatement(statement);
        visitAnnotations(statement.getVariable());
        super.visitCatchStatement(statement);
    }

    /**
     * Visits a {@link ContinueStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the continue statement to visit
     */
    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        visitStatement(statement);
        super.visitContinueStatement(statement);
    }

    /**
     * Visits a {@link DoWhileStatement}, invoking statement hooks before parent traversal.
     *
     * @param statement the do-while statement to visit
     */
    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
        visitStatement(statement);
        visitStatementAnnotations(statement);
        super.visitDoWhileLoop(statement);
    }

    /**
     * Visits an {@link ExpressionStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the expression statement to visit
     */
    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        visitStatement(statement);
        super.visitExpressionStatement(statement);
    }

    /**
     * Visits a {@link ForStatement}, invoking statement hooks and processing loop variable annotations.
     *
     * @param statement the for statement to visit
     */
    @Override
    public void visitForLoop(ForStatement statement) {
        visitStatement(statement);
        visitStatementAnnotations(statement);
        if (statement.getValueVariable() != null) {
            visitAnnotations(statement.getValueVariable());
        }
        super.visitForLoop(statement);
    }

    /**
     * Visits an {@link IfStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the if statement to visit
     */
    @Override
    public void visitIfElse(IfStatement statement) {
        visitStatement(statement);
        super.visitIfElse(statement);
    }

    /**
     * Visits a {@link ReturnStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the return statement to visit
     */
    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        visitStatement(statement);
        super.visitReturnStatement(statement);
    }

    /**
     * Visits a {@link SwitchStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the switch statement to visit
     */
    @Override
    public void visitSwitch(SwitchStatement statement) {
        visitStatement(statement);
        super.visitSwitch(statement);
    }

    /**
     * Visits a {@link SynchronizedStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the synchronized statement to visit
     */
    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        visitStatement(statement);
        super.visitSynchronizedStatement(statement);
    }

    /**
     * Visits a {@link ThrowStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the throw statement to visit
     */
    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        visitStatement(statement);
        super.visitThrowStatement(statement);
    }

    /**
     * Visits a {@link TryCatchStatement}, invoking the statement hook before parent traversal.
     *
     * @param statement the try-catch statement to visit
     */
    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        visitStatement(statement);
        super.visitTryCatchFinally(statement);
    }

    /**
     * Visits a {@link WhileStatement}, invoking statement hooks before parent traversal.
     *
     * @param statement the while statement to visit
     */
    @Override
    public void visitWhileLoop(WhileStatement statement) {
        visitStatement(statement);
        visitStatementAnnotations(statement);
        super.visitWhileLoop(statement);
    }

    /**
     * Hook method called when visiting any {@link Statement}. Subclasses may override
     * to perform common processing on all statements.
     *
     * @param statement the statement being visited
     */
    protected void visitStatement(Statement statement) {
    }

    /**
     * Called for each loop statement ({@code for}, {@code while}, {@code do-while}) that
     * carries statement-level annotations. Subclasses may override to process those annotations.
     *
     * @param statement the loop statement that may have statement-level annotations
     * @since 6.0.0
     */
    protected void visitStatementAnnotations(Statement statement) {
    }

    /**
     * Provides access to the {@link SourceUnit} for error reporting during visitation.
     * Implementations must override this method.
     *
     * @return the source unit for this visitor
     */
    protected abstract SourceUnit getSourceUnit();

    /**
     * Adds an error message associated with an AST node to the source unit.
     * Errors are accumulated and reported after visitation completes.
     *
     * @param error the error message to report
     * @param node the AST node associated with the error location
     * @see ErrorCollecting
     */
    @Override
    public void addError(final String error, final ASTNode node) {
        getSourceUnit().addErrorAndContinue(new SyntaxException(error + '\n', node));
    }
}
