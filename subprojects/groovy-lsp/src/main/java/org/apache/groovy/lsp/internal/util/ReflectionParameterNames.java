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
package org.apache.groovy.lsp.internal.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Formal parameter names for a reflective method or constructor.
 * <p>
 * {@link Parameter#getName()} is {@code arg0} unless the class was compiled
 * with {@code -parameters}. JDK and Groovy class files still record the
 * source names in {@code LocalVariableTable}; those names are used when the
 * method-parameters attribute is absent. The table is cached by class with
 * weak keys, so a throwaway class loader can be collected after the caller
 * drops the class.
 */
public final class ReflectionParameterNames {

    private static final WeakHashMap<Class<?>, Map<String, String[]>> DEBUG_NAMES = new WeakHashMap<>();

    private ReflectionParameterNames() {
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
                : "<init>" + Type.getConstructorDescriptor((Constructor<?>) executable);
        Class<?> type = executable.getDeclaringClass();
        Map<String, String[]> table = cached(type);
        if (table == null) {
            Map<String, String[]> loaded = readClass(type);
            synchronized (DEBUG_NAMES) {
                Map<String, String[]> existing = DEBUG_NAMES.get(type);
                if (existing != null) {
                    table = existing;
                } else {
                    DEBUG_NAMES.put(type, loaded);
                    table = loaded;
                }
            }
        }
        String[] found = table.get(key);
        return found == null ? null : found.clone();
    }

    private static Map<String, String[]> cached(final Class<?> type) {
        synchronized (DEBUG_NAMES) {
            return DEBUG_NAMES.get(type);
        }
    }

    private static Map<String, String[]> readClass(final Class<?> type) {
        Map<String, String[]> table = new HashMap<>();
        if (type == null || type.isPrimitive() || type.isArray()) {
            return table;
        }
        String resource = type.getName().replace('.', '/') + ".class";
        ClassLoader loader = type.getClassLoader();
        InputStream in = loader == null
                ? ClassLoader.getSystemResourceAsStream(resource)
                : loader.getResourceAsStream(resource);
        if (in == null) {
            return table;
        }
        try (in) {
            new ClassReader(in).accept(new ParameterNameCollector(table), ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ignored) {
            return table;
        }
        return table;
    }

    /**
     * Reads {@code LocalVariableTable} names. Javac lists parameters before
     * locals that reuse a slot. Label offsets are not resolved while the
     * table is visited, so the first name at each parameter slot is kept.
     */
    private static final class ParameterNameCollector extends ClassVisitor {
        private final Map<String, String[]> table;

        private ParameterNameCollector(final Map<String, String[]> table) {
            super(Opcodes.ASM9);
            this.table = table;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                          final String signature, final String[] exceptions) {
            return new MethodParameterNames(table, access, name, descriptor);
        }
    }

    private static final class MethodParameterNames extends MethodVisitor {
        private final Map<String, String[]> table;
        private final boolean isStatic;
        private final String name;
        private final String descriptor;
        private final Type[] arguments;
        private final String[] slots;

        private MethodParameterNames(final Map<String, String[]> table, final int access,
                                     final String name, final String descriptor) {
            super(Opcodes.ASM9);
            this.table = table;
            this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
            this.name = name;
            this.descriptor = descriptor;
            this.arguments = Type.getArgumentTypes(descriptor);
            this.slots = new String[arguments.length];
        }

        @Override
        public void visitLocalVariable(final String variable, final String desc, final String sig,
                                        final Label start, final Label end, final int index) {
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
    }
}
