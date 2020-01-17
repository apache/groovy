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

import groovy.lang.GroovyRuntimeException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A shell for compiling or running pure Java code
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
     * @param src the source code
     * @param args arguments for main method
     * @throws Throwable
     */
    public void runMain(String className, String src, String... args) throws Throwable {
        Class<?> c = compile(className, src);
        Method mainMethod = c.getMethod(MAIN_METHOD_NAME, String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    /**
     * Compile and return the main class
     * @param className the main class name
     * @param src the source code
     * @return the main class
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Class<?> compile(final String className, String src) throws IOException, ClassNotFoundException {
        return compileAll(className, src).get(className);
    }

    /**
     * Compile and return all classes
     *
     * @param className the main class name
     * @param src the source code
     * @return all classes
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Map<String, Class<?>> compileAll(final String className, String src) throws IOException, ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (BytesJavaFileManager bjfm = new BytesJavaFileManager(compiler.getStandardFileManager(null, locale, charset))) {
            StringBuilderWriter out = new StringBuilderWriter();
            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            out,
                            bjfm,
                            null,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.singletonList(
                                    new MemJavaFileObject(className, src)
                            )
                    );

            task.call();

            if (bjfm.isEmpty()) {
                throw new GroovyRuntimeException(out.toString());
            }

            final Map<String, byte[]> classMap = bjfm.getClassMap();

            jscl.setClassMap(classMap);

            Map<String, Class<?>> classes = new LinkedHashMap<>();
            for (String cn : classMap.keySet()) {
                Class<?> c = jscl.findClass(cn);
                classes.put(cn, c);
            }

            return classes;
        }
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

        public JavaShellClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            final byte[] bytes = classMap.get(name);

            if (null != bytes) {
                return classCache.computeIfAbsent(name, n -> defineClass(n, bytes, 0, bytes.length));
            }

            return super.findClass(name);
        }

        public Map<String, byte[]> getClassMap() {
            return classMap;
        }

        public void setClassMap(Map<String, byte[]> classMap) {
            this.classMap = classMap;
        }
    }

    private static final class BytesJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, BytesJavaFileObject> fileObjectMap = new HashMap<>();
        private Map<String, byte[]> classMap;

        public BytesJavaFileManager(StandardJavaFileManager sjfm) {
            super(sjfm);
        }

        public boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }

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

        public Map<String, byte[]> getClassMap() {
            if (classMap != null) return classMap;

            classMap = new LinkedHashMap<>();
            fileObjectMap.forEach((key, value) -> classMap.put(key, value.getBytes()));

            return Collections.unmodifiableMap(classMap);
        }
    }

    private static class BytesJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public BytesJavaFileObject(String name, Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() {
            return baos;
        }

        public byte[] getBytes() {
            return baos.toByteArray();
        }
    }
}
