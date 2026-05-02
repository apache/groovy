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
import org.objectweb.asm.TypePath;

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
 * Utility class responsible for decompiling JVM class files into {@link ClassStub} objects that reflect their bytecode structure.
 * Uses ASM to parse compiled bytecode and extract class metadata including fields, methods, constructors, and annotations
 * without requiring access to source code.
 *
 * <p>Parsed stubs are cached using soft references indexed by {@link URI}, enabling efficient reuse across multiple
 * compilations in the same JVM and in test scenarios. The cache occasionally allows misses when multiple threads
 * simultaneously load the same class, but this is acceptable as it avoids serious memory issues.
 *
 * <p>The decompiler skips synthetic class initializers ({@code <clinit>}) and frame debug information during parsing.
 *
 * @see ClassStub
 * @see DecompiledClassNode
 * @see AsmReferenceResolver
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
     * @param url a URL from a class loader, most likely a file system file or a JAR entry
     * @return the class stub containing all extracted bytecode metadata
     * @throws IOException if reading from this URL is impossible
     * @throws GroovyRuntimeException if the URL cannot be converted to a valid URI
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
        /**
         * Creates an {@link AnnotationReader} that populates an annotation stub's member map with attribute values.
         *
         * @param stub the {@link AnnotationStub} to populate
         * @return an {@link AnnotationReader} that collects annotation attribute values
         */
        return new AnnotationReader() {
            @Override
            void visitAttribute(final String name, final Object value) {
                stub.members.put(name, value);
            }
        };
    }

    /**
     * Converts an internal JVM class name (e.g., "java/lang/String") to a fully qualified class name (e.g., "java.lang.String").
     *
     * @param name the internal JVM class name
     * @return the fully qualified class name with dots instead of slashes
     */
    static String fromInternalName(final String name) {
        return name.replace('/', '.');
    }

    /**
     * ASM visitor for decompiling class files into {@link ClassStub} objects.
     * Extracts class metadata, inner class information, methods, fields, annotations, record components,
     * and permitted subclasses from bytecode.
     */
    private static class DecompilingVisitor extends ClassVisitor {

        private ClassStub result;

        /**
         * Creates a decompiling visitor with the current ASM API version.
         */
        public DecompilingVisitor() {
            super(ASM_API_VERSION);
        }

        /**
         * Visits the class declaration, extracting its name, access flags, generics signature, superclass, and interfaces.
         *
         * @param version the Java class file format version
         * @param access the class access flags
         * @param name the fully qualified internal class name
         * @param signature the generic signature or {@code null}
         * @param superName the internal superclass name
         * @param interfaceNames the internal interface names
         */
        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaceNames) {
            result = new ClassStub(fromInternalName(name), access, signature, superName, interfaceNames);
        }

        /**
         * Visits INNERCLASS attributes to capture correct access modifiers for inner classes.
         * Inner class access flags may differ from top-level flags (e.g., private, protected, static modifiers).
         *
         * @param name the fully qualified internal name of the inner class
         * @param outerName the fully qualified internal name of the outer class
         * @param innerName the simple name of the inner class
         * @param access the access modifiers from the INNERCLASS attribute
         */
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

        /**
         * Visits method metadata including access flags, descriptor, signature, and exceptions.
         * Skips static class initializers ({@code <clinit>}).
         * Records method annotations and parameter metadata.
         *
         * @param access the method access flags
         * @param name the method name
         * @param desc the method descriptor
         * @param signature the generic signature or {@code null}
         * @param exceptions the exception types this method throws
         * @return a method visitor to collect parameter and annotation data, or {@code null} to skip
         */
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

        /**
         * Visits class-level annotations.
         *
         * @param desc the annotation type descriptor
         * @param visible {@code true} for runtime-visible annotations
         * @return an annotation visitor to collect member values
         */
        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return readAnnotationMembers(result.addAnnotation(desc));
        }

        /**
         * Visits permitted subclasses declared in a sealed class.
         *
         * @param permittedSubclass the fully qualified internal name of a permitted subclass
         */
        @Override
        public void visitPermittedSubclass(final String permittedSubclass) {
            result.permittedSubclasses.add(permittedSubclass);
        }

        /**
         * Visits record component metadata for record classes.
         *
         * @param name the component name
         * @param descriptor the component type descriptor
         * @param signature the generic signature or {@code null}
         * @return a record component visitor to collect annotations
         */
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

                @Override
                public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
                    return readAnnotationMembers(recordComponentStub.addTypeAnnotation(descriptor));
                }
            };
        }

        /**
         * Visits field metadata including access flags, descriptor, signature, and constant values.
         * Records field annotations.
         *
         * @param access the field access flags
         * @param name the field name
         * @param desc the field type descriptor
         * @param signature the generic signature or {@code null}
         * @param value the constant value or {@code null}
         * @return a field visitor to collect annotations
         */
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

    /**
     * Base visitor class for collecting annotation attribute values from bytecode.
     * Handles type conversions, enum constants, nested annotations, and array attributes.
     * Delegates collected values to subclass implementation via {@code visitAttribute()}.
     */
    private abstract static class AnnotationReader extends AnnotationVisitor {

        /**
         * Creates an annotation reader with the current ASM API version.
         */
        public AnnotationReader() {
            super(ASM_API_VERSION);
        }

        /**
         * Processes a single annotation attribute value.
         * Subclasses override to handle collected values.
         *
         * @param name the attribute name
         * @param value the attribute value, or a wrapper for type values
         */
        abstract void visitAttribute(String name, Object value);

        /**
         * Visits a scalar attribute value, converting ASM Type objects to {@link TypeWrapper} instances.
         *
         * @param name the attribute name
         * @param value the attribute value or ASM {@link Type} object
         */
        @Override
        public void visit(final String name, final Object value) {
            visitAttribute(name, value instanceof Type ? new TypeWrapper(((Type) value).getDescriptor()) : value);
        }

        /**
         * Visits an enum constant attribute value.
         *
         * @param name the attribute name
         * @param desc the enum type descriptor
         * @param value the enum constant name
         */
        @Override
        public void visitEnum(final String name, final String desc, final String value) {
            visitAttribute(name, new EnumConstantWrapper(desc, value));
        }

        /**
         * Visits a nested annotation attribute value.
         *
         * @param name the attribute name
         * @param desc the annotation type descriptor
         * @return an annotation reader for the nested annotation
         */
        @Override
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            AnnotationStub stub = new AnnotationStub(desc);
            visitAttribute(name, stub);
            return readAnnotationMembers(stub);
        }

        /**
         * Visits an array attribute value, collecting elements in a list.
         *
         * @param name the attribute name
         * @return an annotation reader that appends elements to a list
         */
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
