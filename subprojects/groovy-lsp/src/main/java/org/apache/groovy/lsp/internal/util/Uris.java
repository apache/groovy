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
package org.apache.groovy.lsp.internal.util;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Normalizes {@code file:} URIs so clients that encode drive-letter colons
 * and those that do not still identify the same document.
 */
public final class Uris {

    static final String URI_PATH_SEPARATOR = "/";

    private static final String[] GROOVY_EXTENSIONS = {".groovy", ".gvy", ".gy", ".gsh"};
    private static final String JAVA_EXTENSION = ".java";

    private Uris() {
    }

    /**
     * Parses and normalizes a document URI string.
     *
     * @param uriString the URI from the protocol
     * @return a comparable URI
     */
    public static URI parse(final String uriString) {
        return normalize(URI.create(Objects.requireNonNull(uriString, "uriString")));
    }

    /**
     * Returns a canonical {@code file:} URI when {@code uri} is a file URI.
     *
     * @param uri the URI to normalize
     * @return the normalized URI
     */
    public static URI normalize(final URI uri) {
        Objects.requireNonNull(uri, "uri");
        if (!"file".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        Path path = Paths.get(uri);
        URI normalized = path.toUri();
        String scheme = normalized.getScheme();
        String rawPath = normalized.getRawPath();
        if (rawPath != null && rawPath.length() >= 3 && rawPath.charAt(0) == '/'
                && Character.isLetter(rawPath.charAt(1)) && rawPath.charAt(2) == ':') {
            char drive = Character.toLowerCase(rawPath.charAt(1));
            rawPath = URI_PATH_SEPARATOR + drive + rawPath.substring(2);
            return URI.create(scheme + "://" + (normalized.getRawAuthority() == null ? "" : normalized.getRawAuthority())
                    + rawPath);
        }
        return normalized;
    }

    /**
     * Converts a file URI to a path.
     *
     * @param uri a file URI
     * @return the path
     */
    public static Path toPath(final URI uri) {
        return Paths.get(normalize(uri));
    }

    /**
     * Last path segment of {@code uri}, without going through
     * {@link Path#of(String, String...)} on {@link URI#getPath()}. Windows
     * file URIs have a path like {@code /d:/project/Hello.groovy}, which is
     * not a legal {@code Path} on that OS.
     *
     * @param uri a document URI
     * @return the file name, or {@code ""}
     */
    public static String fileName(final URI uri) {
        if (uri == null) {
            return "";
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = uri.getSchemeSpecificPart();
        }
        if (path == null || path.isEmpty()) {
            return "";
        }
        int slash = lastSeparator(path);
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        int query = name.indexOf('?');
        return query >= 0 ? name.substring(0, query) : name;
    }

    /**
     * Index of the last {@code /} or {@code \\} in {@code path}.
     *
     * @param path a URI path or file name
     * @return the index, or {@code -1}
     */
    public static int lastSeparator(final String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }
        return Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    }

    /**
     * Relative path using {@code /} separators, suitable for LSP settings
     * and assertions on every OS.
     *
     * @param path a relative path
     * @return the unix-separated form, or {@code ""}
     */
    public static String toUnix(final Path path) {
        return path == null ? "" : path.toString().replace('\\', '/');
    }

    /**
     * Returns whether {@code uri} looks like a Groovy source document.
     *
     * @param uri document URI
     * @param languageId LSP language id, which may be {@code null}
     * @return {@code true} when the document should be compiled as Groovy
     */
    public static boolean isGroovyDocument(final URI uri, final String languageId) {
        if ("groovy".equalsIgnoreCase(languageId)) {
            return true;
        }
        return uri != null && isGroovyFileName(uri.getPath());
    }

    /**
     * Whether {@code name} ends with a Groovy source extension.
     *
     * @param name a file name or path
     * @return {@code true} for {@code .groovy} / {@code .gvy} / {@code .gy} / {@code .gsh}
     */
    public static boolean isGroovyFileName(final String name) {
        return groovyExtension(name) != null;
    }

    /**
     * Returns whether {@code uri} is a Java source document.
     *
     * @param uri document URI
     * @param languageId LSP language id, which may be {@code null}
     * @return {@code true} for Java sources
     */
    public static boolean isJavaDocument(final URI uri, final String languageId) {
        if ("java".equalsIgnoreCase(languageId)) {
            return true;
        }
        return uri != null && isJavaFileName(uri.getPath());
    }

    /**
     * Whether {@code name} ends with {@code .java}.
     *
     * @param name a file name or path
     * @return {@code true} for Java sources
     */
    public static boolean isJavaFileName(final String name) {
        return name != null && name.toLowerCase(Locale.ROOT).endsWith(JAVA_EXTENSION);
    }

    /**
     * The Groovy source extension of {@code name}, including the leading
     * dot, preserving the source casing of that suffix.
     *
     * @param name a file name or path
     * @return the extension, or {@code null}
     */
    public static String groovyExtension(final String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        for (String extension : GROOVY_EXTENSIONS) {
            if (lower.endsWith(extension)) {
                return name.substring(name.length() - extension.length());
            }
        }
        return null;
    }
}
