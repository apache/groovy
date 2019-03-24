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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.objectweb.asm.MethodVisitor;

/**
 * Helper class that takes an Expression and if visited will load it normally, 
 * storing the result in a helper variable, which then can be requested after
 * the visit is completed. A copy of the variable will stay on the stack. 
 * Subsequent visits will load the stored value instead of visiting the 
 * expression again
 */
public class ExpressionAsVariableSlot extends BytecodeExpression {
    private int index = -1;
    private final Expression exp;
    private final WriterController controller;
    private final String name;

    public ExpressionAsVariableSlot(WriterController controller, Expression expression, String name) {
        this.exp = expression;
        this.controller = controller;
        this.name = name;
    }
    
    public ExpressionAsVariableSlot(WriterController controller, Expression expression) {
        this(controller, expression, "ExpressionAsVariableSlot_TEMP");
    }

    @Override
    public void visit(MethodVisitor mv) {
        OperandStack os = controller.getOperandStack();
        if (index == -1) { // first accept
            // accept expression
            exp.accept(controller.getAcg());
            // make copy & set type
            os.dup();
            this.setType(os.getTopOperand());
            // store copy in temporary variable
            CompileStack compileStack = controller.getCompileStack();
            index = compileStack.defineTemporaryVariable(name, getType(), true);
        } else {
            os.load(getType(), index);
        }
        // since the calling code will push the type again, we better remove it here
        os.remove(1);
    }

    /**
     * returns the index of the bytecode variable
     */
    public int getIndex() {
        if (index == -1) throw new GroovyBugError("index requested before visit!");
        return index;
    }

    @Override
    public String getText() {
        return exp.getText();
    }
}
