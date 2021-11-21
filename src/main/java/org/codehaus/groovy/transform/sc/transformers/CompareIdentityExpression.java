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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BinaryExpression;
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
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;

/**
 * Compares two objects using identity comparison.
 * This expression will generate bytecode using the IF_ACMPNE instruction, instead of
 * using the "equals" method that is currently mapped to "==" in Groovy.
 *
 * This expression should only be used to compare to objects, not primitives, and only
 * in the context of reference equality check.
 */
public class CompareIdentityExpression extends BinaryExpression {

    public CompareIdentityExpression(final Expression leftExpression, final boolean eq, final Expression rightExpression) {
        super(leftExpression, Token.newSymbol(eq ? "===" : "!==", -1, -1), rightExpression);
        super.setType(ClassHelper.boolean_TYPE);
    }

    public CompareIdentityExpression(final Expression leftExpression, final Expression rightExpression) {
        this(leftExpression, true, rightExpression);
    }

    public boolean isEq() {
        return getOperation().getText().charAt(0) == '=';
    }

    @Override
    public void setType(final ClassNode type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new CompareIdentityExpression(transformer.transform(getLeftExpression()), isEq(), transformer.transform(getRightExpression()));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (!(visitor instanceof AsmClassGenerator)) {
            super.visit(visitor);
            return;
        }

        WriterController controller = ((AsmClassGenerator) visitor).getController();
        MethodVisitor mv = controller.getMethodVisitor();
        Label no = new Label(), yes = new Label();

        getLeftExpression().visit(visitor);
        controller.getOperandStack().box();
        getRightExpression().visit(visitor);
        controller.getOperandStack().box();

        mv.visitJumpInsn(isEq() ? IF_ACMPNE : IF_ACMPEQ, no);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, yes);
        mv.visitLabel(no);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(yes);

        controller.getOperandStack().replace(ClassHelper.boolean_TYPE, 2);
    }
}
