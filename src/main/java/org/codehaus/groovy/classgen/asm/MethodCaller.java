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

    public static MethodCaller newStatic(Class theClass, String name) {
        return new MethodCaller(INVOKESTATIC, theClass, name);
    }

    public static MethodCaller newInterface(Class theClass, String name) {
        return new MethodCaller(INVOKEINTERFACE, theClass, name);
    }

    public static MethodCaller newVirtual(Class theClass, String name) {
        return new MethodCaller(INVOKEVIRTUAL, theClass, name);
    }

    /**
     * @since 2.5.0
     */
    protected MethodCaller() {}

    public MethodCaller(int opcode, Class theClass, String name) {
        this.opcode = opcode;
        this.internalName = Type.getInternalName(theClass);
        this.theClass = theClass;
        this.name = name;

    }

    public void call(MethodVisitor methodVisitor) {
        methodVisitor.visitMethodInsn(opcode, internalName, name, getMethodDescriptor(), opcode == INVOKEINTERFACE);
    }

    public String getMethodDescriptor() {
        if (methodDescriptor == null) {
            Method method = getMethod();
            methodDescriptor = Type.getMethodDescriptor(method);
        }
        return methodDescriptor;
    }

    protected Method getMethod() {
        Method[] methods = theClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new ClassGeneratorException("Could not find method: " + name + " on class: " + theClass);
    }
}
