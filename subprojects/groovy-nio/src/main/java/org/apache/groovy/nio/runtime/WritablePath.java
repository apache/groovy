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
package org.apache.groovy.nio.runtime;

import groovy.lang.Writable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * {@link Path} wrapper that also implements {@link Writable}.
 * <p>
 * All {@code Path} operations are delegated to the wrapped path, while
 * {@link #writeTo(Writer)} streams the path contents using the configured
 * character encoding when one is supplied.
 */
public class WritablePath implements Path, Writable {

    private final String encoding;
    private final Path delegate;

    /**
     * Creates a writable wrapper that uses the platform default charset when
     * reading the path contents.
     *
     * @param delegate the path to wrap
     */
    public WritablePath(final Path delegate) {
        this(delegate, null);
    }

    /**
     * Creates a writable wrapper for the supplied path.
     *
     * @param delegate the path to wrap
     * @param encoding the charset name to use when reading path contents;
     *     {@code null} uses the platform default charset
     */
    public WritablePath(final Path delegate, final String encoding) {
        this.encoding = encoding;
        this.delegate = delegate;
    }

    /**
     * Writes the wrapped path contents to the supplied writer.
     *
     * @param out the destination writer
     * @return the supplied writer
     * @throws IOException if the path cannot be read or the writer cannot be updated
     */
    @Override
    public Writer writeTo(final Writer out) throws IOException {

        try (Reader reader = (this.encoding == null)
                ? new InputStreamReader(Files.newInputStream(this))
                : new InputStreamReader(Files.newInputStream(this), Charset.forName(this.encoding))) {
            int c = reader.read();

            while (c != -1) {
                out.write(c);
                c = reader.read();
            }
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public FileSystem getFileSystem() {
        return delegate.getFileSystem();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    /** {@inheritDoc} */
    @Override
    public Path getRoot() {
        return delegate.getRoot();
    }

    /** {@inheritDoc} */
    @Override
    public Path getFileName() {
        return delegate.getFileName();
    }

    /** {@inheritDoc} */
    @Override
    public Path getParent() {
        return delegate.getParent();
    }

    /** {@inheritDoc} */
    @Override
    public int getNameCount() {
        return delegate.getNameCount();
    }

    /** {@inheritDoc} */
    @Override
    public Path getName(int index) {
        return delegate.getName(index);
    }

    /** {@inheritDoc} */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return delegate.subpath(beginIndex, endIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean startsWith(Path other) {
        return delegate.startsWith(other);
    }

    /** {@inheritDoc} */
    @Override
    public boolean startsWith(String other) {
        return delegate.startsWith(other);
    }

    /** {@inheritDoc} */
    @Override
    public boolean endsWith(Path other) {
        return delegate.endsWith(other);
    }

    /** {@inheritDoc} */
    @Override
    public boolean endsWith(String other) {
        return delegate.endsWith(other);
    }

    /** {@inheritDoc} */
    @Override
    public Path normalize() {
        return delegate.normalize();
    }

    /** {@inheritDoc} */
    @Override
    public Path resolve(Path other) {
        return delegate.resolve(other);
    }

    /** {@inheritDoc} */
    @Override
    public Path resolve(String other) {
        return delegate.resolve(other);
    }

    /** {@inheritDoc} */
    @Override
    public Path resolveSibling(Path other) {
        return delegate.resolveSibling(other);
    }

    /** {@inheritDoc} */
    @Override
    public Path resolveSibling(String other) {
        return delegate.resolveSibling(other);
    }

    /** {@inheritDoc} */
    @Override
    public Path relativize(Path other) {
        return delegate.relativize(other);
    }

    /** {@inheritDoc} */
    @Override
    public URI toUri() {
        return delegate.toUri();
    }

    /** {@inheritDoc} */
    @Override
    public Path toAbsolutePath() {
        return delegate.toAbsolutePath();
    }

    /** {@inheritDoc} */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return delegate.toRealPath(options);
    }

    /** {@inheritDoc} */
    @Override
    public File toFile() {
        return delegate.toFile();
    }

    /** {@inheritDoc} */
    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return delegate.register(watcher, events, modifiers);
    }

    /** {@inheritDoc} */
    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return delegate.register(watcher, events);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Path> iterator() {
        return delegate.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Path other) {
        return delegate.compareTo(other);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        return delegate.equals(other);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return delegate.toString();
    }
}
