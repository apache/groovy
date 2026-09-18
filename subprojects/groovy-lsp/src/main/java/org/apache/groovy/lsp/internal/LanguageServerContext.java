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
package org.apache.groovy.lsp.internal;

import org.apache.groovy.lsp.internal.compile.CompilationSnapshot;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.compile.GroovyCompiler;
import org.apache.groovy.lsp.internal.compile.WorkspaceScanner;
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter;
import org.apache.groovy.lsp.internal.feature.CompletionService;
import org.apache.groovy.lsp.internal.feature.HoverService;
import org.apache.groovy.lsp.internal.feature.NavigationService;
import org.apache.groovy.lsp.internal.feature.RenameService;
import org.apache.groovy.lsp.internal.feature.SupportServices;
import org.apache.groovy.lsp.internal.feature.SymbolService;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.compile.ImportSupport;
import org.apache.groovy.lsp.internal.compile.WorkspaceLayout;
import org.apache.groovy.lsp.internal.feature.HierarchyService;
import org.apache.groovy.lsp.internal.protocol.ClientFeatures;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.DocumentStore;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.apache.groovy.lsp.spi.GroovyLspSession;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mutable session state for one language-server process.
 */
public final class LanguageServerContext implements GroovyLspSession {

    private final ExtensionHost extensions;
    private final DocumentStore documents = new DocumentStore();
    private final GroovyCompiler compiler = new GroovyCompiler();
    private final WorkspaceScanner scanner = new WorkspaceScanner();
    private final DiagnosticConverter diagnostics = new DiagnosticConverter();
    private final CompletionService completions = new CompletionService();
    private final HoverService hovers = new HoverService();
    private final NavigationService navigation = new NavigationService();
    private final SymbolService symbols = new SymbolService();
    private final RenameService rename = new RenameService();
    private final SupportServices support = new SupportServices();
    private final HierarchyService hierarchy = new HierarchyService();
    private final Object compileLock = new Object();

    private final AtomicReference<LanguageClient> client = new AtomicReference<>();
    private final AtomicReference<ClientCapabilities> clientCapabilities = new AtomicReference<>();
    private final AtomicReference<PositionEncoding> positionEncoding = new AtomicReference<>(PositionEncoding.UTF16);
    private final AtomicReference<CompilerSettings> settings = new AtomicReference<>(CompilerSettings.defaults());
    private final AtomicReference<CompilationSnapshot> snapshot = new AtomicReference<>(CompilationSnapshot.EMPTY);
    private volatile boolean initialized;
    private volatile boolean shutdown;
    private volatile Integer exitCode;
    private final Set<URI> workspaceFolders = Collections.synchronizedSet(new LinkedHashSet<>());

    private final ScheduledExecutorService debounce = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "groovy-lsp-compile");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> pendingCompile;
    private long compileGeneration;
    private String compileFingerprint;

    /**
     * Creates a session and loads {@code GroovyLspExtension} registrations
     * from this class's loader.
     */
    public LanguageServerContext() {
        this(ExtensionHost.discover(LanguageServerContext.class.getClassLoader()));
    }

    /**
     * Creates a session with an explicit plugin host. Tests use this to
     * register a fake extension without {@code META-INF/services}.
     *
     * @param extensions plugin host, or {@code null} for none
     */
    public LanguageServerContext(final ExtensionHost extensions) {
        this.extensions = extensions == null ? ExtensionHost.none() : extensions;
    }

    /**
     * Compiles open documents plus workspace sources and publishes diagnostics.
     * Unchanged inputs reuse the last snapshot.
     */
    public CompilationSnapshot recompile() {
        synchronized (compileLock) {
            final long generation;
            final CompilerSettings effective;
            final List<Path> extra;
            final Collection<TextDocument> open;
            final String fingerprint;
            final boolean reuse;
            synchronized (this) {
                if (shutdown) {
                    return snapshot.get();
                }
                if (pendingCompile != null) {
                    pendingCompile.cancel(false);
                    pendingCompile = null;
                }
                List<URI> folders;
                synchronized (workspaceFolders) {
                    folders = new ArrayList<>(workspaceFolders);
                }
                effective = WorkspaceLayout.withInferred(settings.get(), folders);
                extra = scanner.scan(folders, effective.getSourcePaths());
                open = documents.snapshot();
                fingerprint = fingerprint(open, extra, effective) + '|' + extensions.ids();
                if (fingerprint.equals(compileFingerprint) && snapshot.get() != CompilationSnapshot.EMPTY) {
                    // Another compile already committed this snapshot but may
                    // not have published yet. Fall through to publishDiagnostics.
                    reuse = true;
                    generation = compileGeneration;
                } else {
                    reuse = false;
                    generation = ++compileGeneration;
                }
            }
            if (!reuse) {
                String token = "groovy-compile-" + generation;
                beginProgress(token, "Compiling Groovy");
                CompilationSnapshot compiled;
                try {
                    compiled = compiler.compile(open, extra, effective, getClass().getClassLoader(), extensions, this);
                } finally {
                    endProgress(token);
                }
                synchronized (this) {
                    if (generation == compileGeneration) {
                        compileFingerprint = fingerprint;
                        snapshot.set(compiled);
                    }
                }
            }
        }
        publishDiagnostics();
        return snapshot.get();
    }

    /**
     * Schedules a recompile after {@code delayMs} of quiet.
     *
     * @param delayMs debounce delay
     */
    public synchronized void scheduleRecompile(final long delayMs) {
        if (shutdown || debounce.isShutdown()) {
            return;
        }
        if (pendingCompile != null) {
            pendingCompile.cancel(false);
        }
        pendingCompile = debounce.schedule(this::recompile, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Publishes diagnostics for every compiled document.
     */
    public void publishDiagnostics() {
        LanguageClient languageClient = client.get();
        if (languageClient == null) {
            return;
        }
        for (var document : snapshot.get().documents()) {
            languageClient.publishDiagnostics(new PublishDiagnosticsParams(
                    document.getUri().toString(), diagnosticsFor(document)));
        }
    }

    /**
     * Compiler, unused-import and plugin diagnostics for one document.
     * Used by both push and pull diagnostic paths.
     *
     * @param document compiled document
     * @return diagnostics, never {@code null}
     */
    public List<Diagnostic> diagnosticsFor(final CompiledDocument document) {
        if (document == null) {
            return List.of();
        }
        PositionEncoding encoding = positionEncoding.get();
        List<Diagnostic> converted = new ArrayList<>(diagnostics.convert(document, encoding));
        converted.addAll(ImportSupport.unusedDiagnostics(document, encoding));
        converted.addAll(extensions.extraDiagnostics(document));
        return converted;
    }

    @Override
    public void applyEdit(final WorkspaceEdit edit) {
        LanguageClient languageClient = client.get();
        if (languageClient == null || edit == null
                || !ClientFeatures.applyEdit(clientCapabilities.get())) {
            return;
        }
        languageClient.applyEdit(new ApplyWorkspaceEditParams(edit));
    }

    @Override
    public String documentText(final URI uri) {
        if (uri == null) {
            return null;
        }
        TextDocument open = documents.get(uri);
        if (open != null) {
            return open.getText();
        }
        CompiledDocument compiled = snapshot.get().get(uri);
        return compiled == null ? null : compiled.getText();
    }

    @Override
    public List<URI> workspaceFolders() {
        synchronized (workspaceFolders) {
            return List.copyOf(workspaceFolders);
        }
    }

    @Override
    public List<String> classpath() {
        return settings.get().getClasspath();
    }

    @Override
    public List<String> sourcePaths() {
        return settings.get().getSourcePaths();
    }

    @Override
    public Map<String, String> extraSettings() {
        return settings.get().getExtra();
    }

    @Override
    public String positionEncoding() {
        return positionEncoding.get().protocolName();
    }

    private void beginProgress(final String token, final String title) {
        LanguageClient languageClient = client.get();
        if (languageClient == null || !ClientFeatures.workDoneProgress(clientCapabilities.get())) {
            return;
        }
        try {
            WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
            begin.setTitle(title);
            begin.setCancellable(false);
            languageClient.createProgress(new WorkDoneProgressCreateParams(Either.forLeft(token)));
            languageClient.notifyProgress(new ProgressParams(Either.forLeft(token), Either.forLeft(begin)));
        } catch (RuntimeException ignored) {
            // progress is optional
        }
    }

    private void endProgress(final String token) {
        LanguageClient languageClient = client.get();
        if (languageClient == null || !ClientFeatures.workDoneProgress(clientCapabilities.get())) {
            return;
        }
        try {
            languageClient.notifyProgress(new ProgressParams(Either.forLeft(token),
                    Either.forLeft(new WorkDoneProgressEnd())));
        } catch (RuntimeException ignored) {
            // progress is optional
        }
    }

    /**
     * Releases background threads.
     */
    public synchronized void close() {
        shutdown = true;
        if (pendingCompile != null) {
            pendingCompile.cancel(false);
            pendingCompile = null;
        }
        debounce.shutdownNow();
        compiler.close();
        documents.clear();
        extensions.shutdown();
    }

    public DocumentStore getDocuments() {
        return documents;
    }

    /**
     * Open buffer for {@code uri}, or a snapshot of the last compiled text
     * when the file is not open.
     *
     * @param uri document URI
     * @return the document, or {@code null}
     */
    public TextDocument documentFor(final String uri) {
        TextDocument document = documents.get(uri);
        if (document != null) {
            return document;
        }
        CompiledDocument compiled = snapshot.get().get(uri);
        return compiled == null ? null : compiled.toTextDocument();
    }

    public GroovyCompiler getCompiler() {
        return compiler;
    }

    public WorkspaceScanner getScanner() {
        return scanner;
    }

    public DiagnosticConverter getDiagnostics() {
        return diagnostics;
    }

    public CompletionService getCompletions() {
        return completions;
    }

    public HoverService getHovers() {
        return hovers;
    }

    public NavigationService getNavigation() {
        return navigation;
    }

    public SymbolService getSymbols() {
        return symbols;
    }

    public RenameService getRename() {
        return rename;
    }

    public SupportServices getSupport() {
        return support;
    }

    public HierarchyService getHierarchy() {
        return hierarchy;
    }

    public ExtensionHost getExtensions() {
        return extensions;
    }

    public LanguageClient getClient() {
        return client.get();
    }

    public void setClient(final LanguageClient client) {
        this.client.set(client);
    }

    public ClientCapabilities getClientCapabilities() {
        return clientCapabilities.get();
    }

    public void setClientCapabilities(final ClientCapabilities clientCapabilities) {
        this.clientCapabilities.set(clientCapabilities);
    }

    public PositionEncoding getPositionEncoding() {
        return positionEncoding.get();
    }

    public void setPositionEncoding(final PositionEncoding positionEncoding) {
        this.positionEncoding.set(positionEncoding);
    }

    public CompilerSettings getSettings() {
        return settings.get();
    }

    public void setSettings(final CompilerSettings settings) {
        this.settings.set(settings);
        compileFingerprint = null;
    }

    public CompilationSnapshot getSnapshot() {
        return snapshot.get();
    }

    public void setSnapshot(final CompilationSnapshot snapshot) {
        this.snapshot.set(snapshot);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(final boolean initialized) {
        boolean was = this.initialized;
        this.initialized = initialized;
        if (initialized && !was) {
            extensions.initialized(this);
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(final boolean shutdown) {
        this.shutdown = shutdown;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(final Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Set<URI> getWorkspaceFolders() {
        return workspaceFolders;
    }

    /**
     * Adds a workspace folder after normalizing {@code file:} drive letters.
     *
     * @param uri folder URI
     */
    public void addWorkspaceFolder(final URI uri) {
        if (uri != null) {
            workspaceFolders.add(Uris.normalize(uri));
        }
    }

    /**
     * Removes a workspace folder using the same normalization as add.
     *
     * @param uri folder URI
     */
    public void removeWorkspaceFolder(final URI uri) {
        if (uri != null) {
            workspaceFolders.remove(Uris.normalize(uri));
        }
    }

    /**
     * Stable key for the current compile inputs. Equal keys skip recompilation.
     *
     * @param open open documents
     * @param extra workspace files
     * @param settings effective settings
     * @return a fingerprint
     */
    public static String fingerprint(final Collection<TextDocument> open, final Collection<Path> extra,
                                     final CompilerSettings settings) {
        StringBuilder builder = new StringBuilder();
        builder.append(settings.getClasspath()).append('|')
                .append(settings.getSourcePaths()).append('|')
                .append(settings.isGrapeEnabled()).append('|')
                .append(settings.isAstTestEnabled()).append('|')
                .append(settings.getThroughPhase()).append('|')
                .append(settings.getExtra());
        if (open != null) {
            for (TextDocument document : open) {
                builder.append('\n').append(document.getUri()).append(':')
                        .append(document.getVersion()).append(':')
                        .append(document.getText().hashCode());
            }
        }
        if (extra != null) {
            for (Path path : extra) {
                long mtime = 0L;
                long size = 0L;
                try {
                    mtime = Files.getLastModifiedTime(path).toMillis();
                    size = Files.size(path);
                } catch (Exception ignored) {
                    // missing files still participate as a zero stamp
                }
                builder.append('\n').append(path).append(':').append(mtime).append(':').append(size);
            }
        }
        return builder.toString();
    }
}
