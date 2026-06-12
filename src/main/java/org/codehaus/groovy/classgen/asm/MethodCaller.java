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

import org.codehaus.groovy.classgen.ClassGeneratorException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * A helper class to invoke methods more easily in ASM
 */
public class MethodCaller {

    private int opcode;
    private String internalName;
    private String name;
    private Class theClass;
    private String methodDescriptor;
    private int parameterCount;
    private static final int ANY_PARAMETER_COUNT = -1;

    /**
     * Creates a MethodCaller for a static method.
     *
     * @param theClass the class containing the method
     * @param name the method name
     * @return a new MethodCaller for a static method
     */
    public static MethodCaller newStatic(Class theClass, String name) {
        return new MethodCaller(INVOKESTATIC, theClass, name);
    }

    /**
     * Creates a MethodCaller for a static method with a specific parameter count.
     *
     * @param theClass the class containing the method
     * @param name the method name
     * @param parameterCount the number of parameters
     * @return a new MethodCaller for a static method
     */
    public static MethodCaller newStatic(Class theClass, String name, int parameterCount) {
        return new MethodCaller(INVOKESTATIC, theClass, name, parameterCount);
    }

    /**
     * Creates a MethodCaller for an interface method.
     *
     * @param theClass the interface containing the method
     * @param name the method name
     * @return a new MethodCaller for an interface method
     */
    public static MethodCaller newInterface(Class theClass, String name) {
        return new MethodCaller(INVOKEINTERFACE, theClass, name);
    }

    /**
     * Creates a MethodCaller for a virtual method.
     *
     * @param theClass the class containing the method
     * @param name the method name
     * @return a new MethodCaller for a virtual method
     */
    public static MethodCaller newVirtual(Class theClass, String name) {
        return new MethodCaller(INVOKEVIRTUAL, theClass, name);
    }

    /**
     * @since 2.5.0
     */
    protected MethodCaller() {}

    /**
     * Creates a MethodCaller for a specific method invocation.
     *
     * @param opcode the invocation opcode
     * @param theClass the class containing the method
     * @param name the method name
     */
    public MethodCaller(int opcode, Class theClass, String name) {
        this(opcode, theClass, name, ANY_PARAMETER_COUNT);
    }

    /**
     * Creates a MethodCaller for a specific method invocation with parameter count.
     *
     * @param opcode the invocation opcode
     * @param theClass the class containing the method
     * @param name the method name
     * @param parameterCount the number of parameters
     */
    public MethodCaller(int opcode, Class theClass, String name, int parameterCount) {
        this.opcode = opcode;
        this.internalName = Type.getInternalName(theClass);
        this.theClass = theClass;
        this.name = name;
        this.parameterCount = parameterCount;
    }

    /**
     * Generates the method invocation bytecode.
     *
     * @param methodVisitor the method visitor to write to
     */
    public void call(MethodVisitor methodVisitor) {
        methodVisitor.visitMethodInsn(opcode, internalName, name, getMethodDescriptor(), opcode == INVOKEINTERFACE);
    }

    /**
     * Returns the method descriptor for this method call.
     *
     * @return the method descriptor
     */
    public String getMethodDescriptor() {
        if (methodDescriptor == null) {
            Method method = getMethod();
            methodDescriptor = Type.getMethodDescriptor(method);
        }
        return methodDescriptor;
    }

    /**
     * Returns the reflected {@link java.lang.reflect.Method} for this caller,
     * matching by name and, if specified, by parameter count.
     *
     * @return the matching {@link java.lang.reflect.Method}
     * @throws RuntimeException if no matching method can be found
     */
    protected Method getMethod() {
        Method[] methods = theClass.getMethods();
        if (parameterCount != ANY_PARAMETER_COUNT) {
            for (Method method : methods) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    return method;
                }
            }
        } else {
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        }
        throw new ClassGeneratorException("Could not find method: " + name +
                (parameterCount >= 0 ? " with parameter count " + parameterCount : "") + " on class: " + theClass);
    }
}
