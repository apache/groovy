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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * A helper class used to generate bytecode for method pointer expressions.
 * @since 3.0.0
 */
public class MethodPointerExpressionWriter {
    // Closure
    static final MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");

    private final WriterController controller;

    public MethodPointerExpressionWriter(final WriterController controller) {
        this.controller = controller;
    }

    public void writeMethodPointerExpression(MethodPointerExpression expression) {
        Expression subExpression = expression.getExpression();
        subExpression.visit(controller.getAcg());
        controller.getOperandStack().box();
        controller.getOperandStack().pushDynamicName(expression.getMethodName());
        getMethodPointer.call(controller.getMethodVisitor());
        controller.getOperandStack().replace(ClassHelper.CLOSURE_TYPE,2);
    }
}
