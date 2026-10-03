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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Best-effort source-root and jar discovery when the client leaves
 * {@code groovy.sourcePaths} / {@code groovy.classpath} empty. Does not
 * invoke Gradle or Maven.
 */
public final class WorkspaceLayout {

    private WorkspaceLayout() {
    }

    /**
     * Search roots for Groovy sources under {@code folder}.
     *
     * @param folder workspace folder
     * @param sourcePaths client source paths, possibly empty
     * @return directories to walk
     */
    public static List<Path> searchRoots(final Path folder, final List<String> sourcePaths) {
        List<Path> roots = new ArrayList<>();
        if (folder == null || !Files.isDirectory(folder)) {
            return roots;
        }
        if (sourcePaths != null && !sourcePaths.isEmpty()) {
            return explicitRoots(folder, sourcePaths);
        }
        addIfDirectory(roots, folder.resolve("src/main/groovy"));
        addIfDirectory(roots, folder.resolve("src/test/groovy"));
        addIfDirectory(roots, folder.resolve("src/main/java"));
        addIfDirectory(roots, folder.resolve("src/test/java"));
        if (roots.isEmpty()) {
            addIfDirectory(roots, folder.resolve("src"));
        }
        if (roots.isEmpty()) {
            roots.add(folder);
        }
        return roots;
    }

    private static List<Path> explicitRoots(final Path folder, final List<String> sourcePaths) {
        List<Path> roots = new ArrayList<>();
        for (String sourcePath : sourcePaths) {
            Path extra = Path.of(sourcePath);
            roots.add(extra.isAbsolute() ? extra : folder.resolve(sourcePath));
        }
        return roots;
    }

    private static void addIfDirectory(final List<Path> roots, final Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path);
        }
    }

    /**
     * Jars under {@code lib/} and {@code libs/} of {@code folder}.
     *
     * @param folder workspace folder
     * @return absolute jar paths
     */
    public static List<String> classpathJars(final Path folder) {
        List<String> jars = new ArrayList<>();
        if (folder == null || !Files.isDirectory(folder)) {
            return jars;
        }
        for (String dir : List.of("lib", "libs")) {
            Path path = folder.resolve(dir);
            if (!Files.isDirectory(path)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(path)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                        .forEach(p -> jars.add(p.toAbsolutePath().toString()));
            } catch (IOException ignored) {
                // skip unreadable lib dirs
            }
        }
        return jars;
    }

    /**
     * Fills empty classpath / source-path lists from conventional layout.
     *
     * @param settings client settings
     * @param folders workspace folders
     * @return settings to compile with
     */
    public static CompilerSettings withInferred(final CompilerSettings settings, final Collection<URI> folders) {
        CompilerSettings current = settings == null ? CompilerSettings.defaults() : settings;
        List<String> classpath = new ArrayList<>(current.getClasspath());
        List<String> sourcePaths = new ArrayList<>(current.getSourcePaths());
        if (folders != null) {
            for (URI folder : folders) {
                inferFromFolder(folder, classpath, sourcePaths);
            }
        }
        return new CompilerSettings(classpath, sourcePaths, current.getThroughPhase(),
                current.isGrapeEnabled(), current.isAstTestEnabled(), current.getExtra());
    }

    private static void inferFromFolder(final URI folder, final List<String> classpath,
                                        final List<String> sourcePaths) {
        if (folder == null || !"file".equalsIgnoreCase(folder.getScheme())) {
            return;
        }
        Path path = Uris.toPath(folder);
        if (sourcePaths.isEmpty()) {
            for (Path root : searchRoots(path, List.of())) {
                if (!root.equals(path)) {
                    sourcePaths.add(Uris.toUnix(path.relativize(root)));
                }
            }
        }
        if (classpath.isEmpty()) {
            classpath.addAll(classpathJars(path));
        }
    }
}
