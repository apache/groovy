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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;

/**
 * Specializes a comparison against {@code null} for static compilation.
 */
public class CompareToNullExpression extends BinaryExpression {
    private final boolean equalsNull;

    /**
     * Creates a comparison between an expression and {@code null}.
     *
     * @param expression the expression being compared
     * @param equalsNull {@code true} for {@code == null}; {@code false} for {@code != null}
     */
    public CompareToNullExpression(final Expression expression, final boolean equalsNull) {
        super(expression, Token.newSymbol(equalsNull ? "==" : "!=", -1, -1), ConstantExpression.NULL);
        super.setType(ClassHelper.boolean_TYPE);
        this.equalsNull = equalsNull;
    }

    /**
     * Returns the expression being compared with {@code null}.
     *
     * @return the compared expression
     */
    public Expression getObjectExpression() {
        return getLeftExpression();
    }

    /**
     * Prevents replacing the compared expression after construction.
     *
     * @param expression ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setLeftExpression(final Expression expression) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prevents changing the fixed {@code null} right-hand operand.
     *
     * @param expression ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setRightExpression(final Expression expression) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prevents changing the fixed boolean result type of this expression.
     *
     * @param type ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setType(final org.codehaus.groovy.ast.ClassNode type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Transforms the compared expression while preserving the {@code null}-comparison semantics.
     *
     * @param transformer the expression transformer to apply
     * @return a transformed {@code null}-comparison expression
     */
    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new CompareToNullExpression(transformer.transform(getObjectExpression()), equalsNull);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    /**
     * Emits direct {@code null}-comparison bytecode for this expression.
     *
     * @param visitor the visitor to accept
     */
    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (!(visitor instanceof AsmClassGenerator)) {
            super.visit(visitor);
            return;
        }

        WriterController controller = ((AsmClassGenerator) visitor).getController();
        MethodVisitor mv = controller.getMethodVisitor();

        getObjectExpression().visit(visitor);

        if (ClassHelper.isPrimitiveType(controller.getOperandStack().getTopOperand())) {
            controller.getOperandStack().pop();
            mv.visitInsn(equalsNull ? ICONST_0 : ICONST_1);
            controller.getOperandStack().push(ClassHelper.boolean_TYPE);
        } else {
            Label no = new Label(), yes = new Label();

            mv.visitJumpInsn(equalsNull ? IFNONNULL : IFNULL, no);
            mv.visitInsn(ICONST_1);
            mv.visitJumpInsn(GOTO, yes);
            mv.visitLabel(no);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(yes);

            controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
        }
    }
}
