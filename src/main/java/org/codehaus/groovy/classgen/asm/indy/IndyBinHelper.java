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
package org.codehaus.groovy.classgen.asm.indy;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BinaryExpressionHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.vmplugin.v8.IndyCompoundAssign;
import org.codehaus.groovy.vmplugin.v8.IndyInterface;
import org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

/**
 * Binary-expression helper that routes prefix and postfix operations through the indy invocation path.
 */
public class IndyBinHelper extends BinaryExpressionHelper {

    // GEP-15: compound-assignment rides the shared IndyInterface bootstrap as its
    // own CallType, so it inherits the standard call-site lifecycle/PIC.
    private static final String BSM_DESCRIPTOR = MethodType.methodType(
            CallSite.class, Lookup.class, String.class, MethodType.class, String.class, int.class
    ).toMethodDescriptorString();

    private static final Handle BSM = new Handle(H_INVOKESTATIC,
            IndyInterface.class.getName().replace('.', '/'), "bootstrap", BSM_DESCRIPTOR, false);

    /**
     * Creates an indy-aware binary-expression helper.
     */
    public IndyBinHelper(WriterController wc) {
        super(wc);
    }

    /** {@inheritDoc} */
    @Override
    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        getController().getInvocationWriter().makeCall(
                orig, EmptyExpression.INSTANCE,
                new ConstantExpression(method),
                MethodCallExpression.NO_ARGUMENTS,
                InvocationWriter.invokeMethod,
                false, false, false);
    }

    /**
     * GEP-15: emit {@code invokedynamic} to {@link IndyInterface#bootstrap}
     * with the {@link CallType#COMPOUND_ASSIGN} call type (resolved by
     * {@link IndyCompoundAssign}), replacing the uncached static call into
     * {@code ScriptBytecodeAdapter.compoundAssign}. Subscript LHS (e.g.
     * {@code a[i] += b}) is out of GEP-15 scope and stays on the legacy path.
     */
    @Override
    protected void evaluateCompoundAssign(final String assignName, final String baseName, final BinaryExpression expression) {
        Expression leftExpression = expression.getLeftExpression();
        if (leftExpression instanceof BinaryExpression bexp
                && bexp.getOperation().getType() == LEFT_SQUARE_BRACKET) {
            super.evaluateCompoundAssign(assignName, baseName, expression); // legacy getAt/putAt path
            return;
        }

        WriterController controller = getController();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();

        // Push receiver (current LHS value) and argument, both boxed to Object to
        // match the (Object,Object)Object compound-assign site the resolver's
        // guards are built for.
        compileStack.pushLHS(false);
        leftExpression.visit(acg);
        operandStack.box();
        expression.getRightExpression().visit(acg);
        operandStack.box();
        compileStack.popLHS();

        // callType name selects COMPOUND_ASSIGN; the bootstrap's name constant
        // carries both operator names packed together; flags are unused (0).
        mv.visitInvokeDynamicInsn(CallType.COMPOUND_ASSIGN.getCallSiteName(),
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                BSM, IndyCompoundAssign.packNames(assignName, baseName), 0);
        operandStack.replace(OBJECT_TYPE, 2);

        // Store the returned value back into the LHS and leave it as the expression value.
        operandStack.dup();
        compileStack.pushLHS(true);
        leftExpression.visit(acg);
        compileStack.popLHS();
    }
}
