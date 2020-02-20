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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * Special class loader, which when running on Sun VM allows to generate accessor classes for any method
 */
public class SunClassLoader extends ClassLoader implements Opcodes {
    protected final Map<String, Class> knownClasses = new HashMap<String, Class>();

    protected static final SunClassLoader sunVM;

    static {
        SunClassLoader res;
        try {
            res = AccessController.doPrivileged((PrivilegedAction<SunClassLoader>) () -> {
                try {
                    return new SunClassLoader();
                } catch (Throwable e) {
                    return null;
                }
            });
        } catch (Throwable e) {
            res = null;
        }
        sunVM = res;
    }

    protected SunClassLoader() throws Throwable {
        super(SunClassLoader.class.getClassLoader());

        final Class magic = ClassLoader.getSystemClassLoader().loadClass("sun.reflect.MagicAccessorImpl");
        knownClasses.put("sun.reflect.MagicAccessorImpl", magic);
        loadMagic();
    }

    private void loadMagic() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, "sun/reflect/GroovyMagic", null, "sun/reflect/MagicAccessorImpl", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "sun/reflect/MagicAccessorImpl", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        define(cw.toByteArray(), "sun.reflect.GroovyMagic");
    }

    protected void loadFromRes(String name) throws IOException {
        try (final InputStream asStream = SunClassLoader.class.getClassLoader().getResourceAsStream(resName(name))) {
            ClassReader reader = new ClassReader(asStream);
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            reader.accept(cw, ClassReader.SKIP_DEBUG);
            define(cw.toByteArray(), name);
        }
    }

    protected static String resName(String s) {
        return s.replace('.', '/') + ".class";
    }

    protected void define(byte[] bytes, final String name) {
        knownClasses.put(name, defineClass(name, bytes, 0, bytes.length));
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class aClass = knownClasses.get(name);
        if (aClass != null)
            return aClass;
        else {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                return getClass().getClassLoader().loadClass(name);
            }
        }
    }

    public Class doesKnow(String name) {
        return knownClasses.get(name);
    }
}
