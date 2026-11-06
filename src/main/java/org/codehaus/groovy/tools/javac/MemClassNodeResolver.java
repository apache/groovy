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
package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.decompiled.AsmDecompiler;
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves types from in-memory class bytes before consulting {@code delegate}.
 * <p>
 * Classes still being compiled from Groovy source are left to the delegate so
 * a class file published at the output phase cannot replace the live AST.
 * Each distinct class file is parsed through a content-addressed URL, so a
 * later compile that replaces the bytes does not reuse a stale decompiled type.
 */
final class MemClassNodeResolver extends ClassNodeResolver {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final Map<String, byte[]> resources;
    private final ClassNodeResolver delegate;
    private final Map<String, ClassNode> parsed = new ConcurrentHashMap<>();

    MemClassNodeResolver(final Map<String, byte[]> resources, final ClassNodeResolver delegate) {
        this.resources = resources;
        this.delegate = delegate;
    }

    void clear() {
        parsed.clear();
    }

    @Override
    public LookupResult resolveName(final String name, final CompilationUnit compilationUnit) {
        LookupResult memory = fromMemory(name, compilationUnit);
        if (memory != null) {
            return memory;
        }
        if (delegate != null) {
            return delegate.resolveName(name, compilationUnit);
        }
        return super.resolveName(name, compilationUnit);
    }

    @Override
    public LookupResult findClassNode(final String name, final CompilationUnit compilationUnit) {
        LookupResult memory = fromMemory(name, compilationUnit);
        if (memory != null) {
            return memory;
        }
        if (delegate != null) {
            return delegate.findClassNode(name, compilationUnit);
        }
        return super.findClassNode(name, compilationUnit);
    }

    private LookupResult fromMemory(final String name, final CompilationUnit compilationUnit) {
        if (name == null || compilationUnit == null || isBeingCompiled(name, compilationUnit)) {
            return null;
        }
        byte[] data;
        try {
            data = resources.get(InMemoryJavaFileManager.resourceName(name));
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (data == null || data.length == 0) {
            return null;
        }
        String fingerprint = fingerprint(data);
        ClassNode node = parsed.get(fingerprint);
        if (node == null) {
            node = decompile(name, data, fingerprint, compilationUnit);
            if (node == null) {
                return null;
            }
            ClassNode raced = parsed.putIfAbsent(fingerprint, node);
            if (raced != null) {
                node = raced;
            }
        }
        if (!name.equals(node.getName())) {
            return null;
        }
        return new LookupResult(null, node);
    }

    @SuppressWarnings("deprecation") // custom handler; URL.of(URI, handler) is not on the Java 17 baseline
    private ClassNode decompile(final String name, final byte[] data, final String fingerprint,
            final CompilationUnit compilationUnit) {
        try {
            // The fingerprint is part of the path so a replaced class file cannot
            // reuse a decompiled type cached under the previous URI. The host stays
            // short; a SHA-256 hex string is not a valid single DNS label.
            URL url = new URL("x-groovy-mem", "bytes", -1,
                    "/" + fingerprint + "/" + InMemoryJavaFileManager.resourceName(name),
                    new BytesHandler(data));
            DecompiledClassNode node = new DecompiledClassNode(
                    AsmDecompiler.parseClass(url), new AsmReferenceResolver(this, compilationUnit));
            return name.equals(node.getName()) ? node : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isBeingCompiled(final String name, final CompilationUnit compilationUnit) {
        if (compilationUnit.getAST() == null) {
            return false;
        }
        for (ModuleNode module : compilationUnit.getAST().getModules()) {
            for (ClassNode classNode : module.getClasses()) {
                if (name.equals(classNode.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String fingerprint(final byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            char[] chars = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int value = digest[i] & 0xff;
                chars[i * 2] = HEX[value >>> 4];
                chars[i * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every Java platform.
            return Integer.toHexString(Arrays.hashCode(data)) + data.length;
        }
    }

    private static final class BytesHandler extends URLStreamHandler {
        private final byte[] data;

        private BytesHandler(final byte[] data) {
            this.data = data;
        }

        @Override
        protected URLConnection openConnection(final URL url) {
            return new URLConnection(url) {
                @Override
                public void connect() {
                    connected = true;
                }

                @Override
                public InputStream getInputStream() {
                    connected = true;
                    return new ByteArrayInputStream(data);
                }
            };
        }
    }
}
