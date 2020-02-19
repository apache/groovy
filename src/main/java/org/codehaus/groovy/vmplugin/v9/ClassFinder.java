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
package org.codehaus.groovy.vmplugin.v9;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Find classes under the specified package
 *
 * @since 3.0.2
 */
public class ClassFinder {
    public static Map<String, Set<String>> find(URI originalUri, String packageName) {
        return find(originalUri, packageName, false);
    }

    public static Map<String, Set<String>> find(URI originalUri, String packageName, boolean recursive) {
        URI uri;
        String prefix;
        if ("jrt".equals(originalUri.getScheme())) {
            uri = URI.create("jrt:/");
            prefix = originalUri.getPath().substring(1) + "/";
        } else {
            Path path = Paths.get(originalUri);
            if (Files.isDirectory(path)) {
                uri = URI.create("file:/");
                prefix = path.toString();
            } else {
                uri = URI.create("jar:" + originalUri.toString());
                prefix = "";
            }
        }

        return find(uri, prefix, packageName, recursive);
    }

    private static Map<String, Set<String>> find(URI uri, String prefix, String packageName, boolean recursive) {
        boolean wfs = "file".equals(uri.getScheme());
        if (wfs) prefix = prefix.replace("/", File.separator);

        final String sepPatten = Pattern.quote(wfs ? File.separator : "/");
        final int prefixElemCnt = prefix.trim().isEmpty() ? 0 : prefix.split(sepPatten).length;

        Map<String, Set<String>> result = new LinkedHashMap<>();
        try (FileSystem fs = newFileSystem(uri)) {
            Files.walkFileTree(fs.getPath(prefix + "/" + packageName), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    String[] pathElems = path.toString().split(sepPatten);
                    int nameCount = pathElems.length;
                    if (nameCount <= prefixElemCnt) {
                        return FileVisitResult.CONTINUE;
                    }

                    String filename = pathElems[nameCount - 1];
                    if (!filename.endsWith(".class") || "module-info.class".equals(filename) || "package-info.class".equals(filename)) {
                        return FileVisitResult.CONTINUE;
                    }

                    String packagePathStr = String.join("/", Arrays.copyOfRange(pathElems, prefixElemCnt + (!wfs && pathElems[0].isEmpty() ? 1 : 0), nameCount - 1));

                    if (recursive || packageName.equals(packagePathStr)) {
                        Set<String> packageNameSet = result.computeIfAbsent(filename.substring(0, filename.lastIndexOf(".")), f -> new HashSet<>(2));
                        packageNameSet.add(packagePathStr);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (UnsupportedOperationException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static FileSystem newFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException e) {
            return FileSystems.getFileSystem(uri);
        }
    }

    private ClassFinder() {}
}
