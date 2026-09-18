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
import org.apache.groovy.lsp.spi.GroovyLspSession;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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

    private static final String ASTTEST_ENABLE = "groovy.asttest.enable";
    private static final String GRAPE_ENABLE = "groovy.grape.enable";

    private GroovyClassLoader groovyLoader;
    private URLClassLoader classpathLoader;
    private LoaderKey loaderKey;
    private Path javacOutput;

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
        return compile(openDocuments, extraFiles, settings, parentLoader, extensions, null);
    }

    /**
     * Compiles like
     * {@link #compile(Collection, Collection, CompilerSettings, ClassLoader, ExtensionHost)}
     * and passes {@code session} to {@code configure} so plugins can read
     * folders and extra settings.
     *
     * @param session session, possibly {@code null}
     * @return an immutable snapshot
     */
    public CompilationSnapshot compile(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                       final CompilerSettings settings, final ClassLoader parentLoader,
                                       final ExtensionHost extensions, final GroovyLspSession session) {
        CompilerConfiguration configuration = settings.toConfiguration();
        ExtensionHost host = extensions == null ? ExtensionHost.none() : extensions;
        host.configure(configuration, session);
        String previousAstTest = System.getProperty(ASTTEST_ENABLE);
        String previousGrape = System.getProperty(GRAPE_ENABLE);
        applyProcessFlags(settings);
        try {
            return compileUnit(openDocuments, extraFiles, settings, parentLoader, host, configuration);
        } finally {
            restoreProperty(ASTTEST_ENABLE, previousAstTest);
            restoreProperty(GRAPE_ENABLE, previousGrape);
        }
    }

    private CompilationSnapshot compileUnit(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                            final CompilerSettings settings, final ClassLoader parentLoader,
                                            final ExtensionHost host, final CompilerConfiguration configuration) {
        Map<URI, Integer> versions = new LinkedHashMap<>();
        Map<URI, String> texts = new LinkedHashMap<>();
        Map<URI, String> javaOverlays = new LinkedHashMap<>();
        List<File> javaFiles = collectJavaSources(openDocuments, extraFiles, versions, texts, javaOverlays);
        LspJavaCompiler javaCompiler = javaFiles.isEmpty() ? null : new LspJavaCompiler(javaOverlays);
        CompilationUnit unit = compilationUnit(settings, parentLoader, configuration, javaCompiler, !javaFiles.isEmpty());
        addOpenDocuments(unit, openDocuments, versions, texts);
        addExtraFiles(unit, extraFiles, versions, texts);
        if (unit instanceof JavaAwareCompilationUnit javaUnit && !javaFiles.isEmpty()) {
            javaUnit.addSources(javaFiles.toArray(File[]::new));
        }

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
        Map<URI, List<JavaMessage>> javaMessages = javaCompiler == null ? Map.of() : javaCompiler.messages();
        for (Map.Entry<URI, String> entry : texts.entrySet()) {
            compiled.computeIfAbsent(entry.getKey(), uri -> new CompiledDocument(uri,
                    versions.getOrDefault(uri, -1), entry.getValue(), null, null, null));
        }
        for (Map.Entry<URI, List<JavaMessage>> entry : javaMessages.entrySet()) {
            compiled.compute(entry.getKey(), (uri, current) -> {
                CompiledDocument base = current == null
                        ? new CompiledDocument(uri, versions.getOrDefault(uri, -1),
                        texts.getOrDefault(uri, ""), null, null, null) : current;
                return base.withJavaMessages(entry.getValue());
            });
        }
        JavaSymbolIndex javaSymbols = javaCompiler == null ? JavaSymbolIndex.EMPTY : javaCompiler.index();
        return new CompilationSnapshot(compiled, javaSymbols);
    }

    private CompilationUnit compilationUnit(final CompilerSettings settings, final ClassLoader parentLoader,
                                            final CompilerConfiguration configuration, final LspJavaCompiler javaCompiler,
                                            final boolean joint) {
        if (!joint || javaCompiler == null) {
            return new CompilationUnit(configuration, null, loaderFor(settings, parentLoader, configuration));
        }
        closeQuietly();
        if (!prepareJavacOutput(configuration)) {
            return new CompilationUnit(configuration, null, loaderFor(settings, parentLoader, configuration));
        }
        Map<String, Object> jointOptions = new HashMap<>();
        jointOptions.put(CompilerConfiguration.MEM_STUB, Boolean.TRUE);
        configuration.setJointCompilationOptions(jointOptions);
        GroovyClassLoader loader = loaderFor(settings, parentLoader, configuration);
        JavaAwareCompilationUnit unit = new JavaAwareCompilationUnit(configuration, loader);
        unit.setCompilerFactory(javaCompiler);
        return unit;
    }

    private synchronized boolean prepareJavacOutput(final CompilerConfiguration configuration) {
        deleteJavacOutput();
        try {
            Path base = Files.createDirectories(Path.of(System.getProperty("user.home"), ".groovy", "lsp-javac"));
            restrictOwner(base);
            javacOutput = Files.createTempDirectory(base, "jc-");
            restrictOwner(javacOutput);
            configuration.setTargetDirectory(javacOutput.toFile());
            return true;
        } catch (IOException | RuntimeException ignored) {
            javacOutput = null;
            return false;
        }
    }

    private static void restrictOwner(final Path directory) {
        try {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // NTFS and similar have no POSIX bits
        }
    }

    private List<File> collectJavaSources(final Collection<TextDocument> openDocuments, final Collection<Path> extraFiles,
                                          final Map<URI, Integer> versions, final Map<URI, String> texts,
                                          final Map<URI, String> javaOverlays) {
        List<File> javaFiles = new ArrayList<>();
        addOpenJava(openDocuments, versions, texts, javaOverlays, javaFiles);
        addDiskJava(extraFiles, versions, texts, javaFiles);
        javaFiles.sort(Comparator.comparing(File::getAbsolutePath));
        return javaFiles;
    }

    private static void addOpenJava(final Collection<TextDocument> openDocuments, final Map<URI, Integer> versions,
                                    final Map<URI, String> texts, final Map<URI, String> javaOverlays,
                                    final List<File> javaFiles) {
        if (openDocuments == null) {
            return;
        }
        for (TextDocument document : openDocuments) {
            if (Uris.isJavaDocument(document.getUri(), document.getLanguageId())) {
                URI uri = Uris.normalize(document.getUri());
                javaOverlays.put(uri, document.getText());
                versions.put(uri, document.getVersion());
                texts.put(uri, document.getText());
                addJavaFile(javaFiles, uri);
            }
        }
    }

    private static void addDiskJava(final Collection<Path> extraFiles, final Map<URI, Integer> versions,
                                    final Map<URI, String> texts, final List<File> javaFiles) {
        if (extraFiles == null) {
            return;
        }
        for (Path path : extraFiles) {
            if (path == null || !Files.isRegularFile(path) || !Uris.isJavaFileName(path.getFileName().toString())) {
                continue;
            }
            URI uri = Uris.normalize(path.toUri());
            if (texts.containsKey(uri)) {
                addJavaFile(javaFiles, uri);
            } else {
                addReadableJava(path, uri, versions, texts, javaFiles);
            }
        }
    }

    private static void addReadableJava(final Path path, final URI uri, final Map<URI, Integer> versions,
                                        final Map<URI, String> texts, final List<File> javaFiles) {
        try {
            texts.put(uri, Files.readString(path, StandardCharsets.UTF_8));
            versions.put(uri, -1);
            addJavaFile(javaFiles, uri);
        } catch (IOException ignored) {
            // skip unreadable files
        }
    }

    private static void addJavaFile(final List<File> javaFiles, final URI uri) {
        if (uri == null || !"file".equalsIgnoreCase(uri.getScheme())) {
            return;
        }
        File file = Uris.toPath(uri).toFile();
        if (!javaFiles.contains(file)) {
            javaFiles.add(file);
        }
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
        System.setProperty(ASTTEST_ENABLE, Boolean.toString(settings.isAstTestEnabled()));
        System.setProperty(GRAPE_ENABLE, Boolean.toString(settings.isGrapeEnabled()));
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
        LoaderKey key = new LoaderKey(settings.getClasspath(), configuration.getClasspath(),
                settings.getExtra(), settings.isGrapeEnabled(), settings.isAstTestEnabled(), parentLoader);
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
        deleteJavacOutput();
    }

    private void deleteJavacOutput() {
        if (javacOutput == null) {
            return;
        }
        try (var stream = Files.walk(javacOutput)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            });
        } catch (IOException ignored) {
            // best-effort cleanup
        }
        javacOutput = null;
    }

    private record LoaderKey(List<String> classpath, List<String> configuredClasspath, Map<String, String> extra,
                             boolean grape, boolean astTest, ClassLoader parent) {

        LoaderKey {
            classpath = classpath == null ? List.of() : List.copyOf(classpath);
            configuredClasspath = configuredClasspath == null ? List.of() : List.copyOf(configuredClasspath);
            extra = extra == null || extra.isEmpty() ? Map.of() : Map.copyOf(extra);
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
                    && Objects.equals(classpath, key.classpath)
                    && Objects.equals(configuredClasspath, key.configuredClasspath)
                    && Objects.equals(extra, key.extra) && parent == key.parent;
            }

            @Override
            public int hashCode() {
                return Objects.hash(classpath, configuredClasspath, extra, grape, astTest,
                        System.identityHashCode(parent));
            }
        }
}
