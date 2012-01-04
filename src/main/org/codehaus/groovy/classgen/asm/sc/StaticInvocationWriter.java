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
}
