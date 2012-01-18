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
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.MethodVisitor;

import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACONST_NULL;

public class StaticInvocationWriter extends InvocationWriter {
    private final WriterController controller;
    
    public StaticInvocationWriter(WriterController wc) {
        super(wc);
        controller = wc;
    }

    @Override
    public void writeInvokeConstructor(final ConstructorCallExpression call) {
        MethodNode mn = (MethodNode) call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn==null) {
            super.writeInvokeConstructor(call);
            return;
        }
        ConstructorNode cn;
        if (mn instanceof ConstructorNode) {
            cn = (ConstructorNode) mn;
        } else {
            cn = new ConstructorNode(mn.getModifiers(), mn.getParameters(), mn.getExceptions(), mn.getCode());
            cn.setDeclaringClass(mn.getDeclaringClass());
        }

        String ownerDescriptor = prepareConstructorCall(cn);
        TupleExpression args = makeArgumentList(call.getArguments());
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, args.getExpressions().size());

    }

    @Override
    protected boolean writeDirectMethodCall(final MethodNode target, final boolean implicitThis, final Expression receiver, final TupleExpression args) {
        if (target instanceof ExtensionMethodNode) {            
            MethodNode node = ((ExtensionMethodNode)target).getExtensionMethodNode();
            String methodName = target.getName();

            MethodVisitor mv = controller.getMethodVisitor();
            int argumentsToRemove = 0;
            List<Expression> argumentList = new LinkedList<Expression> (args.getExpressions());
            argumentList.add(0, receiver);
            Parameter[] parameters = node.getParameters();
            loadArguments(argumentList, parameters);

            String owner = BytecodeHelper.getClassInternalName(node.getDeclaringClass());
            String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), parameters);
            mv.visitMethodInsn(INVOKESTATIC, owner, methodName, desc);
            ClassNode ret = target.getReturnType().redirect();
            if (ret== ClassHelper.VOID_TYPE) {
                ret = ClassHelper.OBJECT_TYPE;
                mv.visitInsn(ACONST_NULL);
            }
            argumentsToRemove += argumentList.size();
            controller.getOperandStack().remove(argumentsToRemove);
            controller.getOperandStack().push(ret);
            return true;
        } else {
            if (target == StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS) {
                // wrap arguments into an array
                ArrayExpression arr = new ArrayExpression(ClassHelper.OBJECT_TYPE, args.getExpressions());
                return super.writeDirectMethodCall(target, implicitThis, receiver, new ArgumentListExpression(arr));
            }
            return super.writeDirectMethodCall(target, implicitThis, receiver, args);
        }
    }

    protected void loadArguments(List<Expression> argumentList, Parameter[] para) {
        if (para.length==0) return;
        ClassNode lastParaType = para[para.length - 1].getOriginType();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();
        if (lastParaType.isArray()
                && (argumentList.size()>para.length || argumentList.size()==para.length-1 || !argumentList.get(para.length-1).getType().isArray())) {
            int stackLen = operandStack.getStackLength()+argumentList.size();
            MethodVisitor mv = controller.getMethodVisitor();
            MethodVisitor orig = mv;
            //mv = new org.objectweb.asm.util.TraceMethodVisitor(mv);
            controller.setMethodVisitor(mv);
            // varg call
            // first parameters as usual
            for (int i = 0; i < para.length-1; i++) {
                argumentList.get(i).visit(acg);
                operandStack.doGroovyCast(para[i].getType());
            }
            // last parameters wrapped in an array
            List<Expression> lastParams = new LinkedList<Expression>();
            for (int i=para.length-1; i<argumentList.size();i++) {
                lastParams.add(argumentList.get(i));
            }
            ArrayExpression array = new ArrayExpression(
                    lastParaType.getComponentType(),
                    lastParams
            );
            array.visit(acg);
            // adjust stack length
            while (operandStack.getStackLength()<stackLen) {
                operandStack.push(ClassHelper.OBJECT_TYPE);
            }
            if (argumentList.size()==para.length-1) {
                operandStack.remove(1);
            }
        } else if (argumentList.size()==para.length) {
            for (int i = 0; i < argumentList.size(); i++) {
                argumentList.get(i).visit(acg);
                operandStack.doGroovyCast(para[i].getType());
            }
        } else {
            // method call with default arguments
            TypeChooser typeChooser = controller.getTypeChooser();
            ClassNode classNode = controller.getClassNode();
            Expression[] arguments = new Expression[para.length];
            for (int i=para.length-1, j=argumentList.size()-1; i>=0;i--) {
                Parameter curParam = para[i];
                ClassNode curParamType = curParam.getType();
                Expression curArg = argumentList.get(j);
                Expression initialExpression = (Expression) curParam.getNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION);
                ClassNode curArgType = typeChooser.resolveType(curArg, classNode);
                if (initialExpression!=null && !compatibleArgumentType(curArgType, curParamType)) {
                    // use default expression
                    arguments[i] = initialExpression;
                } else {
                    arguments[i] = curArg;
                    j--;
                }
            }
            for (int i = 0; i < arguments.length; i++) {
                arguments[i].visit(acg);
                operandStack.doGroovyCast(para[i].getType());
            }
        }
    }

    private boolean compatibleArgumentType(ClassNode argumentType, ClassNode paramType) {
        if (argumentType.equals(paramType)) return true;
        if (paramType.isInterface()) return argumentType.implementsInterface(paramType);
        if (paramType.isArray() && argumentType.isArray()) return compatibleArgumentType(argumentType.getComponentType(),paramType.getComponentType());
        return argumentType.isDerivedFrom(paramType);
    }
}
