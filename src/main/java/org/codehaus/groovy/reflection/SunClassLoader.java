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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
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
    private static final String MAGICACCESSORIMPL_FULL_CLASS_NAME = "sun.reflect.MagicAccessorImpl";
    private static final String SUN_REFLECT_MAGICACCESSORIMPL = "sun/reflect/MagicAccessorImpl";
    protected static final String SUN_REFLECT_GROOVYMAGIC = "sun/reflect/GroovyMagic";
    private static final String GROOVYMAGIC_FULL_CLASS_NAME = SUN_REFLECT_GROOVYMAGIC.replace('/', '.');
    protected final Map<String, Class> knownClasses = new HashMap<>();
    protected static final SunClassLoader sunVM;

    static {
        SunClassLoader scl;
        try {
            scl = AccessController.doPrivileged((PrivilegedAction<SunClassLoader>) () -> {
                try {
                    return new SunClassLoader();
                } catch (Throwable e) {
                    return null;
                }
            });
        } catch (Throwable e) {
            scl = null;
        }
        sunVM = scl;
    }

    protected SunClassLoader() throws Throwable {
        super(SunClassLoader.class.getClassLoader());

        final Class magic = ClassLoader.getSystemClassLoader().loadClass(MAGICACCESSORIMPL_FULL_CLASS_NAME);
        knownClasses.put(MAGICACCESSORIMPL_FULL_CLASS_NAME, magic);
        loadMagic();
    }

    private void loadMagic() {
        ClassWriter cw = new ClassWriter(CompilerConfiguration.ASM_COMPUTE_MODE);
        cw.visit(CompilerConfiguration.DEFAULT.getAsmTargetBytecode(), Opcodes.ACC_PUBLIC, SUN_REFLECT_GROOVYMAGIC, null, SUN_REFLECT_MAGICACCESSORIMPL, null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, SUN_REFLECT_MAGICACCESSORIMPL, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        define(cw.toByteArray(), GROOVYMAGIC_FULL_CLASS_NAME);
    }

    protected void loadFromResource(String name) throws IOException {
        try (InputStream asStream =
                     new BufferedInputStream(
                             SunClassLoader.class.getClassLoader().getResourceAsStream(
                                     resourceName(name)))) {
            ClassReader reader = new ClassReader(asStream);
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            reader.accept(cw, ClassReader.SKIP_CODE);
            define(cw.toByteArray(), name);
        }
    }

    protected static String resourceName(String s) {
        return s.replace('.', '/') + ".class";
    }

    protected void define(byte[] bytes, final String name) {
        knownClasses.put(name, defineClass(name, bytes, 0, bytes.length));
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class aClass = knownClasses.get(name);
        if (aClass != null) {
            return aClass;
        } else {
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
