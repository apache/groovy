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
package org.apache.groovy.lsp.internal.compile;

import org.apache.groovy.lsp.internal.util.Uris;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds Groovy and Java sources under workspace folders and configured source paths.
 */
public final class WorkspaceScanner {

    private static final int MAX_FILES = 4000;

    /**
     * Directory names skipped at any depth unless that directory is the
     * search root itself. Clients add more names with
     * {@code groovy.ignoredDirectories}; those names are unioned with this
     * list rather than replacing it.
     */
    public static final List<String> DEFAULT_IGNORED_DIRECTORIES = List.of(
            "build", "out", "target", ".git", ".gradle", "node_modules");

    /**
     * Scans {@code roots} plus {@code sourcePaths} using the default
     * ignored directory names.
     *
     * @param roots workspace folders
     * @param sourcePaths extra relative or absolute source roots
     * @return discovered files, each path at most once
     */
    public List<Path> scan(final Collection<URI> roots, final List<String> sourcePaths) {
        return scan(roots, sourcePaths, List.of());
    }

    /**
     * Scans {@code roots} plus {@code sourcePaths} (resolved against each root)
     * for Groovy and Java files. Directory names in
     * {@link #DEFAULT_IGNORED_DIRECTORIES} plus {@code ignoredDirectories}
     * are not descended into.
     *
     * @param roots workspace folders
     * @param sourcePaths extra relative or absolute source roots
     * @param ignoredDirectories extra directory names to skip, possibly empty
     * @return discovered files, each path at most once
     */
    public List<Path> scan(final Collection<URI> roots, final List<String> sourcePaths,
                           final Collection<String> ignoredDirectories) {
        List<Path> files = new ArrayList<>();
        List<Path> searchRoots = new ArrayList<>();
        if (roots != null) {
            for (URI root : roots) {
                if (root == null || !"file".equalsIgnoreCase(root.getScheme())) {
                    continue;
                }
                searchRoots.addAll(WorkspaceLayout.searchRoots(Uris.toPath(root), sourcePaths));
            }
        }
        Set<String> ignored = ignoredDirectoryNames(ignoredDirectories);
        Set<Path> seen = new HashSet<>();
        Set<Path> walked = new HashSet<>();
        for (Path root : searchRoots) {
            if (files.size() >= MAX_FILES) {
                break;
            }
            if (!walked.add(normalized(root))) {
                continue;
            }
            addGroovyFiles(files, root, seen, ignored);
        }
        return files;
    }

    /**
     * Default ignored names plus {@code extra}. Path-like entries use the
     * last segment ({@code src/generated} skips directories named
     * {@code generated}). Blank, {@code .}, and {@code ..} are dropped.
     *
     * @param extra client names, possibly {@code null}
     * @return names to skip, never {@code null}
     */
    public static Set<String> ignoredDirectoryNames(final Collection<String> extra) {
        Set<String> names = new HashSet<>(DEFAULT_IGNORED_DIRECTORIES);
        if (extra != null) {
            for (String entry : extra) {
                String name = directoryName(entry);
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private void addGroovyFiles(final List<Path> files, final Path root, final Set<Path> seen,
                                final Set<String> ignored) {
        if (!Files.isDirectory(root)) {
            return;
        }
        Path start = normalized(root);
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                    if (files.size() >= MAX_FILES) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (!normalized(dir).equals(start) && ignored.contains(fileName(dir))) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (files.size() >= MAX_FILES) {
                        return FileVisitResult.TERMINATE;
                    }
                    String name = fileName(file);
                    if ((Uris.isGroovyFileName(name) || Uris.isJavaFileName(name))
                            && seen.add(normalized(file))) {
                        files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignoredWalk) {
            // skip unreadable trees
        }
    }

    private static Path normalized(final Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static String fileName(final Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    private static String directoryName(final String entry) {
        if (entry == null) {
            return null;
        }
        String name = entry.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1).trim();
        }
        if (name.isEmpty() || ".".equals(name) || "..".equals(name)) {
            return null;
        }
        return name;
    }
}
