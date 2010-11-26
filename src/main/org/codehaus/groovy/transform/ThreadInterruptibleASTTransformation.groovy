/*
 * Copyright 2008-2010 the original author or authors.
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
package org.codehaus.groovy.transform;


import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.*
import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression

/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ThreadInterrupt
 * 
 * @author Cédric Champeau
 * @author Hamlet D'Arcy
 *
 * @since 1.8.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ThreadInterruptibleASTTransformation extends ClassCodeVisitorSupport implements ASTTransformation {

    private static final ClassNode MY_TYPE = new ClassNode(ThreadInterrupt.class)
    private static final String CHECK_METHOD_START_MEMBER = 'checkOnMethodStart'
    private static final String PROPAGATE_TO_COMPILE_UNIT = 'applyToAllClasses'
    private final static def INTERRUPT_STATEMENT = createInterruptStatement()
    private SourceUnit source
    private boolean checkOnMethodStart
    private boolean applyToAllClasses  

    public ThreadInterruptibleASTTransformation() {

    }
    
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            internalError("Expecting [AnnotationNode, AnnotatedClass] but got: " + Arrays.asList(nodes))
        }

        this.source = source
        AnnotationNode node = nodes[0]
        AnnotatedNode annotatedNode = nodes[1]

        if (!MY_TYPE.equals(node.getClassNode())) {
            internalError("Transformation called from wrong annotation: " + node.getClassNode().getName())
        }

        checkOnMethodStart = getBooleanAnnotationParameter(node, CHECK_METHOD_START_MEMBER, true)
        applyToAllClasses  = getBooleanAnnotationParameter(node, PROPAGATE_TO_COMPILE_UNIT, true)

        // should be limited to the current SourceUnit or propagated to the whole CompilationUnit
        if (applyToAllClasses) {
            // guard every class and method defined in this script
            source.getAST()?.classes?.each { ClassNode it ->
                this.visitClass(it)
            }
        } else if (annotatedNode instanceof ClassNode) {
            // only guard this particular class
            this.visitClass annotatedNode
        } else {
            // only guard the script class
            source.getAST()?.classes?.each { ClassNode it ->
                if (it.isScript()) {
                    this.visitClass(it)
                }
            }
        }
    }

    public static boolean getBooleanAnnotationParameter(AnnotationNode node, String parameterName, boolean defaultValue) {
        def member = node.getMember(parameterName)
        if (member) {
            if (member instanceof ConstantExpression) {
                try {
                    return Boolean.valueOf(member.value)
                } catch (e) {
                    internalError("Expecting boolean value for ${parameterName} annotation parameter. Found $member")
                }
            } else {
                internalError("Expecting boolean value for ${parameterName} annotation parameter. Found $member")
            }
        }
        return defaultValue
    }

    private static void internalError(String message) {
        throw new RuntimeException("Internal error: " + message)
    }

    /**
     * @return Returns the interruption check statement.
     */
    final static def createInterruptStatement() {
        new IfStatement(
                new BooleanExpression(
                        new MethodCallExpression(
                                new StaticMethodCallExpression(ClassHelper.make(Thread),
                                        'currentThread',
                                        ArgumentListExpression.EMPTY_ARGUMENTS),
                                'isInterrupted', ArgumentListExpression.EMPTY_ARGUMENTS)
                ),
                new ThrowStatement(
                        new ConstructorCallExpression(ClassHelper.make(InterruptedException),
                        new ArgumentListExpression(new ConstantExpression("Execution Interrupted")))
                ),
                new EmptyStatement()
        )
    }


    /**
     * Takes a statement and wraps it into a block statement which first element is the interruption check statement.
     * @param statement the statement to be wrapped
     * @return a {@link BlockStatement block statement}   which first element is for checking interruption, and the
     * second one the statement to be wrapped.
     */
    static private def wrapBlock(statement) {
        def stmt = new BlockStatement();
        stmt.addStatement(INTERRUPT_STATEMENT);
        stmt.addStatement(statement);
        stmt
    }
    @Override
    public void visitClosureExpression(ClosureExpression closureExpr) {
        def code = closureExpr.code
        closureExpr.code = wrapBlock(code)
        super.visitClosureExpression closureExpr
    }

    /**
     * Shortcut method which avoids duplicating code for every type of loop.
     * Actually wraps the loopBlock of different types of loop statements.
     */
    private def visitLoop(loopStatement) {
        def statement = loopStatement.loopBlock
        loopStatement.loopBlock = wrapBlock(statement)
    }

    @Override
    public void visitForLoop(ForStatement forStatement) {
        visitLoop(forStatement)
        super.visitForLoop(forStatement)
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement doWhileStatement) {
        visitLoop(doWhileStatement)
        super.visitDoWhileLoop(doWhileStatement)
    }

    @Override
    public void visitWhileLoop(final WhileStatement whileStatement) {
        visitLoop(whileStatement)
        super.visitWhileLoop(whileStatement)
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (checkOnMethodStart && !node.isSynthetic()) {
            def code = node.code
            node.code = wrapBlock(code);
        }
        super.visitMethod(node)
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}
