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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.codehaus.groovy.runtime.powerassert.SourceTextNotAvailableException;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;

public class AssertionWriter {

    private static class AssertionTracker {
        int recorderIndex;
        SourceText sourceText;
    }

    private final WriterController controller;
    private AssertionTracker assertionTracker;
    private AssertionTracker disabledTracker;

    public AssertionWriter(final WriterController wc) {
        this.controller = wc;
    }

    public void writeAssertStatement(final AssertStatement statement) {
        AssertionTracker oldTracker = assertionTracker;
        assertionTracker = null;
        try {
            MethodVisitor mv = controller.getMethodVisitor();
            OperandStack operandStack = controller.getOperandStack();
            CompileStack compileStack = controller.getCompileStack();

            SourceText sourceText;
            // don't rewrite assertions with message or no source
            if (!isNullConstant(statement.getMessageExpression())
                    || (sourceText = getSourceText(statement)) == null) {
                // test the condition
                statement.getBooleanExpression().visit(controller.getAcg());
                Label falseBranch = operandStack.jump(IFEQ);
                Label afterAssert = new Label();
                mv.visitJumpInsn(GOTO, afterAssert);

                mv.visitLabel(falseBranch);
                writeSourcelessAssertText(statement);
                operandStack.push(ClassHelper.STRING_TYPE);
                statement.getMessageExpression().visit(controller.getAcg());
                operandStack.box();
                throwAssertError();

                mv.visitLabel(afterAssert);
                return;
            }

            assertionTracker = new AssertionTracker();
            assertionTracker.sourceText = sourceText;

            mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/powerassert/ValueRecorder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "<init>", "()V", false);
            operandStack.push(ClassHelper.OBJECT_TYPE); // TODO: maybe use more specialized type here
            assertionTracker.recorderIndex = compileStack.defineTemporaryVariable("recorder", true);
            Label tryStart = new Label(), tryEnd = new Label();
            mv.visitLabel(tryStart);

            // test the condition
            statement.getBooleanExpression().visit(controller.getAcg());
            Label falseBranch = operandStack.jump(IFEQ);

            // clear the value recorder and proceed normally
            mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "clear", "()V", false);
            Label afterAssert = new Label();
            mv.visitJumpInsn(GOTO, afterAssert);

            mv.visitLabel(falseBranch);
            // load the (power assert) expression text
            mv.visitLdcInsn(assertionTracker.sourceText.getNormalizedText());
            mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/powerassert/AssertionRenderer", "render", "(Ljava/lang/String;Lorg/codehaus/groovy/runtime/powerassert/ValueRecorder;)Ljava/lang/String;", false);
            operandStack.push(ClassHelper.STRING_TYPE);

            int recorderIndex = assertionTracker.recorderIndex;
            assertionTracker = null; // deactivate AssertionWriter#record calls

            // load the optional message expression
            statement.getMessageExpression().visit(controller.getAcg());
            operandStack.box();
            throwAssertError();

            mv.visitLabel(tryEnd);

            // catch-all block to clear value recorder
            Label catchAll = new Label();
            mv.visitLabel(catchAll);
            mv.visitVarInsn(ALOAD, recorderIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "clear", "()V", false);
            mv.visitInsn(ATHROW); // re-throw
            compileStack.addExceptionBlock(tryStart, tryEnd, catchAll, null);

            mv.visitLabel(afterAssert);

            compileStack.removeVar(recorderIndex);
        } finally {
            assertionTracker = oldTracker;
        }
    }

    private SourceText getSourceText(final AssertStatement statement) {
        Janitor janitor = new Janitor();
        try {
            // because source position seems to be more reliable for statements
            // than for expressions, get the source text for the whole statement
            return new SourceText(statement, controller.getSourceUnit(), janitor);
        } catch (SourceTextNotAvailableException e) {
            return null;
        } finally {
            // close open file handles from getting a sample for power asserts
            janitor.cleanup();
        }
    }

    private void writeSourcelessAssertText(final AssertStatement statement) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        BooleanExpression booleanExpression = statement.getBooleanExpression();
        String expressionText = booleanExpression.getText();
        // push expression string onto stack
        List<String> names = new ArrayList<>();
        addVariableNames(booleanExpression, names);
        if (names.isEmpty()) {
            mv.visitLdcInsn(expressionText);
        } else {
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(expressionText + ". Values: ");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            operandStack.push(ClassHelper.OBJECT_TYPE); // TODO: maybe use more special type StringBuilder here
            int tempIndex = compileStack.defineTemporaryVariable("assert", true);

            boolean first = true;
            for (String name : names) {
                String text = name + " = ";
                if (first) {
                    first = false;
                } else {
                    text = ", " + text;
                }

                mv.visitVarInsn(ALOAD, tempIndex);
                mv.visitLdcInsn(text);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, tempIndex);
                new VariableExpression(name).visit(controller.getAcg());
                operandStack.box();
                mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "toString", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitInsn(POP);
                operandStack.remove(1);
            }
            mv.visitVarInsn(ALOAD, tempIndex);
            compileStack.removeVar(tempIndex);
        }
    }

    private static void addVariableNames(final Expression expression, final List<String> list) {
        if (expression instanceof BooleanExpression) {
            BooleanExpression boolExp = (BooleanExpression) expression;
            addVariableNames(boolExp.getExpression(), list);
        } else if (expression instanceof BinaryExpression) {
            BinaryExpression binExp = (BinaryExpression) expression;
            addVariableNames(binExp.getLeftExpression(), list);
            addVariableNames(binExp.getRightExpression(), list);
        } else if (expression instanceof VariableExpression) {
            VariableExpression varExp = (VariableExpression) expression;
            list.add(varExp.getName());
        }
    }

    private void throwAssertError() {
        // GROOVY-10878: call method that returns throwable, then throw it from here for better coverage metrics
        controller.getMethodVisitor().visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper",
                "createAssertError", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/AssertionError;", false);
        controller.getMethodVisitor().visitInsn(ATHROW); // throw AssertionError
        controller.getOperandStack().remove(2); // two call arguments
    }

    //--------------------------------------------------------------------------

    public void disableTracker() {
        if (assertionTracker != null) {
            disabledTracker = assertionTracker;
            assertionTracker = null;
        }
    }

    public void reenableTracker() {
        if (disabledTracker != null) {
            assertionTracker = disabledTracker;
            disabledTracker = null;
        }
    }

    public void record(final Token token) {
        if (assertionTracker != null)
            record(assertionTracker.sourceText.getNormalizedColumn(token.getStartLine(), token.getStartColumn()));
    }

    public void record(final Expression expression) {
        if (assertionTracker != null)
            record(assertionTracker.sourceText.getNormalizedColumn(expression.getLineNumber(), expression.getColumnNumber()));
    }

    private void record(final int normalizedColumn) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        operandStack.dup();
        operandStack.box();

        mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        operandStack.swap();
        mv.visitLdcInsn(normalizedColumn);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "record", "(Ljava/lang/Object;I)Ljava/lang/Object;", false);
        mv.visitInsn(POP);
        operandStack.remove(2);
    }
}
