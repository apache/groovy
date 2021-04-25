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

import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;

/**
 * Generates bytecode for method pointer expressions.
 *
 * @since 3.0.0
 */
public class MethodPointerExpressionWriter {

    private static final MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");

    protected final WriterController controller;

    public MethodPointerExpressionWriter(final WriterController controller) {
        this.controller = controller;
    }

    public void writeMethodPointerExpression(final MethodPointerExpression pointerOrReference) {
        pointerOrReference.getExpression().visit(controller.getAcg());
        OperandStack operandStack = controller.getOperandStack();
        operandStack.box();

        operandStack.pushDynamicName(pointerOrReference.getMethodName());
        // delegate to ScriptBytecodeAdapter#getMethodPointer
        getMethodPointer.call(controller.getMethodVisitor());
        operandStack.replace(CLOSURE_TYPE, 2);
    }
}
