/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;

/**
 * A specialized version of the multi type binary expression dispatcher which is aware of static compilation.
 * It is able to generate optimized bytecode for some operations using JVM instructions when available.
 *
 * @author Cedric Champeau
 */
public class StaticTypesBinaryExpressionMultiTypeDispatcher extends BinaryExpressionMultiTypeDispatcher implements Opcodes {
    public StaticTypesBinaryExpressionMultiTypeDispatcher(WriterController wc) {
        super(wc);
    }

    @Override
    protected void writePostOrPrefixMethod(int op, String method, Expression expression, Expression orig) {
        MethodNode mn = (MethodNode) orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn!=null) {
            WriterController controller = getController();
            controller.getOperandStack().pop();
            MethodCallExpression call = new MethodCallExpression(
                    expression,
                    method,
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );
            call.setMethodTarget(mn);
            call.visit(controller.getAcg());
        } else {
            super.writePostOrPrefixMethod(op, method, expression, orig);
        }
    }

    @Override
    public void evaluateEqual(final BinaryExpression expression, final boolean defineVariable) {
        if (!defineVariable) {
            Expression leftExpression = expression.getLeftExpression();
            if (leftExpression instanceof PropertyExpression) {
                PropertyExpression pexp = (PropertyExpression) leftExpression;
                if (makeSetProperty(
                        pexp.getObjectExpression(),
                        pexp.getProperty(),
                        new ArgumentListExpression(expression.getRightExpression()),
                        pexp.isSafe(),
                        pexp.isSpreadSafe(),
                        pexp.isImplicitThis()
                )) return;
            }
        }
        super.evaluateEqual(expression, defineVariable);
    }

    private boolean makeSetProperty(final Expression receiver, final Expression message, final Expression arguments, final boolean safe, final boolean spreadSafe, final boolean implicitThis) {
        WriterController controller = getController();
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode receiverType = typeChooser.resolveType(receiver, controller.getClassNode());
        String property = message.getText();
        String setter = "set"+ MetaClassHelper.capitalize(property);
        MethodNode setterMethod = receiverType.getSetterMethod(setter);
        if (setterMethod==null) {
            PropertyNode propertyNode = receiverType.getProperty(property);
            if (propertyNode!=null) {
                setterMethod = new MethodNode(
                        setter,
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[] { new Parameter(propertyNode.getOriginType(), "value")},
                        ClassNode.EMPTY_ARRAY,
                        EmptyStatement.INSTANCE
                );
                setterMethod.setDeclaringClass(receiverType);
            }
        }
        if (setterMethod!=null) {
            MethodCallExpression call = new MethodCallExpression(
                    receiver,
                    setter,
                    arguments
            );
            call.setImplicitThis(implicitThis);
            call.setSafe(safe);
            call.setSpreadSafe(spreadSafe);
            call.setMethodTarget(setterMethod);
            call.visit(controller.getAcg());
            return true;
        }
        return false;
    }

    protected void assignToArray(Expression parrent, Expression receiver, Expression index, Expression rhsValueLoader) {
        ClassNode current = getController().getClassNode();
        ClassNode arrayType = getController().getTypeChooser().resolveType(receiver, current);
        ClassNode arrayComponentType = arrayType.getComponentType();
        int operationType = getOperandType(arrayComponentType);
        BinaryExpressionWriter bew = binExpWriter[operationType];
        AsmClassGenerator acg = getController().getAcg();

        if (bew.arraySet(true) && arrayType.isArray()) {
            OperandStack operandStack   =   getController().getOperandStack();

            // load the array
            receiver.visit(acg);
            operandStack.doGroovyCast(arrayType);

            // load index
            index.visit(acg);
            operandStack.doGroovyCast(int_TYPE);

            // load rhs
            rhsValueLoader.visit(acg);
            operandStack.doGroovyCast(arrayComponentType);

            // store value in array
            bew.arraySet(false);

            // load return value && correct operand stack stack
            operandStack.remove(3);
            rhsValueLoader.visit(acg);
        } else {
            WriterController controller = getController();
            StaticTypeCheckingVisitor visitor = new StaticCompilationVisitor(controller.getSourceUnit(), controller.getClassNode(), null);
            // let's replace this assignment to a subscript operator with a
            // method call
            // e.g. x[5] = 10
            // -> (x, [], 5), =, 10
            // -> methodCall(x, "putAt", [5, 10])
            ArgumentListExpression ae = new ArgumentListExpression(index, rhsValueLoader);
            MethodCallExpression mce = new MethodCallExpression(
                    receiver,
                    "putAt",
                    ae
            );
            visitor.visitMethodCallExpression(mce);
            mce.visit(controller.getAcg());
            // return value of assignment
            rhsValueLoader.visit(controller.getAcg());
        }
    }

}
