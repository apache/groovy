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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class MopWriter {
    public interface Factory {
        MopWriter create(WriterController controller);
    }

    public static final Factory FACTORY = new Factory() {
        @Override
        public MopWriter create(final WriterController controller) {
            return new MopWriter(controller);
        }
    };

    private static class MopKey {
        final int hash;
        final String name;
        final Parameter[] params;

        MopKey(String name, Parameter[] params) {
            this.name = name;
            this.params = params;
            hash = name.hashCode() << 2 + params.length;
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof MopKey)) {
                return false;
            }

            MopKey other = (MopKey) obj;
            return other.name.equals(name) && equalParameterTypes(other.params,params);
        }
    }
    
    private final WriterController controller;
    
    public MopWriter(WriterController wc) {
        controller = wc;
    }
    
    public void createMopMethods() {
        ClassNode classNode = controller.getClassNode();
        if (classNode.declaresAnyInterfaces(ClassHelper.GENERATED_CLOSURE_Type, ClassHelper.GENERATED_LAMBDA_TYPE)) {
            return;
        }
        Set<MopKey> currentClassSignatures = buildCurrentClassSignatureSet(classNode.getMethods());
        visitMopMethodList(classNode.getMethods(), true, Collections.EMPTY_SET, Collections.EMPTY_LIST);
        visitMopMethodList(classNode.getSuperClass().getAllDeclaredMethods(), false, currentClassSignatures, controller.getSuperMethodNames());
    }

    private static Set<MopKey> buildCurrentClassSignatureSet(List<MethodNode> methods) {
        if (methods.isEmpty()) return Collections.EMPTY_SET;
        Set<MopKey> result = new HashSet<>(methods.size());
        for (MethodNode mn : methods) {
            MopKey key = new MopKey(mn.getName(), mn.getParameters());
            result.add(key);
        }
        return result;
    }
    
    /**
     * filters a list of method for MOP methods. For all methods that are no
     * MOP methods a MOP method is created if the method is not public and the
     * call would be a call on "this" (isThis == true). If the call is not on
     * "this", then the call is a call on "super" and all methods are used,
     * unless they are already a MOP method
     *
     * @param methods unfiltered list of methods for MOP
     * @param isThis  if true, then we are creating a MOP method on "this", "super" else
     * @see #generateMopCalls(LinkedList, boolean)
     */
    private void visitMopMethodList(List<MethodNode> methods, boolean isThis, Set<MopKey> useOnlyIfDeclaredHereToo, List<String> orNameMentionedHere) {
        Map<MopKey, MethodNode> mops = new HashMap<>();
        LinkedList<MethodNode> mopCalls = new LinkedList<>();
        for (MethodNode mn : methods) {
            // mop methods are helper for this and super calls and do direct calls
            // to the target methods. Such a method cannot be abstract or a bridge
            if ((mn.getModifiers() & (ACC_ABSTRACT | ACC_BRIDGE)) != 0) continue;
            if (mn.isStatic()) continue;
            // no this$ methods for non-private isThis=true
            // super$ method for non-private isThis=false
            // --> results in XOR
            boolean isPrivate = Modifier.isPrivate(mn.getModifiers());
            if (isThis ^ isPrivate) continue;
            String methodName = mn.getName();
            if (isMopMethod(methodName)) {
                mops.put(new MopKey(methodName, mn.getParameters()), mn);
                continue;
            }
            if (methodName.startsWith("<")) continue;
            if (!useOnlyIfDeclaredHereToo.contains(new MopKey(methodName, mn.getParameters())) &&
                !orNameMentionedHere.contains(methodName))
            {
                continue;
            }
            String name = getMopMethodName(mn, isThis);
            MopKey key = new MopKey(name, mn.getParameters());
            if (mops.containsKey(key)) continue;
            mops.put(key, mn);
            mopCalls.add(mn);
        }
        generateMopCalls(mopCalls, isThis);
        mopCalls.clear();
        mops.clear();
    }

    /**
     * creates a MOP method name from a method
     *
     * @param method  the method to be called by the mop method
     * @param useThis if true, then it is a call on "this", "super" else
     * @return the mop method name
     */
    public static String getMopMethodName(MethodNode method, boolean useThis) {
        ClassNode declaringNode = method.getDeclaringClass();
        int distance = 0;
        for (; declaringNode != null; declaringNode = declaringNode.getSuperClass()) {
            distance++;
        }
        return (useThis ? "this" : "super") + "$" + distance + "$" + method.getName();
    }

    /**
     * method to determine if a method is a MOP method. This is done by the
     * method name. If the name starts with "this$" or "super$" but does not 
     * contain "$dist$", then it is an MOP method
     *
     * @param methodName name of the method to test
     * @return true if the method is a MOP method
     */
    public static boolean isMopMethod(String methodName) {
        return (methodName.startsWith("this$") ||
                methodName.startsWith("super$")) && !methodName.contains("$dist$");
    }

    /**
     * generates a Meta Object Protocol method, that is used to call a non public
     * method, or to make a call to super.
     *
     * @param mopCalls list of methods a mop call method should be generated for
     * @param useThis  true if "this" should be used for the naming
     */
    protected void generateMopCalls(LinkedList<MethodNode> mopCalls, boolean useThis) {
        for (MethodNode method : mopCalls) {
            String name = getMopMethodName(method, useThis);
            Parameter[] parameters = method.getParameters();
            String methodDescriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameters());
            MethodVisitor mv = controller.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, name, methodDescriptor, null, null);
            controller.setMethodVisitor(mv);
            mv.visitVarInsn(ALOAD, 0);
            int newRegister = 1;
            OperandStack operandStack = controller.getOperandStack();
            for (Parameter parameter : parameters) {
                ClassNode type = parameter.getType();
                operandStack.load(parameter.getType(), newRegister);
                // increment to next register, double/long are using two places
                newRegister++;
                if (type == ClassHelper.double_TYPE || type == ClassHelper.long_TYPE) newRegister++;
            }
            operandStack.remove(parameters.length);
            ClassNode declaringClass = method.getDeclaringClass();
            // JDK 8 support for default methods in interfaces
            // this should probably be strenghtened when we support the A.super.foo() syntax
            int opcode = declaringClass.isInterface()?INVOKEINTERFACE:INVOKESPECIAL;
            mv.visitMethodInsn(opcode, BytecodeHelper.getClassInternalName(declaringClass), method.getName(), methodDescriptor, declaringClass.isInterface());
            BytecodeHelper.doReturn(mv, method.getReturnType());
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            controller.getClassNode().addMethod(name, ACC_PUBLIC | ACC_SYNTHETIC, method.getReturnType(), parameters, null, null);
        }
    }

    public static boolean equalParameterTypes(Parameter[] p1, Parameter[] p2) {
        if (p1.length != p2.length) return false;
        for (int i = 0; i < p1.length; i++) {
            if (!p1[i].getType().equals(p2[i].getType())) return false;
        }
        return true;
    }

}
