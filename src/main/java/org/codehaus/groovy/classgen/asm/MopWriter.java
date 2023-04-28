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
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.objectweb.asm.MethodVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class MopWriter {

    @FunctionalInterface
    public interface Factory {
        MopWriter create(WriterController controller);
    }

    public static final Factory FACTORY = MopWriter::new;

    private static class MopKey {
        final int hash;
        final String name;
        final Parameter[] params;

        MopKey(final String name, final Parameter[] params) {
            this.name = name;
            this.params = params;
            hash = name.hashCode() << 2 + params.length;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof MopKey)) {
                return false;
            }
            MopKey other = (MopKey) obj;
            return other.name.equals(name) && ParameterUtils.parametersEqual(other.params, params);
        }
    }

    //--------------------------------------------------------------------------

    protected final WriterController controller;

    public MopWriter(final WriterController controller) {
        this.controller = requireNonNull(controller);
    }

    public void createMopMethods() {
        ClassNode classNode = controller.getClassNode();
        if (!ClassHelper.isGeneratedFunction(classNode)) {
            visitMopMethodList(classNode.getMethods(), true, Collections.emptySet(), Collections.emptyList());
            visitMopMethodList(getSuperMethods(classNode)/*GROOVY-8693, et al.*/, false, classNode.getMethods().stream()
                    .map(mn -> new MopKey(mn.getName(), mn.getParameters())).collect(toSet()), controller.getSuperMethodNames());
        }
    }

    /**
     * Filters a list of method for MOP methods. For all methods that are not
     * MOP methods a MOP method is created if the method is not public and the
     * call would be a call on "this" (isThis == true). If the call is not on
     * "this", then the call is a call on "super" and all methods are used,
     * unless they are already a MOP method.
     *
     * @param methods unfiltered list of methods for MOP
     * @param isThis  if true, then we are creating a MOP method on "this", "super" else
     *
     * @see #generateMopCalls(LinkedList, boolean)
     */
    private void visitMopMethodList(final Iterable<MethodNode> methods, final boolean isThis, final Set<MopKey> onlyIfThis, final List<String> orThis) {
        LinkedList<MethodNode> list = new LinkedList<>();
        Map<MopKey, MethodNode> map = new HashMap<>();
        for (MethodNode mn : methods) {
            // mop methods are helper for this and super calls and do direct calls
            // to the target methods; such a method cannot be abstract or a bridge
            if ((mn.getModifiers() & (ACC_ABSTRACT | ACC_BRIDGE | ACC_STATIC)) != 0) continue;
            // no this$ methods for non-private isThis=true
            // super$ method for non-private isThis=false
            // --> results in XOR
            if (isThis ^ mn.isPrivate()) continue;

            String methodName = mn.getName();
            Parameter[] parameters = mn.getParameters();
            if (isMopMethod(methodName)) {
                map.put(new MopKey(methodName, parameters), mn);
            } else if (!methodName.startsWith("<") && (onlyIfThis.contains(new MopKey(methodName, parameters)) || orThis.contains(methodName))) {
                if (map.put(new MopKey(getMopMethodName(mn, isThis), parameters), mn) == null) {
                    list.add(mn);
                }
            }
        }
        generateMopCalls(list, isThis);
    }

    /**
     * Generates a Meta Object Protocol method that is used to call a non-public
     * method or to make a call to super.
     *
     * @param methods list of methods a MOP call method should be generated for
     * @param useThis indicates if "this" should be used for the name and call
     */
    protected void generateMopCalls(final LinkedList<MethodNode> methods, final boolean useThis) {
        for (MethodNode method : methods) {
            ClassNode returnType = method.getReturnType();
            Parameter[] parameters = method.getParameters();
            String mopName = getMopMethodName(method, useThis);
            String signature = BytecodeHelper.getMethodDescriptor(returnType, parameters);
            MethodVisitor mv = controller.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, mopName, signature, null, null);
            controller.setMethodVisitor(mv);

            int stackIndex = 0;
            // load "this" and the parameters
            mv.visitVarInsn(ALOAD, stackIndex++);
            OperandStack operandStack = controller.getOperandStack();
            for (Parameter parameter : parameters) {
                ClassNode type = parameter.getType();
                operandStack.load(type, stackIndex++);
                if (isPrimitiveLong(type) || isPrimitiveDouble(type))
                    stackIndex += 1; // long and double use two slots
            }
            operandStack.remove(parameters.length);

            // make call to this or super method with operands
            ClassNode receiverType = controller.getThisType();
            if (!useThis) {
                receiverType = receiverType.getSuperClass();
                ClassNode declaringType = method.getDeclaringClass();
                // GROOVY-8693, GROOVY-9909, et al.: method from interface not implemented by super class
                if (declaringType.isInterface() && !receiverType.implementsInterface(declaringType)) receiverType = declaringType;
            }
            mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(receiverType), method.getName(), signature, receiverType.isInterface());

            BytecodeHelper.doReturn(mv, returnType);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            controller.getClassNode().addMethod(mopName, ACC_PUBLIC | ACC_SYNTHETIC, returnType, parameters, null, null);
        }
    }

    //--------------------------------------------------------------------------

    private static Iterable<MethodNode> getSuperMethods(final ClassNode classNode) {
        Map<String, MethodNode> result = classNode.getSuperClass().getDeclaredMethodsMap();
        for (ClassNode in : classNode.getInterfaces()) { // declared!
            if (!classNode.getSuperClass().implementsInterface(in)) {
                for (MethodNode mn : in.getMethods()) { // only direct default methods!
                    if (mn.isDefault()) result.putIfAbsent(mn.getTypeDescriptor(), mn);
                }
            }
        }
        return result.values();
    }

    /**
     * Creates a MOP method name from a method.
     *
     * @param method  the method to be called by the mop method
     * @param useThis if true, then it is a call on "this", "super" else
     * @return the mop method name
     */
    public static String getMopMethodName(final MethodNode method, final boolean useThis) {
        ClassNode declaringClass = method.getDeclaringClass();
        int distance = 1;
        if (!declaringClass.isInterface()) { // GROOVY-8693: fixed distance for interface methods
            for (ClassNode sc = declaringClass.getSuperClass(); sc != null; sc = sc.getSuperClass()) {
                distance += 1;
            }
        }
        return (useThis ? "this" : "super") + "$" + distance + "$" + method.getName();
    }

    /**
     * Determines if a method is a MOP method. This is done by the method name.
     * If the name starts with "this$" or "super$" but does not contain "$dist$",
     * then it is an MOP method.
     *
     * @param methodName name of the method to test
     * @return true if the method is a MOP method
     */
    public static boolean isMopMethod(final String methodName) {
        return (methodName.startsWith("this$") || methodName.startsWith("super$")) && !methodName.contains("$dist$");
    }

    @Deprecated
    public static boolean equalParameterTypes(final Parameter[] p1, final Parameter[] p2) {
        return ParameterUtils.parametersEqual(p1, p2);
    }
}
