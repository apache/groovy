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
package org.codehaus.groovy.tools.javac;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File manager that keeps javac {@code CLASS_OUTPUT} on the heap.
 * <p>
 * Classpath, source path, and other input locations are forwarded to the
 * wrapped manager. Class files are never opened on the filesystem: {@code -d}
 * is ignored, and {@link StandardLocation#CLASS_OUTPUT} always resolves to an
 * in-memory class file. The manager does not perform network I/O.
 * <p>
 * On JDKs whose file manager asks for output with originating files, the
 * platform default forwards that call to {@link #getJavaFileForOutput}, which
 * this class handles. The wrapped manager is closed when this manager is closed.
 * Captured class bytes remain available afterwards.
 * <p>
 * This manager records javac's class files. When a resource sink is supplied,
 * each closed class file is copied there. Joint compilation uses that sink as
 * the compilation class loader's classpath and writes Groovy's own classes
 * into the sink directly, not back through this manager.
 * <p>
 * {@code -d} is ignored. Generated sources ({@link StandardLocation#SOURCE_OUTPUT})
 * are kept in memory as well, so annotation processing does not fall back to the
 * working directory. Module-oriented locations are not supported; joint
 * compilation here is not modular. Class files whose source sibling is one of
 * the stub file objects passed to the constructor are captured for javac but
 * not published.
 *
 * @since 7.0.0
 */
class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, MemClassFileObject> files = new ConcurrentHashMap<>();
    private final Map<String, MemSourceFileObject> sources = new ConcurrentHashMap<>();
    private final Map<String, byte[]> resourceSink;
    private final Set<FileObject> discardedSiblings;

    /**
     * Creates a manager that records class files only in this instance.
     *
     * @param fileManager the manager that resolves inputs and the classpath;
     *        typically the platform's standard file manager
     */
    InMemoryJavaFileManager(final JavaFileManager fileManager) {
        this(fileManager, null, null);
    }

    /**
     * Creates a manager that records class files in this instance and, when
     * {@code resourceSink} is non-null, publishes each class file there as
     * javac closes it.
     * <p>
     * The sink is keyed by resource name ({@code com/example/Outer$Inner.class}),
     * not by binary name. It must be mutable. Entries are replaced when the
     * same class is written again. This manager does not copy the map.
     *
     * @param fileManager the manager that resolves inputs and the classpath
     * @param resourceSink optional mutable map that receives class bytes; may be {@code null}
     */
    InMemoryJavaFileManager(final JavaFileManager fileManager, final Map<String, byte[]> resourceSink) {
        this(fileManager, resourceSink, null);
    }

    /**
     * Creates a manager that publishes class files and other class-output
     * resources into {@code resourceSink}, except class files whose
     * {@code sibling} is in {@code discardedSiblings}. Comparison of siblings
     * is by identity, which is how javac passes the stub source it compiled.
     *
     * @param fileManager the manager that resolves inputs and the classpath
     * @param resourceSink optional mutable map that receives published bytes; may be {@code null}
     * @param discardedSiblings stub sources whose class files must not be published; may be {@code null}
     */
    InMemoryJavaFileManager(final JavaFileManager fileManager, final Map<String, byte[]> resourceSink,
            final Set<? extends FileObject> discardedSiblings) {
        super(Objects.requireNonNull(fileManager, "fileManager"));
        this.resourceSink = resourceSink;
        Set<FileObject> stubs = Collections.newSetFromMap(new IdentityHashMap<>());
        if (discardedSiblings != null) {
            stubs.addAll(discardedSiblings);
        }
        this.discardedSiblings = stubs;
    }

    /**
     * Returns the resource name for a binary class name.
     *
     * @param binaryName a JVMS binary name such as {@code com.example.Outer$Inner}
     * @return {@code com/example/Outer$Inner.class}
     * @throws IllegalArgumentException if {@code binaryName} is null, empty, or contains {@code ..}
     */
    static String resourceName(final String binaryName) {
        if (binaryName == null || binaryName.isEmpty()
                || binaryName.indexOf('\\') >= 0
                || binaryName.contains("..")) {
            throw new IllegalArgumentException("Invalid binary name: " + binaryName);
        }
        return binaryName.replace('.', '/') + JavaFileObject.Kind.CLASS.extension;
    }

    /**
     * Returns a copy of the bytes javac wrote for {@code binaryName}.
     *
     * @param binaryName the JVMS binary name
     * @return the class bytes, or {@code null} if that class was not written
     */
    public byte[] getClassBytes(final String binaryName) {
        MemClassFileObject file = files.get(Objects.requireNonNull(binaryName, "binaryName"));
        if (file == null || file.isEmpty()) {
            return null;
        }
        return file.getBytes();
    }

    /**
     * Returns a copy of every class file captured by this manager, keyed by
     * binary name. Classes that were opened but never written are omitted.
     * Later writes are not reflected in the returned map.
     *
     * @return an unmodifiable map of binary name to class bytes
     */
    public Map<String, byte[]> getClassBytes() {
        Map<String, byte[]> copy = new ConcurrentHashMap<>();
        for (MemClassFileObject file : files.values()) {
            if (!file.isEmpty()) {
                copy.put(file.getBinaryName(), file.getBytes());
            }
        }
        return Map.copyOf(copy);
    }

    /**
     * Reports that class output is available even though it has no directory.
     *
     * @param location the location to query
     * @return {@code true} for {@link StandardLocation#CLASS_OUTPUT}, otherwise
     *         the wrapped manager's answer
     */
    @Override
    public boolean hasLocation(final Location location) {
        if (location == StandardLocation.CLASS_OUTPUT) {
            return true;
        }
        return super.hasLocation(location);
    }

    /**
     * Ignores {@code -d}. Class files are not written to a directory.
     * The directory argument, when present, is consumed and discarded.
     *
     * @param current the option name
     * @param remaining the remaining options
     * @return {@code true} when the option was recognized
     */
    @Override
    public boolean handleOption(final String current, final Iterator<String> remaining) {
        if ("-d".equals(current)) {
            if (remaining.hasNext()) {
                remaining.next();
            }
            return true;
        }
        return super.handleOption(current, remaining);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader(final Location location) {
        if (location == StandardLocation.CLASS_OUTPUT) {
            return null;
        }
        return super.getClassLoader(location);
    }

    /**
     * Lists class files captured in memory when {@code location} is class output.
     *
     * @param location the location to search
     * @param packageName the package to list, or {@code ""} for the default package
     * @param kinds the kinds of file to include
     * @param recurse whether to include subpackages
     * @return the matching file objects
     * @throws IOException if the wrapped manager fails for an input location
     */
    @Override
    public Iterable<JavaFileObject> list(final Location location, final String packageName,
            final Set<JavaFileObject.Kind> kinds, final boolean recurse) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            if (kinds == null || !kinds.contains(JavaFileObject.Kind.CLASS)) {
                return List.of();
            }
            String pkg = packageName == null ? "" : packageName;
            return files.values().stream()
                    .filter(file -> !file.isEmpty() && inPackage(file.getBinaryName(), pkg, recurse))
                    .map(file -> (JavaFileObject) file)
                    .toList();
        }
        return super.list(location, packageName, kinds, recurse);
    }

    /**
     * Returns the binary name of an in-memory class file, or delegates.
     *
     * @param location the location containing the file
     * @param file the file object
     * @return the binary name
     */
    @Override
    public String inferBinaryName(final Location location, final JavaFileObject file) {
        if (file instanceof MemClassFileObject memClass) {
            return memClass.getBinaryName();
        }
        return super.inferBinaryName(location, file);
    }

    /**
     * Compares two file objects, treating in-memory class files by binary name.
     *
     * @param a the first file
     * @param b the second file
     * @return {@code true} if they represent the same file
     */
    @Override
    public boolean isSameFile(final FileObject a, final FileObject b) {
        if (a instanceof MemClassFileObject || b instanceof MemClassFileObject) {
            return a.equals(b);
        }
        return super.isSameFile(a, b);
    }

    /**
     * Returns a previously written class file when {@code location} is class output.
     *
     * @param location the location to read from
     * @param className the binary name
     * @param kind the kind of file
     * @return the class file, or {@code null} if it has not been written
     * @throws IOException if the wrapped manager fails for an input location
     */
    @Override
    public JavaFileObject getJavaFileForInput(final Location location, final String className,
            final JavaFileObject.Kind kind) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT && kind == JavaFileObject.Kind.CLASS) {
            MemClassFileObject file = files.get(className);
            if (file != null && !file.isEmpty()) {
                return file;
            }
            return null;
        }
        if (location == StandardLocation.SOURCE_OUTPUT && kind == JavaFileObject.Kind.SOURCE) {
            MemSourceFileObject source = sources.get(className);
            return source == null || source.isEmpty() ? null : source;
        }
        return super.getJavaFileForInput(location, className, kind);
    }

    /**
     * Returns an in-memory class file for {@link StandardLocation#CLASS_OUTPUT}.
     * Other locations are forwarded.
     *
     * @param location the output location
     * @param className the binary name
     * @param kind the kind of file
     * @param sibling a source sibling, ignored for in-memory class output
     * @return the file javac should write
     * @throws IOException if the wrapped manager fails for another location
     */
    @Override
    public JavaFileObject getJavaFileForOutput(final Location location, final String className,
            final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT && kind == JavaFileObject.Kind.CLASS) {
            Map<String, byte[]> sink = sibling != null && discardedSiblings.contains(sibling) ? null : resourceSink;
            return files.computeIfAbsent(className, name -> new MemClassFileObject(name, sink));
        }
        if (location == StandardLocation.SOURCE_OUTPUT && kind == JavaFileObject.Kind.SOURCE) {
            return sources.computeIfAbsent(className, MemSourceFileObject::new);
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    /**
     * Redirects {@code .class} files requested from class output into memory.
     * Other class-output names are rejected so they cannot fall through to a directory.
     *
     * @param location the output location
     * @param packageName the package, or {@code ""} for the default package
     * @param relativeName the file name relative to the package
     * @param sibling a source sibling
     * @return the in-memory class file
     * @throws IOException if {@code location} is class output and {@code relativeName}
     *         is not a class file, or if the wrapped manager fails
     */
    @Override
    public FileObject getFileForOutput(final Location location, final String packageName,
            final String relativeName, final FileObject sibling) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            if (relativeName == null) {
                throw new IOException("CLASS_OUTPUT is kept in memory and does not accept a null name");
            }
            if (relativeName.endsWith(JavaFileObject.Kind.CLASS.extension)) {
                return getJavaFileForOutput(location, binaryName(packageName, relativeName), JavaFileObject.Kind.CLASS, sibling);
            }
            String resource = resourceKey(packageName, relativeName);
            return new MemClassFileObject(resource, resource, resourceSink, JavaFileObject.Kind.OTHER);
        }
        return super.getFileForOutput(location, packageName, relativeName, sibling);
    }

    /**
     * Reads a class-output resource back from the sink.
     * Processors that call {@code Filer.getResource} land here.
     */
    @Override
    public FileObject getFileForInput(final Location location, final String packageName, final String relativeName) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            if (resourceSink == null || relativeName == null) {
                return null;
            }
            String resource = resourceKey(packageName, relativeName);
            byte[] bytes = resourceSink.get(resource);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            MemClassFileObject file = new MemClassFileObject(resource, resource, null, JavaFileObject.Kind.OTHER);
            file.openOutputStream().write(bytes);
            return file;
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    /**
     * Reports whether {@code fileObject} is a class file captured by this manager.
     *
     * @param location the location to test
     * @param fileObject the file
     * @return {@code true} if this manager holds that class file in the given location
     * @throws IOException if the wrapped manager fails
     */
    @Override
    public boolean contains(final Location location, final FileObject fileObject) throws IOException {
        if (fileObject instanceof MemClassFileObject memClass) {
            return location == StandardLocation.CLASS_OUTPUT && files.get(memClass.getBinaryName()) == memClass;
        }
        if (location == StandardLocation.CLASS_OUTPUT) {
            return false;
        }
        return super.contains(location, fileObject);
    }

    /**
     * Publishes captured class bytes to the resource sink, then flushes the wrapped manager.
     *
     * @throws IOException if the wrapped manager fails to flush
     */
    @Override
    public void flush() throws IOException {
        for (MemClassFileObject file : files.values()) {
            file.publish();
        }
        super.flush();
    }

    /**
     * Publishes captured class bytes and closes the wrapped manager.
     * Bytes already captured remain readable.
     *
     * @throws IOException if the wrapped manager fails to close
     */
    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }

    private static boolean inPackage(final String binaryName, final String packageName, final boolean recurse) {
        String pkg = packageOf(binaryName);
        if (recurse) {
            return packageName.isEmpty() || pkg.equals(packageName) || pkg.startsWith(packageName + ".");
        }
        return pkg.equals(packageName);
    }

    private static String packageOf(final String binaryName) {
        int dot = binaryName.lastIndexOf('.');
        return dot < 0 ? "" : binaryName.substring(0, dot);
    }

    private static String binaryName(final String packageName, final String relativeName) {
        String simple = relativeName.substring(0, relativeName.length() - JavaFileObject.Kind.CLASS.extension.length());
        simple = simple.replace('/', '.');
        if (packageName == null || packageName.isEmpty()) {
            return simple;
        }
        return packageName + "." + simple;
    }

    private static String resourceKey(final String packageName, final String relativeName) {
        String relative = relativeName == null ? "" : relativeName.replace('\\', '/');
        if (packageName == null || packageName.isEmpty()) {
            return relative;
        }
        return packageName.replace('.', '/') + "/" + relative;
    }

    /**
     * Generated Java source kept on the heap so a later annotation-processing
     * round can read it back without a directory.
     */
    private static final class MemSourceFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        private MemSourceFileObject(final String className) {
            super(URI.create("mem:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        }

        private boolean isEmpty() {
            return buffer.size() == 0;
        }

        @Override
        public OutputStream openOutputStream() {
            buffer.reset();
            return buffer;
        }

        @Override
        public Writer openWriter() {
            return new OutputStreamWriter(openOutputStream(), StandardCharsets.UTF_8);
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
