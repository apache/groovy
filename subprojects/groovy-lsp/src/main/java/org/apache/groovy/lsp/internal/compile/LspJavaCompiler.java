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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import org.apache.groovy.lsp.internal.util.Uris;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.javac.JavaCompiler;
import org.codehaus.groovy.tools.javac.JavaCompilerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Joint-compilation javac backend for groovy-lsp. Uses the public
 * {@code javax.tools} / {@code com.sun.source} APIs (no {@code --add-exports}).
 * Open buffers overlay the filesystem; stub sources stay in memory
 * ({@code memStub}).
 */
public final class LspJavaCompiler implements JavaCompiler, JavaCompilerFactory {

    private final Map<URI, String> overlays;
    private JavaSymbolIndex index = JavaSymbolIndex.EMPTY;
    private final Map<URI, List<JavaMessage>> messages = new LinkedHashMap<>();

    /**
     * @param overlays open Java buffers keyed by normalized URI
     */
    public LspJavaCompiler(final Map<URI, String> overlays) {
        this.overlays = overlays == null ? Map.of() : Map.copyOf(overlays);
    }

    /**
     * @return types and members from the last javac run
     */
    public JavaSymbolIndex index() {
        return index;
    }

    /**
     * @return javac diagnostics from the last run, keyed by source URI
     */
    public Map<URI, List<JavaMessage>> messages() {
        return Map.copyOf(messages);
    }

    @Override
    public JavaCompiler createCompiler(final CompilerConfiguration config) {
        return this;
    }

    @Override
    public void compile(final List<String> files, final CompilationUnit cu) {
        index = JavaSymbolIndex.EMPTY;
        messages.clear();
        var javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null || cu == null) {
            return;
        }
        CompilerConfiguration config = cu.getConfiguration();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Charset charset = Charset.forName(config.getSourceEncoding());
        try (StandardJavaFileManager fileManager = javac.getStandardFileManager(diagnostics, Locale.ENGLISH, charset)) {
            Set<JavaFileObject> units = new LinkedHashSet<>();
            if (cu.getJavaCompilationUnitSet() != null) {
                units.addAll(cu.getJavaCompilationUnitSet());
            }
            addSources(units, files, fileManager);
            JavacTask task = (JavacTask) javac.getTask(null, fileManager, diagnostics, parameters(config),
                    null, units);
            Trees treeApi = Trees.instance(task);
            Iterable<? extends CompilationUnitTree> trees = task.parse();
            task.analyze();
            task.generate();
            index = JavaSymbolIndex.of(treeApi, trees);
            collectDiagnostics(diagnostics);
        } catch (IOException | RuntimeException ignored) {
            // keep whatever diagnostics were collected
        }
    }

    private void addSources(final Set<JavaFileObject> units, final List<String> files,
                            final StandardJavaFileManager fileManager) {
        Set<URI> seen = recordedUris(units);
        addOverlays(units, seen);
        addRemainingFiles(units, files, fileManager, seen);
    }

    private static Set<URI> recordedUris(final Set<JavaFileObject> units) {
        Set<URI> seen = new LinkedHashSet<>();
        for (JavaFileObject unit : units) {
            if (unit != null && unit.toUri() != null) {
                seen.add(Uris.normalize(unit.toUri()));
            }
        }
        return seen;
    }

    private void addOverlays(final Set<JavaFileObject> units, final Set<URI> seen) {
        for (Map.Entry<URI, String> overlay : overlays.entrySet()) {
            URI uri = Uris.normalize(overlay.getKey());
            if (seen.add(uri)) {
                units.add(new BufferJavaFileObject(uri, overlay.getValue()));
            }
        }
    }

    private void addRemainingFiles(final Set<JavaFileObject> units, final List<String> files,
                                   final StandardJavaFileManager fileManager, final Set<URI> seen) {
        if (files == null) {
            return;
        }
        List<File> remaining = new ArrayList<>();
        for (String file : files) {
            if (file != null) {
                addIfNewFile(remaining, file, seen);
            }
        }
        if (!remaining.isEmpty()) {
            for (JavaFileObject object : fileManager.getJavaFileObjectsFromFiles(remaining)) {
                units.add(object);
            }
        }
    }

    private void addIfNewFile(final List<File> remaining, final String file, final Set<URI> seen) {
        Path path = Path.of(file);
        URI uri = Uris.normalize(path.toUri());
        if (!overlays.containsKey(uri) && seen.add(uri) && Files.isRegularFile(path)) {
            remaining.add(path.toFile());
        }
    }

    private void collectDiagnostics(final DiagnosticCollector<JavaFileObject> diagnostics) {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            URI uri = diagnosticUri(diagnostic);
            if (uri != null) {
                messages.computeIfAbsent(uri, key -> new ArrayList<>()).add(toMessage(diagnostic));
            }
        }
    }

    private static URI diagnosticUri(final Diagnostic<? extends JavaFileObject> diagnostic) {
        if (diagnostic == null || diagnostic.getKind() == Diagnostic.Kind.NOTE) {
            return null;
        }
        JavaFileObject source = diagnostic.getSource();
        if (source == null || source.toUri() == null) {
            return null;
        }
        URI uri = Uris.normalize(source.toUri());
        return "string".equalsIgnoreCase(uri.getScheme()) ? null : uri;
    }

    private static JavaMessage toMessage(final Diagnostic<? extends JavaFileObject> diagnostic) {
        int line = bounded(diagnostic.getLineNumber());
        int column = bounded(diagnostic.getColumnNumber());
        String text = diagnostic.getMessage(Locale.ENGLISH);
        boolean error = diagnostic.getKind() == Diagnostic.Kind.ERROR;
        return new JavaMessage(line, column, line, column + 1, text == null ? "" : text, error);
    }

    private static int bounded(final long value) {
        if (value <= 0L || value > Integer.MAX_VALUE) {
            return 1;
        }
        return (int) value;
    }

    private static List<String> parameters(final CompilerConfiguration config) {
        List<String> params = new ArrayList<>();
        File target = config.getTargetDirectory();
        if (target == null) {
            target = new File(".");
        }
        params.add("-d");
        params.add(target.getAbsolutePath());
        params.add("-proc:none");
        params.add("--release");
        params.add(config.getTargetBytecode());
        List<String> classpath = new ArrayList<>(config.getClasspath());
        try {
            var codeSource = GroovyObject.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                classpath.add(new File(codeSource.getLocation().toURI()).getPath());
            }
        } catch (Exception ignored) {
            // groovy runtime already on the compiler classpath in tests
        }
        if (!classpath.isEmpty()) {
            params.add("-classpath");
            params.add(String.join(File.pathSeparator, classpath));
        }
        return params;
    }

    private static final class BufferJavaFileObject extends SimpleJavaFileObject {
        private final String content;

        private BufferJavaFileObject(final URI uri, final String content) {
            super(uri, Kind.SOURCE);
            this.content = content == null ? "" : content;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return content;
        }
    }
}
