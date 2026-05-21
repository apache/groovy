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
package org.apache.groovy.util;

import org.apache.groovy.io.StringBuilderWriter;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.javac.MemJavaFileObject;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A shell for compiling or running pure Java code in-memory using the platform's
 * {@link javax.tools.JavaCompiler}. Compiled bytes are kept in a backing class loader
 * (see {@link #getClassLoader()}) and can optionally be written to disk via
 * {@link #compileAllTo(String, Iterable, String, Path)}.
 *
 * <p>The {@code className} argument supplied to {@code run}, {@code compile},
 * {@code compileAll} and {@code compileAllTo} must be the fully-qualified binary name
 * and match any {@code package} declaration in {@code src}: if the source begins
 * with {@code package com.example;} and declares class {@code Foo}, pass
 * {@code "com.example.Foo"}, not {@code "Foo"}. A mismatch produces a standard
 * {@code javac} diagnostic ("class X is public, should be declared in a file named X.java").
 * Source with no {@code package} declaration takes a simple name (e.g. {@code "Foo"});
 * {@code compileAllTo} writes such a class directly under {@code outputDir} as
 * {@code Foo.class}, while packaged classes land under the matching subdirectory
 * (e.g. {@code <outputDir>/com/example/Foo.class}).
 *
 * <p>The {@code Iterable<String>} compiler-options parameter accepted by {@code run},
 * {@code compile}, {@code compileAll} and {@code compileAllTo} uses the same flags as
 * the {@code javac} command line, with one token per element (so {@code "-source"} and
 * {@code "17"} are two entries, not a single {@code "-source 17"} string). Examples:
 * <ul>
 *   <li>{@code List.of("--release", "17")} — target a specific Java release</li>
 *   <li>{@code List.of("-source", "17", "-target", "17")} — separate source/target levels</li>
 *   <li>{@code List.of("-parameters")} — retain formal parameter names</li>
 *   <li>{@code List.of("-g")} — emit debug information</li>
 *   <li>{@code List.of("-Xlint:all", "-Werror")} — enable all warnings and treat them as errors</li>
 *   <li>{@code List.of("-proc:none")} — disable annotation processing</li>
 *   <li>{@code List.of("-classpath", extraJars)} — extend the compile-time class path</li>
 * </ul>
 * The {@code -d} flag has no effect: in-memory output is always captured (and is
 * additionally written to disk by {@code compileAllTo}). See the {@code javac} documentation
 * for the complete list of supported flags.
 */
@Incubating
public class JavaShell {
    private static final String MAIN_METHOD_NAME = "main";
    private static final URL[] EMPTY_URL_ARRAY = new URL[0];
    private final JavaShellClassLoader jscl;
    private final Locale locale;
    private final Charset charset;

    /**
     * Initializes a newly created {@code JavaShell} object
     */
    public JavaShell() {
        this(null);
    }

    /**
     * Initializes a newly created {@code JavaShell} object
     *
     * @param parentClassLoader the parent class loader for delegation
     */
    public JavaShell(ClassLoader parentClassLoader) {
        jscl = new JavaShellClassLoader(
                EMPTY_URL_ARRAY,
                null == parentClassLoader
                        ? JavaShell.class.getClassLoader()
                        : parentClassLoader
        );

        locale = Locale.ENGLISH;
        charset = Charset.forName(CompilerConfiguration.DEFAULT.getSourceEncoding());
    }

    /**
     * Run main method
     *
     * @param className the main class name
     * @param options compiler options
     * @param src the source code
     * @param args arguments for main method
     */
    public void run(String className, Iterable<String> options, String src, String... args) throws Throwable {
        Class<?> c = compile(className, options, src);
        Method mainMethod = c.getMethod(MAIN_METHOD_NAME, String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    /**
     * Run main method
     *
     * @param className the main class name
     * @param src the source code
     * @param args arguments for main method
     */
    public void run(String className, String src, String... args) throws Throwable {
        run(className, Collections.emptyList(), src, args);
    }

    /**
     * Compile and return the main class
     * @param className the main class name
     * @param options compiler options
     * @param src the source code
     * @return the main class
     */
    public Class<?> compile(final String className, Iterable<String> options, String src) throws IOException, ClassNotFoundException {
        doCompile(className, src, options);

        return jscl.findClass(className);
    }

    /**
     * Compile and return the main class
     * @param className the main class name
     * @param src the source code
     * @return the main class
     */
    public Class<?> compile(final String className, String src) throws IOException, ClassNotFoundException {
        return compile(className, Collections.emptyList(), src);
    }

    /**
     * Compile and return all classes
     *
     * @param className the main class name
     * @param options compiler options
     * @param src the source code
     * @return all classes
     */
    public Map<String, Class<?>> compileAll(final String className, Iterable<String> options, String src) throws IOException, ClassNotFoundException {
        doCompile(className, src, options);

        Map<String, Class<?>> classes = new LinkedHashMap<>();
        for (String cn : jscl.getClassMap().keySet()) {
            Class<?> c = jscl.findClass(cn);
            classes.put(cn, c);
        }

        return classes;
    }

    /**
     * Compile and return all classes
     *
     * @param className the main class name
     * @param src the source code
     * @return all classes
     */
    public Map<String, Class<?>> compileAll(final String className, String src) throws IOException, ClassNotFoundException {
        return compileAll(className, Collections.emptyList(), src);
    }

    private void doCompile(String className, String src, Iterable<String> options) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (BytesJavaFileManager bjfm = new BytesJavaFileManager(compiler.getStandardFileManager(null, locale, charset))) {
            StringBuilderWriter out = new StringBuilderWriter();
            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            out,
                            bjfm,
                            null,
                            options,
                            Collections.emptyList(),
                            Collections.singletonList(
                                    new MemJavaFileObject(className, src)
                            )
                    );

            task.call();

            if (bjfm.isEmpty()) {
                throw new JavaShellCompilationException(out.toString());
            }

            final Map<String, byte[]> classMap = bjfm.getClassMap();

            jscl.setClassMap(classMap);
        }
    }

    /**
     * Compile {@code src} and write each resulting class file beneath {@code outputDir}
     * as a standard package directory tree (e.g. {@code com.example.Foo$Bar} becomes
     * {@code <outputDir>/com/example/Foo$Bar.class}). Intermediate directories are
     * created as needed; existing class files at those locations are overwritten.
     * The compiled classes also remain available through {@link #getClassLoader()}.
     *
     * @param className the main class name (binary name, e.g. {@code com.example.Foo})
     * @param options   compiler options; see the
     *                  {@linkplain JavaShell class-level documentation} for the expected format
     * @param src       the source code
     * @param outputDir root directory under which class files are written; created if absent
     * @return a map from binary class name to the {@link Path} of the written
     *         {@code .class} file, iterating in the order the compiler emitted them
     *         (stable across invocations for the same source, but JDK-dependent —
     *         inner and auxiliary classes commonly appear before the enclosing class)
     * @throws IOException                   if writing a class file fails
     * @throws JavaShellCompilationException if the source fails to compile
     * @since 6.0.0
     */
    public Map<String, Path> compileAllTo(String className, Iterable<String> options,
                                           String src, Path outputDir)
        throws IOException {
        doCompile(className, src, options);                 // populates jscl's classMap

        Map<String, byte[]> classes = jscl.getClassMap();   // already public internally
        Map<String, Path> written = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> e : classes.entrySet()) {
            // binary name -> relative path: com.example.Foo$Bar -> com/example/Foo$Bar.class
            Path target = outputDir.resolve(e.getKey().replace('.', '/') + ".class");
            Files.createDirectories(target.getParent() == null ? outputDir : target.getParent());
            Files.write(target, e.getValue());              // CREATE + TRUNCATE_EXISTING by default
            written.put(e.getKey(), target);
        }
        return written;
    }

    /**
     * Convenience overload of {@link #compileAllTo(String, Iterable, String, Path)} that
     * compiles with no extra compiler options.
     *
     * @param className the main class name (binary name)
     * @param src       the source code
     * @param outputDir root directory under which class files are written; created if absent
     * @return a map from binary class name to the {@link Path} of the written {@code .class} file
     * @throws IOException                   if writing a class file fails
     * @throws JavaShellCompilationException if the source fails to compile
     * @since 6.0.0
     */
    public Map<String, Path> compileAllTo(String className, String src, Path outputDir)
        throws IOException {
        return compileAllTo(className, Collections.emptyList(), src, outputDir);
    }

    /**
     * When and only when {@link #compile(String, String)} or {@link #compileAll(String, String)} is invoked,
     * returned class loader will reference the compiled classes.
     */
    public JavaShellClassLoader getClassLoader() {
        return jscl;
    }

    private static final class JavaShellClassLoader extends URLClassLoader {
        private Map<String, byte[]> classMap = Collections.emptyMap();
        private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

        /**
         * Creates a class loader for compiled JavaShell classes.
         *
         * @param urls the class path URLs
         * @param parent the parent class loader
         */
        public JavaShellClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            final byte[] bytes = classMap.get(name);

            if (null != bytes) {
                return classCache.computeIfAbsent(name, n -> defineClass(n, bytes, 0, bytes.length));
            }

            return super.findClass(name);
        }

        /**
         * Returns the compiled class bytes keyed by class name.
         *
         * @return the compiled class bytes
         */
        public Map<String, byte[]> getClassMap() {
            return classMap;
        }

        /**
         * Replaces the compiled class bytes available to this loader.
         *
         * @param classMap the compiled class bytes keyed by class name
         */
        public void setClassMap(Map<String, byte[]> classMap) {
            this.classMap = classMap;
        }
    }

    private static final class BytesJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, BytesJavaFileObject> fileObjectMap = new LinkedHashMap<>();
        private Map<String, byte[]> classMap;

        /**
         * Creates a file manager that stores compiled class bytes in memory.
         *
         * @param sjfm the standard file manager to delegate to
         */
        public BytesJavaFileManager(StandardJavaFileManager sjfm) {
            super(sjfm);
        }

        /**
         * Returns whether no compiled classes have been captured yet.
         *
         * @return {@code true} when no class output has been recorded
         */
        public boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public JavaFileObject getJavaFileForOutput(
                JavaFileManager.Location location,
                String className,
                JavaFileObject.Kind kind,
                FileObject sibling) {
            BytesJavaFileObject bjfo = new BytesJavaFileObject(className, kind);
            fileObjectMap.put(className, bjfo);
            return bjfo;
        }

        /**
         * Returns the compiled class bytes captured by this file manager.
         *
         * @return an immutable view of the compiled class bytes
         */
        public Map<String, byte[]> getClassMap() {
            if (classMap != null) return classMap;

            classMap = new LinkedHashMap<>();
            fileObjectMap.forEach((key, value) -> classMap.put(key, value.getBytes()));

            return Collections.unmodifiableMap(classMap);
        }
    }

    private static class BytesJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /**
         * Creates an in-memory class file for the supplied class name.
         *
         * @param name the binary class name
         * @param kind the file object kind
         */
        public BytesJavaFileObject(String name, Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        /** {@inheritDoc} */
        @Override
        public OutputStream openOutputStream() {
            return baos;
        }

        /**
         * Returns the bytes written to this class file.
         *
         * @return the compiled class bytes
         */
        public byte[] getBytes() {
            return baos.toByteArray();
        }
    }
}
