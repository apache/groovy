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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.UnaryExpressionHelper;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;

/**
 * An unary expression helper which generates optimized bytecode depending on
 * the current type on top of the operand stack.
 */
public class StaticTypesUnaryExpressionHelper extends UnaryExpressionHelper implements Opcodes {
    private static final UnaryMinusExpression EMPTY_UNARY_MINUS = new UnaryMinusExpression(EmptyExpression.INSTANCE);
    private static final UnaryPlusExpression EMPTY_UNARY_PLUS = new UnaryPlusExpression(EmptyExpression.INSTANCE);
    private static final BitwiseNegationExpression EMPTY_BITWISE_NEGATE = new BitwiseNegationExpression(EmptyExpression.INSTANCE);

    private final WriterController controller;

    public StaticTypesUnaryExpressionHelper(final WriterController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void writeBitwiseNegate(final BitwiseNegationExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        if (isPrimitiveOnTop()) {
            final ClassNode top = getTopOperand();
            if (top==int_TYPE || top==short_TYPE || top==byte_TYPE || top==char_TYPE || top==long_TYPE) {
                BytecodeExpression bytecodeExpression = new BytecodeExpression() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        if (long_TYPE==top) {
                            mv.visitLdcInsn(-1L);
                            mv.visitInsn(LXOR);
                        } else {
                            mv.visitInsn(ICONST_M1);
                            mv.visitInsn(IXOR);
                            if (byte_TYPE==top) {
                                mv.visitInsn(I2B);
                            } else if (char_TYPE==top) {
                                mv.visitInsn(I2C);
                            } else if (short_TYPE==top) {
                                mv.visitInsn(I2S);
                            }
                        }
                    }
                };
                bytecodeExpression.visit(controller.getAcg());
                controller.getOperandStack().remove(1);
                return;
            }
        }
        super.writeBitwiseNegate(EMPTY_BITWISE_NEGATE);
    }

    @Override
    public void writeNotExpression(final NotExpression expression) {
        TypeChooser typeChooser = controller.getTypeChooser();
        Expression subExpression = expression.getExpression();
        ClassNode classNode = controller.getClassNode();
        if (typeChooser.resolveType(subExpression, classNode) == boolean_TYPE) {
            subExpression.visit(controller.getAcg());
            controller.getOperandStack().doGroovyCast(boolean_TYPE);
            BytecodeExpression bytecodeExpression = new BytecodeExpression() {
                @Override
                public void visit(final MethodVisitor mv) {
                    Label ne = new Label();
                    mv.visitJumpInsn(IFNE, ne);
                    mv.visitInsn(ICONST_1);
                    Label out = new Label();
                    mv.visitJumpInsn(GOTO, out);
                    mv.visitLabel(ne);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(out);
                }
            };
            bytecodeExpression.visit(controller.getAcg());
            controller.getOperandStack().remove(1);
            return;
        }
        super.writeNotExpression(expression);
    }

    @Override
    public void writeUnaryMinus(final UnaryMinusExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        if (isPrimitiveOnTop()) {
            final ClassNode top = getTopOperand();
            if (top!=boolean_TYPE) {
                BytecodeExpression bytecodeExpression = new BytecodeExpression() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        if (int_TYPE == top || short_TYPE == top || byte_TYPE==top || char_TYPE==top) {
                            mv.visitInsn(INEG);
                            if (byte_TYPE==top) {
                                mv.visitInsn(I2B);
                            } else if (char_TYPE==top) {
                                mv.visitInsn(I2C);
                            } else if (short_TYPE==top) {
                                mv.visitInsn(I2S);
                            }
                        } else if (long_TYPE == top) {
                            mv.visitInsn(LNEG);
                        } else if (float_TYPE == top) {
                            mv.visitInsn(FNEG);
                        } else if (double_TYPE == top) {
                            mv.visitInsn(DNEG);
                        }
                    }
                };
                bytecodeExpression.visit(controller.getAcg());
                controller.getOperandStack().remove(1);
                return;
            }
        }
        // we already visited the sub expression
        super.writeUnaryMinus(EMPTY_UNARY_MINUS);
    }

    @Override
    public void writeUnaryPlus(final UnaryPlusExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        if (isPrimitiveOnTop()) {
            // only visit the expression
            return;
        }
        super.writeUnaryPlus(EMPTY_UNARY_PLUS);
    }

    private boolean isPrimitiveOnTop() {
        return isPrimitiveType(getTopOperand());
    }

    private ClassNode getTopOperand() {
        return controller.getOperandStack().getTopOperand();
    }
}
