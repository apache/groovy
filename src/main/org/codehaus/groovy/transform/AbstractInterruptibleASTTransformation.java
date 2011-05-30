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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for AST Transformations which will automatically throw an {@link InterruptedException} when
 * some conditions are met.
 *
 * @author Cedric Champeau
 * @author Hamlet D'Arcy
 * @since 1.8.0
 */
public abstract class AbstractInterruptibleASTTransformation extends ClassCodeVisitorSupport implements ASTTransformation, Opcodes {

    protected static final String CHECK_METHOD_START_MEMBER = "checkOnMethodStart";
    protected static final String PROPAGATE_TO_COMPILE_UNIT = "applyToAllClasses";
    protected static final String THROWN_EXCEPTION_TYPE = "thrown";
    protected SourceUnit source;
    protected boolean checkOnMethodStart;
    protected boolean applyToAllClasses;
    protected ClassNode thrownExceptionType;

    protected SourceUnit getSourceUnit() {
        return source;
    }

    protected abstract ClassNode type();

    /**
     * Subclasses should implement this method to set the condition of the interruption statement
     */
    protected abstract Expression createCondition();

    /**
     * Subclasses should implement this method to provide good error resolution. 
     */
    protected abstract String getErrorMessage();

    protected void setupTransform(AnnotationNode node) {
        checkOnMethodStart = getBooleanAnnotationParameter(node, CHECK_METHOD_START_MEMBER, true);
        applyToAllClasses = getBooleanAnnotationParameter(node, PROPAGATE_TO_COMPILE_UNIT, true);
        thrownExceptionType = getClassAnnotationParameter(node, THROWN_EXCEPTION_TYPE, ClassHelper.make(InterruptedException.class));
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            internalError("Expecting [AnnotationNode, AnnotatedClass] but got: " + Arrays.asList(nodes));
        }

        this.source = source;
        AnnotationNode node = (AnnotationNode) nodes[0];
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];

        if (!type().equals(node.getClassNode())) {
            internalError("Transformation called from wrong annotation: " + node.getClassNode().getName());
        }

        setupTransform(node);

        // should be limited to the current SourceUnit or propagated to the whole CompilationUnit
        final ModuleNode tree = source.getAST();
        if (applyToAllClasses) {
            // guard every class and method defined in this script
            if (tree != null) {
                final List<ClassNode> classes = tree.getClasses();
                for (ClassNode classNode : classes) {
                    visitClass(classNode);
                }
            }
        } else if (annotatedNode instanceof ClassNode) {
            // only guard this particular class
            this.visitClass((ClassNode) annotatedNode);
        } else {
            // only guard the script class
            if (tree != null) {
                final List<ClassNode> classes = tree.getClasses();
                for (ClassNode classNode : classes) {
                    if (classNode.isScript()) {
                        visitClass(classNode);
                    }
                }
            }
        }
    }


    protected static boolean getBooleanAnnotationParameter(AnnotationNode node, String parameterName, boolean defaultValue) {
        Expression member = node.getMember(parameterName);
        if (member != null) {
            if (member instanceof ConstantExpression) {
                try {
                    return DefaultGroovyMethods.asType(((ConstantExpression) member).getValue(), Boolean.class);
                } catch (Exception e) {
                    internalError("Expecting boolean value for " + parameterName + " annotation parameter. Found " + member + "member");
                }
            } else {
                internalError("Expecting boolean value for " + parameterName + " annotation parameter. Found " + member + "member");
            }
        }
        return defaultValue;
    }

    protected static ClassNode getClassAnnotationParameter(AnnotationNode node, String parameterName, ClassNode defaultValue) {
        Expression member = node.getMember(parameterName);
        if (member != null) {
            if (member instanceof ClassExpression) {
                try {
                    return member.getType();
                } catch (Exception e) {
                    internalError("Expecting class value for " + parameterName + " annotation parameter. Found " + member + "member");
                }
            } else {
                internalError("Expecting class value for " + parameterName + " annotation parameter. Found " + member + "member");
            }
        }
        return defaultValue;
    }

    protected static void internalError(String message) {
        throw new GroovyBugError("Internal error: " + message);
    }

    /**
     * @return Returns the interruption check statement.
     */
    protected Statement createInterruptStatement() {
        return new IfStatement(
                new BooleanExpression(
                        createCondition()
                ),
                new ThrowStatement(
                        new ConstructorCallExpression(thrownExceptionType,
                                new ArgumentListExpression(new ConstantExpression(getErrorMessage())))
                ),
                new EmptyStatement()
        );
    }

    /**
     * Takes a statement and wraps it into a block statement which first element is the interruption check statement.
     *
     * @param statement the statement to be wrapped
     * @return a {@link BlockStatement block statement}   which first element is for checking interruption, and the
     *         second one the statement to be wrapped.
     */
    protected final Statement wrapBlock(Statement statement) {
        BlockStatement stmt = new BlockStatement();
        stmt.addStatement(createInterruptStatement());
        stmt.addStatement(statement);
        return stmt;
    }

    @Override
    public final void visitForLoop(ForStatement forStatement) {
        visitLoop(forStatement);
        super.visitForLoop(forStatement); 
    }

    /**
     * Shortcut method which avoids duplicating code for every type of loop.
     * Actually wraps the loopBlock of different types of loop statements.
     */
    private void visitLoop(LoopingStatement loopStatement) {
        Statement statement = loopStatement.getLoopBlock();
        loopStatement.setLoopBlock(wrapBlock(statement));
    }

    @Override
    public final void visitDoWhileLoop(DoWhileStatement doWhileStatement) {
        visitLoop(doWhileStatement); 
        super.visitDoWhileLoop(doWhileStatement);
    }

    @Override
    public final void visitWhileLoop(WhileStatement whileStatement) {
        visitLoop(whileStatement);
        super.visitWhileLoop(whileStatement);
    }
}
