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

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Infers a Groovy package from a file's path under conventional source roots,
 * the same idea as Metals' package provider.
 */
public final class PackageGuess {

    private PackageGuess() {
    }

    /**
     * @param file document URI
     * @param folders workspace folders
     * @param sourcePaths client source paths
     * @return a dotted package, or {@code ""}
     */
    public static String fromUri(final URI file, final Collection<URI> folders, final List<String> sourcePaths) {
        if (file == null || !"file".equalsIgnoreCase(file.getScheme()) || folders == null) {
            return "";
        }
        Path path;
        try {
            path = Uris.toPath(file);
        } catch (RuntimeException ex) {
            return "";
        }
        Path parent = path.getParent();
        if (parent == null) {
            return "";
        }
        for (URI folder : folders) {
            String guessed = packageUnder(parent, folder, sourcePaths);
            if (guessed != null) {
                return guessed;
            }
        }
        return "";
    }

    private static String packageUnder(final Path parent, final URI folder, final List<String> sourcePaths) {
        if (folder == null || !"file".equalsIgnoreCase(folder.getScheme())) {
            return null;
        }
        Path root = Uris.toPath(folder);
        for (Path sourceRoot : WorkspaceLayout.searchRoots(root, sourcePaths)) {
            if (parent.startsWith(sourceRoot)) {
                Path relative = sourceRoot.relativize(parent);
                if (relative.getNameCount() == 0 || ".".equals(relative.toString())) {
                    return "";
                }
                return relative.toString().replace('\\', '/').replace('/', '.');
            }
        }
        return null;
    }
}
