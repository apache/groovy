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
package org.codehaus.groovy.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utilities for opening URL connections and reading last-modified times
 * with Groovy-specific defaults.
 * <p>
 * Caching is disabled so {@code file:} and {@code jar:} URLs do not pin a
 * file handle. On Windows an open handle prevents the file from being deleted
 * or replaced.
 */
public class URLStreams {
    private URLStreams() {
    }

    /**
     * Opens an {@link InputStream} reading from the given URL without
     * caching the connection. This prevents file descriptor leaks when
     * reading from file-system URLs.
     *
     * @param url the URL to connect to
     * @return an input stream reading from the URL connection
     */
    public static InputStream openUncachedStream(URL url) throws IOException {
        return openUncachedConnection(url).getInputStream();
    }

    /**
     * Last-modified time of {@code url} for source-freshness checks used by
     * {@link groovy.lang.GroovyClassLoader} and
     * {@link org.codehaus.groovy.control.ClassNodeResolver}.
     * <p>
     * {@code file:} URLs use {@link File#lastModified()} because
     * {@link URLConnection#getLastModified()} often reports {@code -1}. The
     * path mapping includes the historical Windows form {@code file://c|/...}
     * where {@code |} stood for {@code :}. Other protocols use
     * {@link #getUncachedLastModified(URL)}.
     *
     * @param url the source URL
     * @return the last-modified time in milliseconds since the epoch, or
     *         {@code 0} if it is not known
     * @throws IOException if a non-{@code file:} URL cannot be opened
     */
    public static long getLastModified(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            return toFile(url).lastModified();
        }
        return getUncachedLastModified(url);
    }

    /**
     * Converts a {@code file:} URL to a {@link File}, including the historical
     * Netscape Windows form {@code file://c|/...} where {@code |} stood for
     * {@code :}.
     */
    static File toFile(URL url) {
        String path = url.getPath().replace('/', File.separatorChar).replace('|', ':');
        return new File(path);
    }

    /**
     * Returns {@link URLConnection#getLastModified()} for {@code url} without
     * caching the connection, then closes it.
     * <p>
     * {@code jar:} URLs otherwise keep a cached {@link java.util.jar.JarFile}
     * open after {@link URLConnection#getInputStream()} is closed, which on
     * Windows locks the JAR until the JVM exits. A failure to open the stream
     * (missing JAR entry, unreachable URL) is propagated so callers can treat
     * the source as unreadable; a failure to close after a successful open is
     * ignored because last-modified was already obtained.
     *
     * @param url the URL whose last-modified time is required
     * @return the last-modified time in milliseconds since the epoch, or
     *         {@code 0} if it is not known
     * @throws IOException if the connection or stream cannot be opened
     */
    public static long getUncachedLastModified(URL url) throws IOException {
        URLConnection urlConnection = openUncachedConnection(url);
        // Open first so a missing jar: entry fails before last-modified pins the JAR.
        InputStream in = urlConnection.getInputStream();
        try {
            return urlConnection.getLastModified();
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                // last-modified was already obtained
            }
        }
    }

    private static URLConnection openUncachedConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setUseCaches(false);
        return urlConnection;
    }
}
