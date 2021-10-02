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
package org.codehaus.groovy.ast.decompiled;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.util.URLStreams;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.codehaus.groovy.control.CompilerConfiguration.ASM_API_VERSION;
import static org.codehaus.groovy.control.ResolveVisitor.EMPTY_STRING_ARRAY;

/**
 * A utility class responsible for decompiling JVM class files and producing {@link ClassStub} objects reflecting their structure.
 */
public abstract class AsmDecompiler {

    /**
     * Caches stubs per URI. This cache is useful when performing multiple compilations in the same JVM/class loader and in tests.
     *
     * It's synchronized "just in case". Occasional misses are expected if several threads attempt to load the same class,
     * but this shouldn't result in serious memory issues.
     */
    private static final Map<URI, SoftReference<ClassStub>> stubCache = new ConcurrentHashMap<>(); // According to http://michaelscharf.blogspot.jp/2006/11/javaneturlequals-and-hashcode-make.html, use java.net.URI instead.

    /**
     * Loads the URL contents and parses them with ASM, producing a {@link ClassStub} object representing the structure of
     * the corresponding class file. Stubs are cached and reused if queried several times with equal URLs.
     *
     * @param url an URL from a class loader, most likely a file system file or a JAR entry.
     * @return the class stub
     * @throws IOException if reading from this URL is impossible
     */
    public static ClassStub parseClass(final URL url) throws IOException {
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new GroovyRuntimeException(e);
        }

        SoftReference<ClassStub> ref = stubCache.get(uri);
        ClassStub stub = (ref != null ? ref.get() : null);
        if (stub == null) {
            DecompilingVisitor visitor = new DecompilingVisitor();

            try (InputStream stream = new BufferedInputStream(URLStreams.openUncachedStream(url))) {
                new ClassReader(stream).accept(visitor, ClassReader.SKIP_FRAMES);
            }

            stub = visitor.result;
            stubCache.put(uri, new SoftReference<>(stub));
        }
        return stub;
    }

    private static AnnotationReader readAnnotationMembers(final AnnotationStub stub) {
        return new AnnotationReader() {
            @Override
            void visitAttribute(final String name, final Object value) {
                stub.members.put(name, value);
            }
        };
    }

    static String fromInternalName(final String name) {
        return name.replace('/', '.');
    }

    //--------------------------------------------------------------------------

    private static class DecompilingVisitor extends ClassVisitor {

        private ClassStub result;

        public DecompilingVisitor() {
            super(ASM_API_VERSION);
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaceNames) {
            result = new ClassStub(fromInternalName(name), access, signature, superName, interfaceNames);
        }

        @Override
        public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            /*
             * Class files generated for inner classes have an INNERCLASS
             * reference to self. The top level class access modifiers for
             * an inner class will not accurately reflect their access. For
             * example, top-level access modifiers for private inner classes
             * are package-private, protected inner classes are public, and
             * the static modifier is not included. So the INNERCLASS self
             * reference is used to capture the correct modifiers.
             *
             * Must compare against the fully qualified name because there may
             * be other INNERCLASS references to same named nested classes from
             * other classes.
             *
             * Example:
             *
             *   public final class org/foo/Groovy8632$Builder extends org/foo/Groovy8632Abstract$Builder  {
             *     public final static INNERCLASS org/foo/Groovy8632$Builder org/foo/Groovy8632 Builder
             *     public static abstract INNERCLASS org/foo/Groovy8632Abstract$Builder org/foo/Groovy8632Abstract Builder
             */
            if (fromInternalName(name).equals(result.className)) {
                result.innerClassModifiers = access;
            }
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            if ("<clinit>".equals(name)) return null;

            MethodStub stub = new MethodStub(name, access, desc, signature, exceptions != null ? exceptions : EMPTY_STRING_ARRAY);
            if (result.methods == null) result.methods = new ArrayList<>(1);
            result.methods.add(stub);
            return new MethodVisitor(api) {
                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    return readAnnotationMembers(stub.addAnnotation(desc));
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
                    if (stub.parameterAnnotations == null) stub.parameterAnnotations = new HashMap<>(1);
                    List<AnnotationStub> list = stub.parameterAnnotations.computeIfAbsent(parameter, k -> new ArrayList<>());
                    AnnotationStub annotationStub = new AnnotationStub(desc);
                    list.add(annotationStub);
                    return readAnnotationMembers(annotationStub);
                }

                @Override
                public AnnotationVisitor visitAnnotationDefault() {
                    return new AnnotationReader() {
                        @Override
                        void visitAttribute(final String name, final Object value) {
                            stub.annotationDefault = value;
                        }
                    };
                }

                @Override
                public void visitParameter(final String name, final int access) {
                    if (stub.parameterNames == null) stub.parameterNames = new ArrayList<>();
                    stub.parameterNames.add(name);
                }
            };
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return readAnnotationMembers(result.addAnnotation(desc));
        }

        @Override
        public void visitPermittedSubclass(final String permittedSubclass) {
            result.permittedSubclasses.add(permittedSubclass);
        }

        @Override
        public RecordComponentVisitor visitRecordComponent(
                final String name, final String descriptor, final String signature) {

            RecordComponentStub recordComponentStub = new RecordComponentStub(name, descriptor, signature);
            result.recordComponents.add(recordComponentStub);

            return new RecordComponentVisitor(api) {
                @Override
                public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                    return readAnnotationMembers(recordComponentStub.addAnnotation(descriptor));
                }
            };
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
            FieldStub stub = new FieldStub(name, access, desc, signature, value);
            if (result.fields == null) result.fields = new ArrayList<>(1);
            result.fields.add(stub);
            return new FieldVisitor(api) {
                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    return readAnnotationMembers(stub.addAnnotation(desc));
                }
            };
        }
    }

    //--------------------------------------------------------------------------

    private abstract static class AnnotationReader extends AnnotationVisitor {

        public AnnotationReader() {
            super(ASM_API_VERSION);
        }

        abstract void visitAttribute(String name, Object value);

        @Override
        public void visit(final String name, final Object value) {
            visitAttribute(name, value instanceof Type ? new TypeWrapper(((Type) value).getDescriptor()) : value);
        }

        @Override
        public void visitEnum(final String name, final String desc, final String value) {
            visitAttribute(name, new EnumConstantWrapper(desc, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            AnnotationStub stub = new AnnotationStub(desc);
            visitAttribute(name, stub);
            return readAnnotationMembers(stub);
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            List<Object> list = new ArrayList<>();
            visitAttribute(name, list);
            return new AnnotationReader() {
                @Override
                void visitAttribute(String name, Object value) {
                    list.add(value);
                }
            };
        }
    }
}
