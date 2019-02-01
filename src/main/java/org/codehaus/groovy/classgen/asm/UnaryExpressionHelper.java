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
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * A helper class used to generate bytecode for unary expressions. AST transformations willing to use
 * a custom unary expression helper may set the {@link WriterControllerFactory} node metadata on a
 * class node to provide a custom {@link WriterController} which would in turn use a custom expression
 * helper.
 *
 * @see BinaryExpressionHelper
 */
public class UnaryExpressionHelper {

    // unary plus, unary minus, bitwise negation
    static final MethodCaller unaryPlus = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "unaryPlus");
    static final MethodCaller unaryMinus = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "unaryMinus");
    static final MethodCaller bitwiseNegate = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "bitwiseNegate");

    private final WriterController controller;

    public UnaryExpressionHelper(final WriterController controller) {
        this.controller = controller;
    }

    public void writeUnaryPlus(UnaryPlusExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().box();
        unaryPlus.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
        controller.getAssertionWriter().record(expression);
    }

    public void writeUnaryMinus(UnaryMinusExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().box();
        unaryMinus.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
        controller.getAssertionWriter().record(expression);
    }

    public void writeBitwiseNegate(BitwiseNegationExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().box();
        bitwiseNegate.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.OBJECT_TYPE);
        controller.getAssertionWriter().record(expression);
    }

    public void writeNotExpression(NotExpression expression) {
        Expression subExpression = expression.getExpression();
        int mark = controller.getOperandStack().getStackLength();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().castToBool(mark, true);
        BytecodeHelper.negateBoolean(controller.getMethodVisitor());
        controller.getAssertionWriter().record(expression);
    }
}
