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
package org.codehaus.groovy.reflection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formal parameter names for a reflective method or constructor.
 * <p>
 * {@link Parameter#getName()} is {@code arg0} unless the class was compiled
 * with {@code -parameters}. JDK and Groovy class files still record the
 * source names in {@code LocalVariableTable}; those names are used when the
 * method-parameters attribute is absent.
 */
public final class ReflectionParameterNames {

    private static final Map<Class<?>, Map<String, String[]>> DEBUG_NAMES = new ConcurrentHashMap<>();

    private ReflectionParameterNames() {
    }

    private static boolean anyName(final String[] names) {
        if (names == null) {
            return false;
        }
        for (String name : names) {
            if (name != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param executable method or constructor
     * @return one name per parameter, never {@code null}; missing names are {@code argN}
     */
    public static String[] names(final Executable executable) {
        int count = executable == null ? 0 : executable.getParameterCount();
        String[] names = new String[count];
        if (executable == null || count == 0) {
            return names;
        }
        Parameter[] parameters = executable.getParameters();
        String[] debug = null;
        for (int i = 0; i < count; i++) {
            if (i < parameters.length && parameters[i].isNamePresent()) {
                names[i] = parameters[i].getName();
            } else {
                if (debug == null) {
                    debug = debugNames(executable);
                }
                names[i] = debug != null && i < debug.length && debug[i] != null ? debug[i] : "arg" + i;
            }
        }
        return names;
    }

    private static String[] debugNames(final Executable executable) {
        String key = executable instanceof Method method
                ? method.getName() + Type.getMethodDescriptor(method)
                : "<init>" + Type.getConstructorDescriptor((java.lang.reflect.Constructor<?>) executable);
        Map<String, String[]> table = DEBUG_NAMES.computeIfAbsent(executable.getDeclaringClass(), ReflectionParameterNames::readClass);
        String[] found = table.get(key);
        return found == null ? null : found.clone();
    }

    private static Map<String, String[]> readClass(final Class<?> type) {
        Map<String, String[]> table = new HashMap<>();
        if (type == null || type.isPrimitive() || type.isArray()) {
            return table;
        }
        String resource = type.getName().replace('.', '/') + ".class";
        InputStream in = type.getClassLoader() == null
                ? ClassLoader.getSystemResourceAsStream(resource)
                : type.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            return table;
        }
        try (in) {
            ClassReader reader = new ClassReader(in);
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                                  final String signature, final String[] exceptions) {
                    boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
                    Type[] arguments = Type.getArgumentTypes(descriptor);
                    String[] slots = new String[arguments.length];
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitLocalVariable(final String variable, final String desc, final String sig,
                                                        final Label start, final Label end, final int index) {
                            // Javac lists parameters before locals that reuse a slot.
                            // Label offsets are not resolved while the table is visited.
                            if (variable == null || "this".equals(variable)) {
                                return;
                            }
                            int slot = isStatic ? 0 : 1;
                            for (int i = 0; i < arguments.length; i++) {
                                if (slot == index && slots[i] == null) {
                                    slots[i] = variable;
                                    return;
                                }
                                slot += arguments[i].getSize();
                            }
                        }

                        @Override
                        public void visitEnd() {
                            if (!anyName(slots)) {
                                return;
                            }
                            String[] existing = table.get(name + descriptor);
                            if (existing == null || !anyName(existing)) {
                                table.put(name + descriptor, slots);
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ignored) {
            return table;
        }
        return table;
    }
}
