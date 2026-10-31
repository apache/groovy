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
package org.apache.groovy.mcp.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Confines file access to one workspace root. The lexical path and the
 * real path both have to stay inside the root, so a symlink cannot step
 * outside it. The root is not walked.
 */
public final class PathPolicy {

    private final Path root;

    /**
     * @param root absolute normalized workspace root, or {@code null} when files are refused
     */
    public PathPolicy(final Path root) {
        this.root = root;
    }

    /**
     * Resolves a regular file under the root and checks its size.
     *
     * @param raw path from the client
     * @param maxBytes maximum file size
     * @return the real path
     */
    public Path resolveFile(final String raw, final int maxBytes) {
        Path absolute = absolute(raw);
        if (!Files.isRegularFile(absolute)) {
            throw new ToolFailure("path is not a file");
        }
        long size = sizeOf(absolute);
        if (size > maxBytes) {
            throw new ToolFailure("file exceeds the source limit");
        }
        return realInside(absolute);
    }

    /**
     * Checks that an existing file URI's path is still inside the root.
     * The returned path is the real path, which is the identity
     * {@code open(Path)} stores.
     *
     * @param path file path
     * @return the real path inside the root
     */
    public Path requireInside(final Path path) {
        if (root == null) {
            throw new ToolFailure("path requires --root");
        }
        if (path == null) {
            throw new ToolFailure("path is outside the workspace root");
        }
        Path absolute = path.toAbsolutePath().normalize();
        if (!inside(root, absolute)) {
            throw new ToolFailure("path is outside the workspace root");
        }
        return realInside(absolute);
    }

    private Path absolute(final String raw) {
        if (root == null) {
            throw new ToolFailure("path requires --root");
        }
        if (raw == null || raw.isBlank()) {
            throw new ToolFailure("path is empty");
        }
        Path candidate;
        try {
            candidate = Path.of(raw);
        } catch (InvalidPathException ex) {
            throw new ToolFailure("path is not valid");
        }
        Path absolute = (candidate.isAbsolute() ? candidate : root.resolve(candidate)).normalize();
        if (!inside(root, absolute)) {
            throw new ToolFailure("path is outside the workspace root");
        }
        return absolute;
    }

    private Path realInside(final Path absolute) {
        Path realRoot;
        Path real;
        try {
            realRoot = root.toRealPath();
            real = absolute.toRealPath();
        } catch (IOException ex) {
            throw new ToolFailure("path is not readable");
        }
        if (!inside(realRoot, real)) {
            throw new ToolFailure("path is outside the workspace root");
        }
        return real;
    }

    private static long sizeOf(final Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            throw new ToolFailure("path is not readable");
        }
    }

    /**
     * {@link Path#startsWith} compares name elements, so {@code /root/foo}
     * does not contain {@code /root/foobar}.
     */
    static boolean inside(final Path root, final Path candidate) {
        return root != null && candidate != null && candidate.startsWith(root);
    }
}
