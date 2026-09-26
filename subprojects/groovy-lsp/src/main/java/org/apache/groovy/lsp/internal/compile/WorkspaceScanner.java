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
import java.util.stream.Stream;

/**
 * Finds Groovy and Java sources under workspace folders and configured source paths.
 */
public final class WorkspaceScanner {

    private static final int MAX_FILES = 4000;

    /**
     * Scans {@code roots} plus {@code sourcePaths} (resolved against each root)
     * for Groovy files, skipping {@code build}, {@code .git} and similar.
     *
     * @param roots workspace folders
     * @param sourcePaths extra relative or absolute source roots
     * @return discovered files
     */
    public List<Path> scan(final Collection<URI> roots, final List<String> sourcePaths) {
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
        for (Path root : searchRoots) {
            if (files.size() >= MAX_FILES) {
                break;
            }
            addGroovyFiles(files, root);
        }
        return files;
    }

    private void addGroovyFiles(final List<Path> files, final Path root) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return Uris.isGroovyFileName(name) || Uris.isJavaFileName(name);
                    })
                    .filter(p -> !isIgnored(root.relativize(p)))
                    .limit(MAX_FILES - (long) files.size())
                    .forEach(files::add);
        } catch (IOException ignored) {
            // skip unreadable trees
        }
    }

    private boolean isIgnored(final Path relative) {
        for (Path part : relative) {
            String name = part.toString();
            if ("build".equals(name) || "out".equals(name) || "target".equals(name)
                    || ".git".equals(name) || ".gradle".equals(name) || "node_modules".equals(name)) {
                return true;
            }
        }
        return false;
    }
}
