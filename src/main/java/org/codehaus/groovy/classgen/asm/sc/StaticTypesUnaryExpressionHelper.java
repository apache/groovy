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
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.UnaryExpressionHelper;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;

/**
 * An expression helper which generates optimized bytecode depending on the
 * current type on top of the operand stack.
 */
public class StaticTypesUnaryExpressionHelper extends UnaryExpressionHelper implements Opcodes {

    private static final BitwiseNegationExpression EMPTY_BITWISE_NEGATE = new BitwiseNegationExpression(EmptyExpression.INSTANCE);
    private static final UnaryMinusExpression EMPTY_UNARY_MINUS = new UnaryMinusExpression(EmptyExpression.INSTANCE);
    private static final UnaryPlusExpression EMPTY_UNARY_PLUS = new UnaryPlusExpression(EmptyExpression.INSTANCE);

    public StaticTypesUnaryExpressionHelper(final WriterController controller) {
        super(controller);
    }

    @Override
    public void writeBitwiseNegate(final BitwiseNegationExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        ClassNode top = controller.getOperandStack().getTopOperand();
        if (top == int_TYPE || top == long_TYPE || top == short_TYPE || top == byte_TYPE || top == char_TYPE) {
            bytecodeX(mv -> {
                if (top == long_TYPE) {
                    mv.visitLdcInsn(-1L);
                    mv.visitInsn(LXOR);
                } else {
                    mv.visitInsn(ICONST_M1);
                    mv.visitInsn(IXOR);
                    if (top == byte_TYPE) {
                        mv.visitInsn(I2B);
                    } else if (top == char_TYPE) {
                        mv.visitInsn(I2C);
                    } else if (top == short_TYPE) {
                        mv.visitInsn(I2S);
                    }
                }
            }).visit(controller.getAcg());
            controller.getOperandStack().remove(1);
        } else {
            super.writeBitwiseNegate(EMPTY_BITWISE_NEGATE);
        }
    }

    @Override
    public void writeNotExpression(final NotExpression expression) {
        Expression subExpression = expression.getExpression();
        TypeChooser typeChooser = controller.getTypeChooser();
        if (typeChooser.resolveType(subExpression, controller.getClassNode()) == boolean_TYPE) {
            subExpression.visit(controller.getAcg());
            controller.getOperandStack().doGroovyCast(boolean_TYPE);
            bytecodeX(mv -> {
                Label ne = new Label();
                mv.visitJumpInsn(IFNE, ne);
                mv.visitInsn(ICONST_1);
                Label out = new Label();
                mv.visitJumpInsn(GOTO, out);
                mv.visitLabel(ne);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(out);
            }).visit(controller.getAcg());
            controller.getOperandStack().remove(1);
        } else {
            super.writeNotExpression(expression);
        }
    }

    @Override
    public void writeUnaryMinus(final UnaryMinusExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        ClassNode top = controller.getOperandStack().getTopOperand();
        if (top == int_TYPE || top == long_TYPE || top == short_TYPE || top == float_TYPE || top == double_TYPE || top == byte_TYPE || top == char_TYPE) {
            bytecodeX(mv -> {
                if (top == int_TYPE || top == short_TYPE || top == byte_TYPE || top == char_TYPE) {
                    mv.visitInsn(INEG);
                    if (top == byte_TYPE) {
                        mv.visitInsn(I2B);
                    } else if (top == char_TYPE) {
                        mv.visitInsn(I2C);
                    } else if (top == short_TYPE) {
                        mv.visitInsn(I2S);
                    }
                } else if (top == long_TYPE) {
                    mv.visitInsn(LNEG);
                } else if (top == float_TYPE) {
                    mv.visitInsn(FNEG);
                } else if (top == double_TYPE) {
                    mv.visitInsn(DNEG);
                }
            }).visit(controller.getAcg());
            controller.getOperandStack().remove(1);
        } else {
            super.writeUnaryMinus(EMPTY_UNARY_MINUS);
        }
    }

    @Override
    public void writeUnaryPlus(final UnaryPlusExpression expression) {
        expression.getExpression().visit(controller.getAcg());
        ClassNode top = controller.getOperandStack().getTopOperand();
        if (top == int_TYPE || top == long_TYPE || top == short_TYPE || top == float_TYPE || top == double_TYPE || top == byte_TYPE || top == char_TYPE) {
            // only visit the sub-expression
        } else {
            super.writeUnaryPlus(EMPTY_UNARY_PLUS);
        }
    }
}
