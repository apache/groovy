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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.codehaus.groovy.runtime.powerassert.SourceTextNotAvailableException;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

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
    // assert
    private static final MethodCaller assertFailedMethod = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "assertFailed");
    
    private static class AssertionTracker {
        int recorderIndex;
        SourceText sourceText;
    }

    private final WriterController controller;
    private AssertionTracker assertionTracker;
    private AssertionTracker disabledTracker;
    
    public AssertionWriter(WriterController wc) {
        this.controller = wc;
    }
    
    public void writeAssertStatement(AssertStatement statement) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        
        boolean rewriteAssert = true;
        // don't rewrite assertions with message
        rewriteAssert = statement.getMessageExpression() == ConstantExpression.NULL;
        AssertionTracker oldTracker = assertionTracker;
        Janitor janitor = new Janitor();
        final Label tryStart = new Label();
        if (rewriteAssert){
            assertionTracker = new AssertionTracker();
            try {
                // because source position seems to be more reliable for statements
                // than for expressions, we get the source text for the whole statement
                assertionTracker.sourceText = new SourceText(statement, controller.getSourceUnit(), janitor);
                mv.visitTypeInsn(NEW, "org/codehaus/groovy/runtime/powerassert/ValueRecorder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "<init>", "()V", false);
                //TODO: maybe use more specialized type here
                controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
                assertionTracker.recorderIndex = controller.getCompileStack().defineTemporaryVariable("recorder", true);
                mv.visitLabel(tryStart);
            } catch (SourceTextNotAvailableException e) {
                // set assertionTracker to null to deactivate AssertionWriter#record calls
                assertionTracker = null;
                // don't rewrite assertions w/o source text
                rewriteAssert = false;
            }
        }
        
        statement.getBooleanExpression().visit(controller.getAcg());

        Label exceptionThrower = operandStack.jump(IFEQ);

        // do nothing, but clear the value recorder
        if (rewriteAssert) {
            //clean up assertion recorder
            mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "clear", "()V", false);
        }
        Label afterAssert = new Label();
        mv.visitJumpInsn(GOTO, afterAssert);
        mv.visitLabel(exceptionThrower);
        
        if (rewriteAssert) {
            mv.visitLdcInsn(assertionTracker.sourceText.getNormalizedText());
            mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/powerassert/AssertionRenderer", "render", "(Ljava/lang/String;Lorg/codehaus/groovy/runtime/powerassert/ValueRecorder;)Ljava/lang/String;", false);
        } else {
            writeSourcelessAssertText(statement);
        }
        operandStack.push(ClassHelper.STRING_TYPE);
        AssertionTracker savedTracker = assertionTracker;
        assertionTracker = null;
        
        // now the optional exception expression
        statement.getMessageExpression().visit(controller.getAcg());
        operandStack.box();
        assertFailedMethod.call(mv);
        operandStack.remove(2); // assertFailed called static with 2 arguments 
        
        if (rewriteAssert) {
            final Label tryEnd = new Label();
            mv.visitLabel(tryEnd);
            mv.visitJumpInsn(GOTO, afterAssert);
            // finally block to clean assertion recorder
            final Label catchAny = new Label();
            mv.visitLabel(catchAny);
            mv.visitVarInsn(ALOAD, savedTracker.recorderIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "clear", "()V", false);
            mv.visitInsn(ATHROW);
            // add catch any block to exception table
            controller.getCompileStack().addExceptionBlock(tryStart, tryEnd, catchAny, null);
        }
        
        mv.visitLabel(afterAssert);
        if (rewriteAssert) {
            controller.getCompileStack().removeVar(savedTracker.recorderIndex);
        }
        assertionTracker = oldTracker;
        // close possibly open file handles from getting a sample for 
        // power asserts
        janitor.cleanup();
    }
    
    private void writeSourcelessAssertText(AssertStatement statement) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        
        BooleanExpression booleanExpression = statement.getBooleanExpression();
        // push expression string onto stack
        String expressionText = booleanExpression.getText();
        List<String> list = new ArrayList<>();
        addVariableNames(booleanExpression, list);
        if (list.isEmpty()) {
            mv.visitLdcInsn(expressionText);
        } else {
            boolean first = true;

            // let's create a new expression
            mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(expressionText + ". Values: ");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V", false);
            //TODO: maybe use more special type StringBuffer here
            operandStack.push(ClassHelper.OBJECT_TYPE);
            int tempIndex = controller.getCompileStack().defineTemporaryVariable("assert", true);

            for (String name : list) {
                String text = name + " = ";
                if (first) {
                    first = false;
                } else {
                    text = ", " + text;
                }

                mv.visitVarInsn(ALOAD, tempIndex);
                mv.visitLdcInsn(text);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuffer;", false);
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, tempIndex);
                new VariableExpression(name).visit(controller.getAcg());
                operandStack.box();
                mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "toString", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;", false);
                mv.visitInsn(POP);
                operandStack.remove(1);
            }
            mv.visitVarInsn(ALOAD, tempIndex);
            controller.getCompileStack().removeVar(tempIndex);
        }
    }
    
    public void record(Expression expression) {
        if (assertionTracker==null) return;
        record(assertionTracker.sourceText.getNormalizedColumn(expression.getLineNumber(), expression.getColumnNumber()));
    }

    public void record(Token op) {
        if (assertionTracker==null) return;
        record(assertionTracker.sourceText.getNormalizedColumn(op.getStartLine(), op.getStartColumn()));
    }
    
    private void record(int normalizedColumn) {
        if (assertionTracker==null) return;
        
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        
        operandStack.dup();
        operandStack.box();
        
        mv.visitVarInsn(ALOAD, assertionTracker.recorderIndex);
        operandStack.push(ClassHelper.OBJECT_TYPE);
        //helper.swapWithObject(ClassHelper.OBJECT_TYPE);
        operandStack.swap();
        mv.visitLdcInsn(normalizedColumn);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/powerassert/ValueRecorder", "record", "(Ljava/lang/Object;I)Ljava/lang/Object;", false);
        mv.visitInsn(POP);
        operandStack.remove(2);
    }
    
    private void addVariableNames(Expression expression, List<String> list) {
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

    public void disableTracker() {
        if (assertionTracker==null) return;
        disabledTracker = assertionTracker;
        assertionTracker = null;
    }

    public void reenableTracker() {
        if (disabledTracker==null) return;
        assertionTracker = disabledTracker;
        disabledTracker = null;
    }

}
