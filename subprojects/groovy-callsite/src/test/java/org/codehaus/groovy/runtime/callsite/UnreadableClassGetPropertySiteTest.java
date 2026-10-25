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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reflection over a class can fail although the class itself works, as in a native image
 * without reflection metadata for it. A class with a {@code getProperty} of its own needs
 * no reflection to answer a read, so a read through a classic call site must still reach
 * it, and must not be answered by the metaclass instead.
 * <p>
 * The class read here is built by hand to stand in for that case on the JVM: a public
 * method naming a type absent at run time makes reflection over its public methods fail.
 * A class compiled by Groovy with such a method cannot even be constructed on the JVM,
 * because building its metaclass needs the same reflection.
 * <p>
 * Written in Java so that nothing but the reads under test touches the object: a Groovy
 * cast or call would consult its metaclass, which, not being built from its own class,
 * knows nothing about it.
 */
final class UnreadableClassGetPropertySiteTest {

    private static final String NAME = "org.codehaus.groovy.runtime.callsite.UnreadableReader";
    private static final String INTERNAL_NAME = NAME.replace('.', '/');

    private final List<Object> reads = new ArrayList<>();

    // GROOVY-12489
    @Test
    void readThroughClassicCallSiteReachesOwnGetProperty() throws Exception {
        GroovyObject target = newUnreadableReader();
        CallSite site = new CallSiteArray(UnreadableClassGetPropertySiteTest.class, new String[]{"foo"}).array[0];

        assertEquals("FOO", read(() -> site.callGetProperty(target)));
        assertEquals(List.of("foo"), reads);
    }

    // GROOVY-12489
    @Test
    void readInScriptCompiledWithoutIndyReachesOwnGetProperty() throws Exception {
        Binding binding = new Binding();
        binding.setVariable("target", newUnreadableReader());
        CompilerConfiguration config = new CompilerConfiguration();
        config.getOptimizationOptions().put(CompilerConfiguration.INVOKEDYNAMIC, Boolean.FALSE);

        assertEquals("FOO", read(() -> new GroovyShell(binding, config).evaluate("target.foo")));
        assertEquals(List.of("foo"), reads);
    }

    /**
     * Performs a read, turning a failure into an assertion error that names only the class
     * of what was thrown: the message of a {@code MissingPropertyException} about this class
     * needs the class's own metaclass, so reporting it would fail and hide the failure.
     */
    private static Object read(ThrowingSupplier<Object> read) {
        try {
            return read.get();
        } catch (Throwable t) {
            throw new AssertionError("the read threw " + t.getClass().getName());
        }
    }

    /**
     * A {@code GroovyObject} that answers a read through its own {@code getProperty} with
     * the property name in upper case, and records the name in {@link #reads}. Its
     * metaclass is the one of {@code Object}, and its public method {@code take(no.such.Type)}
     * makes reflection over its public methods fail with a {@code NoClassDefFoundError}.
     */
    private GroovyObject newUnreadableReader() throws ReflectiveOperationException {
        MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(Object.class);
        return (GroovyObject) unreadableReaderClass().getConstructor(MetaClass.class, List.class).newInstance(metaClass, reads);
    }

    private static Class<?> unreadableReaderClass() throws ClassNotFoundException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, INTERNAL_NAME, null, "java/lang/Object",
                new String[]{"groovy/lang/GroovyObject"});
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "metaClass", "Lgroovy/lang/MetaClass;", null, null).visitEnd();
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "reads", "Ljava/util/List;", null, null).visitEnd();

        MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lgroovy/lang/MetaClass;Ljava/util/List;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, INTERNAL_NAME, "metaClass", "Lgroovy/lang/MetaClass;");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitFieldInsn(Opcodes.PUTFIELD, INTERNAL_NAME, "reads", "Ljava/util/List;");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "getMetaClass", "()Lgroovy/lang/MetaClass;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, INTERNAL_NAME, "metaClass", "Lgroovy/lang/MetaClass;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "setMetaClass", "(Lgroovy/lang/MetaClass;)V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "getProperty", "(Ljava/lang/String;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, INTERNAL_NAME, "reads", "Ljava/util/List;");
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(Opcodes.POP);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = writer.visitMethod(Opcodes.ACC_PUBLIC, "take", "(Lno/such/Type;)V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        writer.visitEnd();
        byte[] bytes = writer.toByteArray();
        ClassLoader loader = new ClassLoader(UnreadableClassGetPropertySiteTest.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String className) throws ClassNotFoundException {
                return NAME.equals(className) ? defineClass(className, bytes, 0, bytes.length) : super.findClass(className);
            }
        };
        return loader.loadClass(NAME);
    }
}
