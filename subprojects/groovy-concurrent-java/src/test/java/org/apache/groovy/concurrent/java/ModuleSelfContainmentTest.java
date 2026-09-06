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
package org.apache.groovy.concurrent.java;

import groovy.concurrent.ChannelSelect;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The module jar must stand alone: a Java project depending on it has no
 * Groovy runtime, so every {@code groovy.*} type a class in the jar
 * mentions, in a supertype, a signature, or an annotation, has to ship in
 * the jar too. The jar's include list lives in this module's build script;
 * these checks are what keep it honest.
 */
class ModuleSelfContainmentTest {

    /** A class name in one of Groovy's own packages, as written in a class file. */
    private static final String GROOVY_CLASS = "(?:groovy|org/apache/groovy)(?:/[a-z_]\\w*)*/[A-Z][\\w$]*";
    private static final Pattern BARE_NAME = Pattern.compile("^" + GROOVY_CLASS + "$");
    private static final Pattern DESCRIPTOR = Pattern.compile("L(" + GROOVY_CLASS + ")[<;]");

    @Test
    void groovyCoreIsNotOnTheTestClasspath() {
        // the tests only prove anything if they are compiled and run the way
        // a Java consumer is: against the module jar alone
        assertThrows(ClassNotFoundException.class, () -> Class.forName("groovy.lang.GroovyObject"));
    }

    @Test
    void everyGroovyReferenceResolvesInsideTheJar() throws IOException {
        Map<String, Set<String>> unresolved = new TreeMap<>();
        try (JarFile jar = new JarFile(moduleJar().toFile())) {
            Set<String> present = jar.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .collect(Collectors.toSet());
            for (String name : present) {
                Set<String> missing = new TreeSet<>();
                try (InputStream in = jar.getInputStream(jar.getJarEntry(name))) {
                    for (String ref : groovyReferences(in.readAllBytes())) {
                        if (!present.contains(ref + ".class")) missing.add(ref);
                    }
                }
                if (!missing.isEmpty()) unresolved.put(name, missing);
            }
        }
        assertTrue(unresolved.isEmpty(),
                () -> "groovy types referenced but not shipped in the jar (add them to the jar's include list): " + unresolved);
    }

    @Test
    void everyClassReflectsWithoutGroovyCore() throws Exception {
        Path jar = moduleJar();
        List<String> failures = new ArrayList<>();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader());
             JarFile jarFile = new JarFile(jar.toFile())) {
            List<String> classNames = jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
                    .collect(Collectors.toList());
            int members = 0;
            for (String className : classNames) {
                try {
                    Class<?> c = Class.forName(className, false, loader);
                    // each of these resolves the types it mentions
                    if (c.getSuperclass() != null) members++;
                    members += c.getInterfaces().length
                            + c.getDeclaredFields().length
                            + c.getDeclaredConstructors().length
                            + c.getDeclaredMethods().length;
                } catch (Throwable t) {
                    failures.add(className + ": " + t);
                }
            }
            assertTrue(members > 0, "nothing was reflected over");
        }
        assertTrue(failures.isEmpty(), () -> "classes that cannot be reflected over without Groovy core: " + failures);
    }

    private static Path moduleJar() {
        URL location = ChannelSelect.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(java.net.URI.create(location.toString()));
        assertTrue(path.getFileName().toString().endsWith(".jar"),
                () -> "the API should be loaded from the module jar, not from " + path);
        return path;
    }

    private static final int CONSTANT_UTF8 = 1;
    private static final int CONSTANT_LONG = 5;
    private static final int CONSTANT_DOUBLE = 6;
    /** Byte size of each non-Utf8 constant pool entry, by tag (JVMS §4.4). */
    private static final int[] CONSTANT_SIZE = new int[21];

    static {
        for (int tag : new int[]{7, 8, 16, 19, 20}) CONSTANT_SIZE[tag] = 2; // Class, String, MethodType, Module, Package
        CONSTANT_SIZE[15] = 3; // MethodHandle
        for (int tag : new int[]{3, 4, 9, 10, 11, 12, 17, 18}) CONSTANT_SIZE[tag] = 4; // Integer, Float, refs, NameAndType, Dynamic, InvokeDynamic
        CONSTANT_SIZE[CONSTANT_LONG] = 8;
        CONSTANT_SIZE[CONSTANT_DOUBLE] = 8;
    }

    /**
     * Every Groovy-package class name mentioned in the constant pool: class
     * references are stored as bare internal names, while field, method,
     * generic-signature and annotation references embed them in descriptors.
     */
    static Set<String> groovyReferences(byte[] classFile) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(classFile));
        in.readInt(); // magic
        in.readUnsignedShort(); // minor version
        in.readUnsignedShort(); // major version
        int count = in.readUnsignedShort();
        Set<String> refs = new TreeSet<>();
        for (int i = 1; i < count; i++) {
            int tag = in.readUnsignedByte();
            if (tag == CONSTANT_UTF8) {
                collectGroovyNames(in.readUTF(), refs);
                continue;
            }
            int size = tag < CONSTANT_SIZE.length ? CONSTANT_SIZE[tag] : 0;
            if (size == 0) throw new IllegalStateException("unknown constant pool tag " + tag);
            if (in.skipBytes(size) != size) throw new IllegalStateException("truncated constant pool");
            if (tag == CONSTANT_LONG || tag == CONSTANT_DOUBLE) i++; // these take two slots
        }
        return refs;
    }

    private static void collectGroovyNames(String text, Set<String> refs) {
        if (BARE_NAME.matcher(text).matches()) refs.add(text);
        Matcher m = DESCRIPTOR.matcher(text);
        while (m.find()) refs.add(m.group(1));
    }
}
