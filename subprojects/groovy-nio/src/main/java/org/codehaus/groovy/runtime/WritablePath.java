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
package org.codehaus.groovy.runtime;

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
 * A Writable Path.
 */

public class WritablePath implements Path, Writable {

    private final String encoding;
    private final Path delegate;

    public WritablePath(final Path delegate) {
        this(delegate, null);
    }

    public WritablePath(final Path delegate, final String encoding) {
        this.encoding = encoding;
        this.delegate = delegate;
    }

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

    @Override
    public FileSystem getFileSystem() {
        return delegate.getFileSystem();
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return delegate.getRoot();
    }

    @Override
    public Path getFileName() {
        return delegate.getFileName();
    }

    @Override
    public Path getParent() {
        return delegate.getParent();
    }

    @Override
    public int getNameCount() {
        return delegate.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return delegate.getName(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return delegate.subpath(beginIndex, endIndex);
    }

    @Override
    public boolean startsWith(Path other) {
        return delegate.startsWith(other);
    }

    @Override
    public boolean startsWith(String other) {
        return delegate.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return delegate.endsWith(other);
    }

    @Override
    public boolean endsWith(String other) {
        return delegate.endsWith(other);
    }

    @Override
    public Path normalize() {
        return delegate.normalize();
    }

    @Override
    public Path resolve(Path other) {
        return delegate.resolve(other);
    }

    @Override
    public Path resolve(String other) {
        return delegate.resolve(other);
    }

    @Override
    public Path resolveSibling(Path other) {
        return delegate.resolveSibling(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return delegate.resolveSibling(other);
    }

    @Override
    public Path relativize(Path other) {
        return delegate.relativize(other);
    }

    @Override
    public URI toUri() {
        return delegate.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        return delegate.toAbsolutePath();
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return delegate.toRealPath(options);
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return delegate.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return delegate.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        return delegate.iterator();
    }

    @Override
    public int compareTo(Path other) {
        return delegate.compareTo(other);
    }

    @Override
    public boolean equals(Object other) {
        return delegate.equals(other);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
