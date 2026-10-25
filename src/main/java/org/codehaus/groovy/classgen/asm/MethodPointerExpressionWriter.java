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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isClassType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Generates bytecode for method pointer expressions.
 *
 * @since 3.0.0
 */
public class MethodPointerExpressionWriter {

    private static final MethodCaller getMethodPointer = MethodCaller.newStatic(ScriptBytecodeAdapter.class, "getMethodPointer");

    /** The controller coordinating all bytecode writers for the current class. */
    protected final WriterController controller;

    /**
     * Creates a method pointer expression writer with the given controller.
     *
     * @param controller the writer controller
     */
    public MethodPointerExpressionWriter(final WriterController controller) {
        this.controller = controller;
    }

    /**
     * Generates bytecode for a method pointer expression.
     *
     * @param pointerOrReference the method pointer expression
     */
    public void writeMethodPointerExpression(final MethodPointerExpression pointerOrReference) {
        pointerOrReference.getExpression().visit(controller.getAcg());
        OperandStack operandStack = controller.getOperandStack();
        operandStack.box();

        var type = operandStack.getTopOperand();
        // GROOVY-12569: statically compiled code types a value of a class it cannot name (not public, of another
        // run-time package) as an accessible superclass; its class is loaded by name, so retain the class itself
        var inferredType = getInferredType(pointerOrReference.getExpression());
        if (inferredType != null && !AsmClassGenerator.isClassDirectlyAccessible(inferredType, controller.getClassNode())) {
            type = inferredType;
        }
        if (!isClassType(type) && !isObjectType(type)) {
            // GROOVY-5051, GROOVY-10568: retain static type
            controller.getAcg().loadWrapper(varX("", type));
        }

        operandStack.pushDynamicName(pointerOrReference.getMethodName());
        // delegate to ScriptBytecodeAdapter#getMethodPointer
        getMethodPointer.call(controller.getMethodVisitor());
        operandStack.replace(CLOSURE_TYPE, 2);
    }

    private static ClassNode getInferredType(final Expression expression) {
        Object type = expression.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (type == null && expression instanceof VariableExpression variable
                && variable.getAccessedVariable() instanceof ASTNode target && target != expression) {
            type = target.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        }
        return type instanceof ClassNode cn ? cn : null;
    }
}
