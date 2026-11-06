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
import javax.tools.StandardLocation;
import java.io.IOException;
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
 * is ignored, and {@link StandardLocation#CLASS_OUTPUT} always resolves to a
 * {@link MemClassFileObject}. The manager does not perform network I/O.
 * <p>
 * The wrapped manager is closed when this manager is closed. Captured class
 * bytes remain available afterwards.
 *
 * @since 7.0.0
 */
public class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, MemClassFileObject> files = new ConcurrentHashMap<>();
    private final Map<String, byte[]> resourceSink;

    /**
     * Creates a manager that records class files only in this instance.
     *
     * @param fileManager the manager that resolves inputs and the classpath;
     *        typically the platform's standard file manager
     */
    public InMemoryJavaFileManager(final JavaFileManager fileManager) {
        this(fileManager, null);
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
    public InMemoryJavaFileManager(final JavaFileManager fileManager, final Map<String, byte[]> resourceSink) {
        super(Objects.requireNonNull(fileManager, "fileManager"));
        this.resourceSink = resourceSink;
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
        if (file == null) {
            return null;
        }
        byte[] bytes = file.getBytes();
        return bytes.length == 0 ? null : bytes;
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
            byte[] bytes = file.getBytes();
            if (bytes.length > 0) {
                copy.put(file.getBinaryName(), bytes);
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
                    .filter(file -> file.getBytes().length > 0 && inPackage(file.getBinaryName(), pkg, recurse))
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
            if (file != null && file.getBytes().length > 0) {
                return file;
            }
            return null;
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
            return files.computeIfAbsent(className, name -> new MemClassFileObject(name, resourceSink));
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    /**
     * Same as {@link #getJavaFileForOutput(Location, String, JavaFileObject.Kind, FileObject)}.
     * Overridden so class output is not forwarded to the wrapped manager on JDKs
     * that call the originating-files variant.
     *
     * @param location the output location
     * @param className the binary name
     * @param kind the kind of file
     * @param originatingFiles files that caused this output, ignored for class bytes
     * @return the file javac should write
     * @throws IOException if the wrapped manager fails for another location
     */
    @Override
    public JavaFileObject getJavaFileForOutputForOriginatingFiles(final Location location, final String className,
            final JavaFileObject.Kind kind, final FileObject... originatingFiles) throws IOException {
        FileObject sibling = originatingFiles == null || originatingFiles.length == 0 ? null : originatingFiles[0];
        return getJavaFileForOutput(location, className, kind, sibling);
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
            if (relativeName != null && relativeName.endsWith(JavaFileObject.Kind.CLASS.extension)) {
                return getJavaFileForOutput(location, binaryName(packageName, relativeName), JavaFileObject.Kind.CLASS, sibling);
            }
            throw new IOException("CLASS_OUTPUT is kept in memory and does not accept " + relativeName);
        }
        return super.getFileForOutput(location, packageName, relativeName, sibling);
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
            return location == StandardLocation.CLASS_OUTPUT && files.containsValue(memClass);
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
        if (packageName == null || packageName.isEmpty()) {
            return simple;
        }
        return packageName + "." + simple;
    }
}
