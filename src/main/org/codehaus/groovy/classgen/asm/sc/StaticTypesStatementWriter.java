/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.classgen.asm.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * A class to write out the optimized statements
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class StaticTypesStatementWriter extends StatementWriter {

    private static final ClassNode ITERABLE_CLASSNODE = ClassHelper.make(Iterable.class);
    private StaticTypesWriterController controller;

    public StaticTypesStatementWriter(StaticTypesWriterController controller) {
        super(controller);
        this.controller = controller;
    }
    
    @Override
    public void writeBlockStatement(BlockStatement statement) {
        controller.switchToFastPath();
        super.writeBlockStatement(statement);
        controller.switchToSlowPath();
    }

    @Override
    protected void writeForInLoop(final ForStatement loop) {
        controller.getAcg().onLineNumber(loop,"visitForLoop");
        writeStatementLabel(loop);

        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        compileStack.pushLoop(loop.getVariableScope(), loop.getStatementLabel());

        // Declare the loop counter.
        BytecodeVariable variable = compileStack.defineVariable(loop.getVariable(), false);

        // Identify type of collection
        TypeChooser typeChooser = controller.getTypeChooser();
        Expression collectionExpression = loop.getCollectionExpression();
        ClassNode collectionType = typeChooser.resolveType(collectionExpression, controller.getClassNode());

        if (collectionType.implementsInterface(ITERABLE_CLASSNODE)) {
            MethodCallExpression iterator = new MethodCallExpression(collectionExpression, "iterator", new ArgumentListExpression());
            iterator.setMethodTarget(collectionType.getMethod("iterator", Parameter.EMPTY_ARRAY));
            iterator.setImplicitThis(false);
            iterator.visit(controller.getAcg());
        } else {
            collectionExpression.visit(controller.getAcg());
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    "org/codehaus/groovy/runtime/DefaultGroovyMethods",
                    "iterator",
                    "(Ljava/lang/Object;)Ljava/util/Iterator;"
            );
        }

        // Then get the iterator and generate the loop control

        final int iteratorIdx = compileStack.defineTemporaryVariable("iterator", ClassHelper.Iterator_TYPE, true);

        Label continueLabel = compileStack.getContinueLabel();
        Label breakLabel = compileStack.getBreakLabel();

        mv.visitLabel(continueLabel);
        mv.visitVarInsn(ALOAD, iteratorIdx);
        writeIteratorHasNext(mv);
        // note: ifeq tests for ==0, a boolean is 0 if it is false
        mv.visitJumpInsn(IFEQ, breakLabel);

        mv.visitVarInsn(ALOAD, iteratorIdx);
        writeIteratorNext(mv);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        operandStack.storeVar(variable);

        // Generate the loop body
        loop.getLoopBlock().visit(controller.getAcg());

        mv.visitJumpInsn(GOTO, continueLabel);
        mv.visitLabel(breakLabel);

        compileStack.pop();
    }
}
