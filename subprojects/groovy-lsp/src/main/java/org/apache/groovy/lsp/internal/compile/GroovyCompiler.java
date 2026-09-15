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

import groovy.lang.GroovyClassLoader;
import org.apache.groovy.lsp.internal.ExtensionHost;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Compiles Groovy sources through semantic analysis (no class files) so
 * the language server can offer diagnostics and navigation on a partial
 * AST. Errors are collected rather than aborting the compile.
 */
public final class GroovyCompiler implements AutoCloseable {

    private GroovyClassLoader groovyLoader;
    private URLClassLoader classpathLoader;
    private LoaderKey loaderKey;

    /**
     * Compiles {@code openDocuments} together with extra files from disk
     * that are not already open. Open buffers overlay the filesystem.
     *
     * @param openDocuments open editor buffers
     * @param extraFiles additional Groovy files
     * @param settings compiler settings
     * @param parentLoader parent class loader
     * @return an immutable snapshot
     */
    public CompilationSnapshot compile(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                       final CompilerSettings settings, final ClassLoader parentLoader) {
        return compile(openDocuments, extraFiles, settings, parentLoader, ExtensionHost.none());
    }

    /**
     * Compiles like {@link #compile(Collection, Collection, CompilerSettings, ClassLoader)}
     * and then runs {@code extensions} so plugins can inject AST or run an
     * intelligence pass before the snapshot is frozen.
     *
     * @param openDocuments open editor buffers
     * @param extraFiles additional Groovy files
     * @param settings compiler settings
     * @param parentLoader parent class loader
     * @param extensions plugins, never {@code null}
     * @return an immutable snapshot
     */
    public CompilationSnapshot compile(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                       final CompilerSettings settings, final ClassLoader parentLoader,
                                       final ExtensionHost extensions) {
        CompilerConfiguration configuration = settings.toConfiguration();
        ExtensionHost host = extensions == null ? ExtensionHost.none() : extensions;
        host.configure(configuration, settings);
        String previousAstTest = System.getProperty("groovy.asttest.enable");
        String previousGrape = System.getProperty("groovy.grape.enable");
        applyProcessFlags(settings);
        try {
            return compileUnit(openDocuments, extraFiles, settings, parentLoader, host, configuration);
        } finally {
            restoreProperty("groovy.asttest.enable", previousAstTest);
            restoreProperty("groovy.grape.enable", previousGrape);
        }
    }

    private CompilationSnapshot compileUnit(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                            final CompilerSettings settings, final ClassLoader parentLoader,
                                            final ExtensionHost host, final CompilerConfiguration configuration) {
        GroovyClassLoader loader = loaderFor(settings, parentLoader, configuration);
        CompilationUnit unit = new CompilationUnit(configuration, null, loader);
        Map<URI, Integer> versions = new LinkedHashMap<>();
        Map<URI, String> texts = new LinkedHashMap<>();

        addOpenDocuments(unit, openDocuments, versions, texts);
        addExtraFiles(unit, extraFiles, versions, texts);

        try {
            unit.compile(settings.getThroughPhase());
        } catch (CompilationFailedException ignored) {
            // keep the partial AST and collected messages
        }
        host.afterCompile(unit);

        Map<URI, CompiledDocument> compiled = CompilationSnapshot.newMap();
        var sourceIterator = unit.iterator();
        while (sourceIterator.hasNext()) {
            addCompiled(compiled, versions, texts, sourceIterator.next());
        }
        for (Map.Entry<URI, String> entry : texts.entrySet()) {
            compiled.computeIfAbsent(entry.getKey(), uri -> new CompiledDocument(uri,
                    versions.getOrDefault(uri, -1), entry.getValue(), null, null, null));
        }
        return new CompilationSnapshot(compiled);
    }

    private static void addCompiled(final Map<URI, CompiledDocument> compiled, final Map<URI, Integer> versions,
                                    final Map<URI, String> texts, final SourceUnit sourceUnit) {
        URI uri;
        try {
            uri = Uris.parse(sourceUnit.getName());
        } catch (IllegalArgumentException ex) {
            return;
        }
        compiled.put(uri, new CompiledDocument(uri, versions.getOrDefault(uri, -1),
                texts.getOrDefault(uri, ""), sourceUnit.getAST(), sourceUnit, sourceUnit.getErrorCollector()));
    }

    private static void addOpenDocuments(final CompilationUnit unit, final Collection<TextDocument> openDocuments,
                                         final Map<URI, Integer> versions, final Map<URI, String> texts) {
        if (openDocuments == null) {
            return;
        }
        for (TextDocument document : openDocuments) {
            if (Uris.isGroovyDocument(document.getUri(), document.getLanguageId())) {
                URI uri = Uris.normalize(document.getUri());
                unit.addSource(uri.toString(), document.getText());
                versions.put(uri, document.getVersion());
                texts.put(uri, document.getText());
            }
        }
    }

    private static void addExtraFiles(final CompilationUnit unit, final Collection<Path> extraFiles,
                                      final Map<URI, Integer> versions, final Map<URI, String> texts) {
        if (extraFiles == null) {
            return;
        }
        for (Path path : extraFiles) {
            addExtraFile(unit, path, versions, texts);
        }
    }

    private static void addExtraFile(final CompilationUnit unit, final Path path,
                                     final Map<URI, Integer> versions, final Map<URI, String> texts) {
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        URI uri = Uris.normalize(path.toUri());
        if (texts.containsKey(uri) || !Uris.isGroovyDocument(uri, null)) {
            return;
        }
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            unit.addSource(uri.toString(), text);
            versions.put(uri, -1);
            texts.put(uri, text);
        } catch (IOException ignored) {
            // skip unreadable files
        }
    }

    /**
     * {@code @ASTTest} is a local transform; {@code disabledGlobalASTTransformations}
     * does not turn it off. The documented process flags do.
     *
     * @param settings compiler settings
     */
    static void applyProcessFlags(final CompilerSettings settings) {
        System.setProperty("groovy.asttest.enable", Boolean.toString(settings.isAstTestEnabled()));
        System.setProperty("groovy.grape.enable", Boolean.toString(settings.isGrapeEnabled()));
    }

    private static void restoreProperty(final String key, final String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }

    private synchronized GroovyClassLoader loaderFor(final CompilerSettings settings, final ClassLoader parentLoader,
                                                     final CompilerConfiguration configuration) {
        LoaderKey key = new LoaderKey(settings.getClasspath(), settings.isGrapeEnabled(),
                settings.isAstTestEnabled(), parentLoader);
        if (groovyLoader != null && key.equals(loaderKey)) {
            return groovyLoader;
        }
        closeQuietly();
        loaderKey = key;
        classpathLoader = settings.createClassLoader(parentLoader);
        groovyLoader = new GroovyClassLoader(classpathLoader, configuration);
        return groovyLoader;
    }

    /**
     * Closes the reused class loaders.
     */
    @Override
    public synchronized void close() {
        closeQuietly();
    }

    private void closeQuietly() {
        if (groovyLoader != null) {
            try {
                groovyLoader.close();
            } catch (IOException ignored) {
                // best-effort close
            }
            groovyLoader = null;
        }
        if (classpathLoader != null) {
            try {
                classpathLoader.close();
            } catch (IOException ignored) {
                // best-effort close
            }
            classpathLoader = null;
        }
        loaderKey = null;
    }

    private static final class LoaderKey {
        private final List<String> classpath;
        private final boolean grape;
        private final boolean astTest;
        private final ClassLoader parent;

        private LoaderKey(final List<String> classpath, final boolean grape, final boolean astTest,
                          final ClassLoader parent) {
            this.classpath = classpath;
            this.grape = grape;
            this.astTest = astTest;
            this.parent = parent;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LoaderKey key)) {
                return false;
            }
            return grape == key.grape && astTest == key.astTest
                    && Objects.equals(classpath, key.classpath) && parent == key.parent;
        }

        @Override
        public int hashCode() {
            return Objects.hash(classpath, grape, astTest, System.identityHashCode(parent));
        }
    }
}
