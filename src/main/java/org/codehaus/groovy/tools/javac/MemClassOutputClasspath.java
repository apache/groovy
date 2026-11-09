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

import groovy.lang.GroovyClassLoader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publishes in-memory class bytes through a {@link GroovyClassLoader} so
 * one joint compilation unit can resolve them without a class-output directory.
 * <p>
 * {@link #open} always creates a new store and appends one URL. The
 * compilation unit keeps that store. A compile writes a private stage. The
 * stage becomes visible to {@code getResource} immediately, so later phases
 * can resolve classes javac just produced, and it is copied into the
 * committed map only after that unit reaches output without error. A failed
 * compile discards the stage and leaves classes this store already committed.
 * <p>
 * Stores are not retained across compilation units. {@code groovyc} and the
 * Ant task already build a new loader for each run, and compiling the same
 * classes again needs a new loader: a class the loader has already defined
 * cannot be replaced. Calling {@link #open} again on a loader appends another
 * URL. Which URL {@code getResource} returns is then the platform's search
 * order, not a cache defined here. Bytes are also hidden when the same
 * resource is already visible from a parent loader or from an earlier URL.
 * <p>
 * The protocol is not registered with the JVM. Only URLs created here can
 * be opened. Paths that escape the map are rejected. Building the URL does
 * not touch the filesystem or the network. A {@code SecurityManager}, still
 * possible on the Java 17 baseline, may require
 * {@code NetPermission("specifyStreamHandler")} for the handler.
 */
final class MemClassOutputClasspath {

    static final String PROTOCOL = "x-groovy-mem";

    private MemClassOutputClasspath() {
    }

    /**
     * Reports whether {@code url} was created for an in-memory class path.
     *
     * @param url a class-loader URL, or {@code null}
     * @return {@code true} when {@code url} uses this classpath's protocol
     */
    static boolean isInMemory(final URL url) {
        return url != null && PROTOCOL.equals(url.getProtocol());
    }

    /**
     * Appends an in-memory classpath URL to {@code loader} and returns the
     * store behind it. A later call appends another URL; it does not return
     * the previous store.
     *
     * @param loader the compilation unit's class loader
     * @return the new store
     */
    @SuppressWarnings("deprecation") // custom handler; URL.of(URI, handler) is not on the Java 17 baseline
    static Store open(final GroovyClassLoader loader) {
        Objects.requireNonNull(loader, "loader");
        String id = UUID.randomUUID().toString();
        Store created = new Store(id);
        try {
            // No host: a host would make URL.equals/hashCode resolve it as a DNS name.
            URL base = new URL(PROTOCOL, "", -1, "/" + id + "/", new Handler(created));
            loader.addURL(base);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not install in-memory class output", e);
        }
        return created;
    }

    /**
     * Committed class bytes for one compilation unit, plus at most one open stage.
     */
    static final class Store {
        private final String id;
        private final ConcurrentHashMap<String, byte[]> committed = new ConcurrentHashMap<>();
        private volatile ConcurrentHashMap<String, byte[]> staged;

        private Store(final String id) {
            this.id = id;
        }

        /**
         * Opens the stage, or returns the stage already open on this store.
         *
         * @return the stage map, keyed by {@code com/example/Type.class}
         */
        synchronized Map<String, byte[]> begin() {
            if (staged == null) {
                staged = new ConcurrentHashMap<>();
            }
            return staged;
        }

        /**
         * Copies the open stage into the committed map.
         * Names this compile did not produce are left as they were.
         * Does nothing when no stage is open.
         */
        synchronized void commit() {
            ConcurrentHashMap<String, byte[]> stage = staged;
            if (stage != null) {
                committed.putAll(stage);
            }
            staged = null;
        }

        /**
         * Drops the open stage. Committed classes are unchanged.
         * Does nothing when no stage is open.
         */
        synchronized void abort() {
            staged = null;
        }

        private byte[] read(final String resourceName) {
            ConcurrentHashMap<String, byte[]> stage = staged;
            if (stage != null) {
                byte[] stagedBytes = stage.get(resourceName);
                if (stagedBytes != null) {
                    return stagedBytes;
                }
            }
            return committed.get(resourceName);
        }
    }

    /**
     * Equals and hash code never resolve the host. The URL has an empty host
     * for the same reason: JDK 17's default implementation calls
     * {@code InetAddress.getByName}.
     */
    private static final class Handler extends URLStreamHandler {
        private final Store store;

        private Handler(final Store store) {
            this.store = store;
        }

        @Override
        protected URLConnection openConnection(final URL url) {
            return new Connection(url, store);
        }

        @Override
        protected InetAddress getHostAddress(final URL url) {
            return null;
        }

        /**
         * Compares hosts as strings. The default implementation resolves them.
         */
        @Override
        protected boolean hostsEqual(final URL left, final URL right) {
            return Objects.equals(left.getHost(), right.getHost());
        }
    }

    private static final class Connection extends URLConnection {
        private final Store store;
        private byte[] data;

        private Connection(final URL url, final Store store) {
            super(url);
            this.store = store;
        }

        @Override
        public void connect() throws IOException {
            if (connected) {
                return;
            }
            data = store.read(resourcePath(url, store.id));
            if (data == null) {
                throw new FileNotFoundException(String.valueOf(url));
            }
            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(data);
        }

        @Override
        public long getContentLengthLong() {
            try {
                connect();
            } catch (IOException e) {
                return -1L;
            }
            return data.length;
        }

        @Override
        public long getLastModified() {
            return 0L;
        }
    }

    /**
     * Resource name inside the map. {@link URI#getPath()} percent-decodes
     * without treating {@code +} as a space.
     */
    private static String resourcePath(final URL url, final String id) throws IOException {
        String path;
        try {
            path = url.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new FileNotFoundException(String.valueOf(url));
        }
        if (path == null || path.isEmpty()) {
            throw new FileNotFoundException(String.valueOf(url));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String prefix = id + "/";
        if (!path.startsWith(prefix)) {
            throw new FileNotFoundException(path);
        }
        path = path.substring(prefix.length());
        if (path.isEmpty() || path.charAt(0) == '/' || path.indexOf('\\') >= 0 || path.contains("..")) {
            throw new FileNotFoundException(path);
        }
        return path;
    }
}
