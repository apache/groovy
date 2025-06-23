/*
 * Copyright (c) 2002-2023, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.apache.groovy.groovysh.jline;

import org.jline.utils.Log;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Helper class to resolve java.base module classes
 */
public class JrtJavaBasePackages {
    public static List<Object> getClassesForPackage(String pckgname) {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        List<String> dirs = new ArrayList<>();
        dirs.add("java.base");
        boolean nestedClasses = true;
        boolean onlyCurrent = false;
        if (pckgname.endsWith(".*")) {
            onlyCurrent = true;
            nestedClasses = false;
            pckgname = pckgname.substring(0, pckgname.length() - 2);
        } else if (pckgname.endsWith(".**")) {
            onlyCurrent = true;
            pckgname = pckgname.substring(0, pckgname.length() - 3);
        }
        dirs.addAll(Arrays.asList(pckgname.split("\\.")));
        Path path = fs.getPath("modules", dirs.toArray(new String[0]));
        FileVisitor fv = new FileVisitor(pckgname, nestedClasses);
        try {
            if (onlyCurrent) {
                Files.walkFileTree(path, new HashSet<>(), 1, fv);
            } else {
                Files.walkFileTree(path, fv);
            }
        } catch (IOException e) {
            if (Log.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        return fv.getClasses();
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {
        private final List<Object> classes = new ArrayList<>();
        private final boolean nestedClasses;
        private final String packageName;

        public FileVisitor(String packageName, boolean nestedClasses) {
            super();
            this.packageName = packageName;
            this.nestedClasses = nestedClasses;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            try {
                String name = file.toString().substring(18);
                if (name.endsWith(".class") && (nestedClasses || !name.contains("$"))) {
                    String className = name.substring(0, name.length() - 6).replaceAll("/", ".");
                    if (Character.isUpperCase(className.charAt(packageName.length() + 1))) {
                        classes.add(Class.forName(className));
                    } else {
                        classes.add(className);
                    }
                }
            } catch (Exception | Error e) {
                if (Log.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
            return CONTINUE;
        }

        private List<Object> getClasses() {
            return classes;
        }
    }
}
