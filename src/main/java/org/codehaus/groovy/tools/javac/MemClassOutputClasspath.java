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

import org.codehaus.groovy.control.CompilerConfiguration;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publishes in-memory class bytes through a {@link GroovyClassLoader} so
 * one joint compilation unit can resolve them without a class-output directory.
 * <p>
 * {@link #open} on an ordinary loader always creates a new store and appends
 * one URL. The compilation unit keeps that store. A compile writes a private
 * stage. The stage becomes visible to {@code getResource} immediately, so
 * later phases can resolve classes javac just produced, and it is copied into
 * the committed map only after that unit reaches output without error. A
 * failed compile discards the stage and leaves classes this store already
 * committed. Calling {@link #open} again on that loader appends another URL.
 * Which URL {@code getResource} returns is then the platform's search order.
 * Bytes are also hidden when the same resource is already visible from a
 * parent loader or from an earlier URL.
 * <p>
 * A loader from {@link MemClassOutputPublication} is different. Its store is
 * still appended when the unit compiles, after the unit has had a chance to
 * put dependency URLs on the loader, and it is not appended to the long-lived
 * parent. A name this compilation produced is read from its stage, then from
 * its committed map. A miss falls through to bytes the host has published,
 * so an overlapping name uses this compilation and a name it did not produce
 * stays visible. A second {@link #open} on that loader is rejected: two units
 * must not share one stage. There is no loader-identity cache.
 * <p>
 * {@code groovyc} and the Ant task build a new ordinary loader per run and do
 * not publish. A class a loader has already defined cannot be replaced.
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
     * store behind it.
     * <p>
     * On an ordinary loader a later call appends another URL and returns that
     * new store. On a {@link CompilationLoader} the first call appends one URL
     * whose reads fall through to the publication, and a later call is rejected.
     *
     * @param loader the compilation unit's class loader
     * @return the store for this compilation
     */
    static Store open(final GroovyClassLoader loader) {
        Objects.requireNonNull(loader, "loader");
        if (loader instanceof CompilationLoader compilation) {
            synchronized (compilation) {
                if (compilation.ownStore() != null) {
                    throw new IllegalStateException("this compilation loader is already in use");
                }
                Store created = install(loader, compilation.publishedOutput());
                compilation.remember(created);
                return created;
            }
        }
        return install(loader, null);
    }

    static PublishedOutput newPublishedOutput() {
        return new PublishedOutput();
    }

    /**
     * One compilation loader. The long-lived parent is {@code parent}.
     * In-memory bytes are appended to this loader, not to {@code parent}.
     */
    static GroovyClassLoader newCompilationLoader(final ClassLoader parent, final CompilerConfiguration configuration,
                                                  final PublishedOutput published) {
        return new CompilationLoader(parent, configuration, published);
    }

    /**
     * Appends one URL. {@code published} is consulted only after this store misses.
     */
    @SuppressWarnings("deprecation") // custom handler; URL.of(URI, handler) is not on the Java 17 baseline
    private static Store install(final GroovyClassLoader loader, final PublishedOutput published) {
        String id = UUID.randomUUID().toString();
        Store created = new Store(id, published);
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
     * {@code published} is the host's earlier output, or {@code null} when this
     * store stands alone.
     */
    static final class Store {
        private final String id;
        private final PublishedOutput published;
        private final ConcurrentHashMap<String, byte[]> committed = new ConcurrentHashMap<>();
        private volatile ConcurrentHashMap<String, byte[]> staged;

        private Store(final String id, final PublishedOutput published) {
            this.id = id;
            this.published = published;
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

        /**
         * Copies committed bytes into {@code target}. The open stage is not
         * copied. Each array is cloned so a later compile on this store cannot
         * change bytes already published.
         *
         * @param target the publication that later compilation loaders read
         */
        void copyCommittedInto(final PublishedOutput target) {
            Map<String, byte[]> snapshot;
            synchronized (this) {
                snapshot = new HashMap<>(committed.size());
                for (Map.Entry<String, byte[]> entry : committed.entrySet()) {
                    snapshot.put(entry.getKey(), entry.getValue().clone());
                }
            }
            target.merge(snapshot);
        }

        private byte[] read(final String resourceName) {
            ConcurrentHashMap<String, byte[]> stage = staged;
            if (stage != null) {
                byte[] stagedBytes = stage.get(resourceName);
                if (stagedBytes != null) {
                    return stagedBytes;
                }
            }
            byte[] committedBytes = committed.get(resourceName);
            if (committedBytes != null) {
                return committedBytes;
            }
            PublishedOutput prior = published;
            if (prior == null) {
                return null;
            }
            return prior.get(resourceName);
        }

        /**
         * Classes a reader of this store can see. The open stage hides a
         * committed class of the same name, and a committed class hides one
         * published by an earlier compilation. The arrays are the live map
         * entries and must not be modified.
         *
         * @return a snapshot of resource name to class bytes
         */
        Map<String, byte[]> visible() {
            Map<String, byte[]> view = new HashMap<>();
            PublishedOutput prior = published;
            if (prior != null) {
                view.putAll(prior.snapshot());
            }
            synchronized (this) {
                view.putAll(committed);
                ConcurrentHashMap<String, byte[]> stage = staged;
                if (stage != null) {
                    view.putAll(stage);
                }
            }
            return view;
        }
    }

    /**
     * Bytes a host has published for later compilations. Readers see the map
     * from before a {@link #merge} or the map from after it, not a mix of the
     * two. The map is not installed as a URL on the long-lived parent, so
     * that parent does not define these classes.
     */
    static final class PublishedOutput {
        private volatile Map<String, byte[]> bytes = Map.of();

        private PublishedOutput() {
        }

        byte[] get(final String resourceName) {
            return bytes.get(resourceName);
        }

        /**
         * The map readers currently see. The map is not modified again;
         * {@link #merge} replaces it.
         */
        Map<String, byte[]> snapshot() {
            return bytes;
        }

        void merge(final Map<String, byte[]> incoming) {
            if (incoming.isEmpty()) {
                return;
            }
            synchronized (this) {
                Map<String, byte[]> next = new HashMap<>(bytes);
                next.putAll(incoming);
                bytes = Map.copyOf(next);
            }
        }
    }

    /**
     * Defines one compilation's classes. {@link #addURL} stays on this loader,
     * unlike {@link GroovyClassLoader.InnerLoader}, whose {@code addURL} and
     * {@code getResource} delegate to the outer loader.
     */
    static final class CompilationLoader extends GroovyClassLoader {
        private final PublishedOutput published;
        private volatile Store own;

        private CompilationLoader(final ClassLoader parent, final CompilerConfiguration configuration,
                                  final PublishedOutput published) {
            super(parent, configuration, true);
            this.published = published;
        }

        PublishedOutput publishedOutput() {
            return published;
        }

        Store ownStore() {
            return own;
        }

        void remember(final Store store) {
            own = store;
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
                throw missing(String.valueOf(url));
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
            throw missing(String.valueOf(url));
        }
        if (path == null || path.isEmpty()) {
            throw missing(String.valueOf(url));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String prefix = id + "/";
        if (!path.startsWith(prefix)) {
            throw missing(path);
        }
        path = path.substring(prefix.length());
        if (path.isEmpty() || path.charAt(0) == '/' || path.indexOf('\\') >= 0 || path.contains("..")) {
            throw missing(path);
        }
        return path;
    }

    /**
     * A miss during a speculative lookup. Resolvers ask for many names that
     * are not present, and filling in the stack is the expensive part.
     * Callers only need the exception type and the path.
     */
    private static final class NotFound extends FileNotFoundException {
        private NotFound(final String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static FileNotFoundException missing(final String path) {
        return new NotFound(path);
    }
}
