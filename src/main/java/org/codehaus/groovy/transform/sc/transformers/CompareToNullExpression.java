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
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CompareToNullExpression extends BinaryExpression implements Opcodes {
    private final boolean equalsNull;

    public CompareToNullExpression(final Expression objectExpression, final boolean compareToNull) {
        super(objectExpression, new Token(Types.COMPARE_TO, compareToNull ? "==" : "!=", -1, -1), ConstantExpression.NULL);
        super.setType(ClassHelper.boolean_TYPE);
        this.equalsNull = compareToNull;
    }

    public Expression getObjectExpression() {
        return getLeftExpression();
    }

    @Override
    public void setLeftExpression(final Expression expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRightExpression(final Expression expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setType(final org.codehaus.groovy.ast.ClassNode type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new CompareToNullExpression(transformer.transform(getObjectExpression()), equalsNull);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (visitor instanceof AsmClassGenerator) {
            AsmClassGenerator acg = (AsmClassGenerator) visitor;
            WriterController controller = acg.getController();
            MethodVisitor mv = controller.getMethodVisitor();

            getObjectExpression().visit(acg);

            if (ClassHelper.isPrimitiveType(controller.getOperandStack().getTopOperand())) {
                controller.getOperandStack().pop();
                mv.visitInsn(equalsNull ? ICONST_0 : ICONST_1);

                controller.getOperandStack().push(ClassHelper.boolean_TYPE);
            } else {
                Label zero = new Label();
                mv.visitJumpInsn(equalsNull ? IFNONNULL : IFNULL, zero);
                mv.visitInsn(ICONST_1);
                Label end = new Label();
                mv.visitJumpInsn(GOTO, end);
                mv.visitLabel(zero);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(end);

                controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
            }
        } else {
            super.visit(visitor);
        }
    }
}
