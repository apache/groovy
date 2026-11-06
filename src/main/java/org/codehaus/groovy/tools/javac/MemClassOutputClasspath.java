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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publishes in-memory class bytes through a {@link GroovyClassLoader} so
 * joint compilation can resolve them without a class-output directory.
 * <p>
 * The protocol is not registered with the JVM. Only URLs created here can
 * be opened, and each URL serves just the map it was given. Paths that
 * escape that map are rejected. No filesystem or network access is performed.
 */
final class MemClassOutputClasspath {

    private static final String PROTOCOL = "x-groovy-mem";

    private static final Map<GroovyClassLoader, Map<String, byte[]>> INSTALLED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private MemClassOutputClasspath() {
    }

    /**
     * Attaches an in-memory classpath to {@code loader}, or returns the map
     * already attached to that loader. A second compile on the same loader
     * therefore replaces entries in place and stays visible to
     * {@code getResource}.
     *
     * @param loader the compilation unit's class loader
     * @return the mutable resource map, keyed by {@code com/example/Type.class}
     */
    @SuppressWarnings("deprecation") // custom handler; URL.of(URI, handler) is not on the Java 17 baseline
    static Map<String, byte[]> install(final GroovyClassLoader loader) {
        Objects.requireNonNull(loader, "loader");
        synchronized (INSTALLED) {
            Map<String, byte[]> existing = INSTALLED.get(loader);
            if (existing != null) {
                return existing;
            }
            Map<String, byte[]> created = new ConcurrentHashMap<>();
            try {
                URL base = new URL(PROTOCOL, UUID.randomUUID().toString(), -1, "/", new Handler(created));
                loader.addURL(base);
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Could not install in-memory class output", e);
            }
            INSTALLED.put(loader, created);
            return created;
        }
    }

    private static final class Handler extends URLStreamHandler {
        private final Map<String, byte[]> resources;

        private Handler(final Map<String, byte[]> resources) {
            this.resources = resources;
        }

        @Override
        protected URLConnection openConnection(final URL url) {
            return new Connection(url, resources);
        }
    }

    private static final class Connection extends URLConnection {
        private final Map<String, byte[]> resources;
        private byte[] data;

        private Connection(final URL url, final Map<String, byte[]> resources) {
            super(url);
            this.resources = resources;
        }

        @Override
        public void connect() throws IOException {
            if (connected) {
                return;
            }
            String path = resourcePath(url);
            data = resources.get(path);
            if (data == null) {
                throw new FileNotFoundException(path);
            }
            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(data);
        }

        @Override
        public long getLastModified() {
            return 0L;
        }
    }

    private static String resourcePath(final URL url) throws IOException {
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            throw new FileNotFoundException(String.valueOf(url));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try {
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new FileNotFoundException(path);
        }
        if (path.isEmpty() || path.charAt(0) == '/' || path.indexOf('\\') >= 0 || path.contains("..")) {
            throw new FileNotFoundException(path);
        }
        return path;
    }
}
