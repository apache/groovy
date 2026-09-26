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
package org.apache.groovy.lsp.internal.engine;

import org.apache.groovy.lsp.internal.LanguageServerContext;
import org.apache.groovy.lsp.internal.compile.CompiledDocument;
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter;
import org.apache.groovy.lsp.internal.feature.HoverService;
import org.apache.groovy.lsp.internal.feature.NavigationService;
import org.apache.groovy.lsp.internal.feature.RenameService;
import org.apache.groovy.lsp.internal.feature.SymbolService;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceSymbol;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In-process facade over the same compile snapshot the LSP handlers use.
 * Callers pass paths and 0-based UTF-16 positions and get JDK types back
 * (no JSON-RPC, no LSP4J in the result records). A later MCP adapter can
 * wrap this engine.
 */
public final class GroovyLanguageEngine implements AutoCloseable {

    private static final int SNIPPET_CONTEXT_LINES = 3;

    private final LanguageServerContext context;
    private final HoverService hovers = new HoverService();
    private final NavigationService navigation = new NavigationService();
    private final SymbolService symbols = new SymbolService();
    private final RenameService rename = new RenameService();

    /**
     * Creates an engine with a fresh session. It does not scan a workspace
     * folder, so a checkout root is never compiled by accident.
     */
    public GroovyLanguageEngine() {
        this(new LanguageServerContext());
    }

    /**
     * @param context existing session
     */
    public GroovyLanguageEngine(final LanguageServerContext context) {
        this.context = context == null ? new LanguageServerContext() : context;
    }

    /**
     * @return the session this engine drives
     */
    public LanguageServerContext context() {
        return context;
    }

    /**
     * Reads {@code path} from disk, overlays the open buffer, and compiles.
     *
     * @param path source file
     * @return the document URI
     */
    public URI open(final Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("not a file: " + path);
        }
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("unreadable: " + path, ex);
        }
        URI uri = Uris.normalize(path.toUri());
        String language = Uris.isJavaFileName(path.getFileName().toString()) ? "java" : "groovy";
        TextDocument existing = context.getDocuments().get(uri);
        int version = existing == null ? 1 : existing.getVersion() + 1;
        context.getDocuments().open(new TextDocumentItem(uri.toString(), language, version, text));
        context.recompile();
        return uri;
    }

    /**
     * Compiler and unused-import diagnostics for {@code path} at its
     * current on-disk content.
     *
     * @param path source file
     * @return diagnostics, never {@code null}
     */
    public List<Hit> diagnostics(final Path path) {
        URI uri = open(path);
        CompiledDocument compiled = context.getSnapshot().get(uri);
        List<Hit> hits = new ArrayList<>();
        for (Diagnostic diagnostic : context.diagnosticsFor(compiled)) {
            hits.add(toHit(uri, diagnostic));
        }
        return hits;
    }

    /**
     * Hover markdown at a 0-based UTF-16 position, or empty.
     *
     * @param path source file
     * @param line 0-based line
     * @param character 0-based character
     * @return markdown
     */
    public String describe(final Path path, final int line, final int character) {
        TextDocument document = documentAt(path);
        return hovers.markdown(document, context.getSnapshot(), new Position(line, character), encoding());
    }

    /**
     * Definition targets at a 0-based UTF-16 position, each with a snippet.
     *
     * @param path source file
     * @param line 0-based line
     * @param character 0-based character
     * @return locations, never {@code null}
     */
    public List<Site> definition(final Path path, final int line, final int character) {
        TextDocument document = documentAt(path);
        List<Site> sites = new ArrayList<>();
        for (LocationLink link : navigation.definition(document, context.getSnapshot(),
                new Position(line, character), encoding())) {
            sites.add(toSite(link));
        }
        return sites;
    }

    /**
     * Implementations at a 0-based UTF-16 position.
     *
     * @param path source file
     * @param line 0-based line
     * @param character 0-based character
     * @return locations, never {@code null}
     */
    public List<Site> implementations(final Path path, final int line, final int character) {
        TextDocument document = documentAt(path);
        List<Site> sites = new ArrayList<>();
        for (LocationLink link : navigation.implementation(document, context.getSnapshot(),
                new Position(line, character), encoding())) {
            sites.add(toSite(link));
        }
        return sites;
    }

    /**
     * References at a 0-based UTF-16 position, capped to {@code maxResults}.
     *
     * @param path source file
     * @param line 0-based line
     * @param character 0-based character
     * @param maxResults maximum locations to return
     * @return references
     */
    public Refs references(final Path path, final int line, final int character, final int maxResults) {
        TextDocument document = documentAt(path);
        List<Location> locations = navigation.references(document, context.getSnapshot(),
                new Position(line, character), encoding(), true);
        int cap = Math.max(0, maxResults);
        List<Site> sites = new ArrayList<>();
        int limit = Math.min(cap, locations.size());
        for (int i = 0; i < limit; i++) {
            sites.add(toSite(locations.get(i)));
        }
        return new Refs(locations.size(), locations.size() > cap, List.copyOf(sites));
    }

    /**
     * Workspace symbols matching {@code query} (substring or CamelHumps).
     *
     * @param query search text
     * @param maxResults maximum hits
     * @return symbols, never {@code null}
     */
    public List<Symbol> symbols(final String query, final int maxResults) {
        List<WorkspaceSymbol> found = symbols.workspaceSymbols(
                context.getSnapshot(), query, encoding());
        int cap = Math.max(0, maxResults);
        List<Symbol> hits = new ArrayList<>();
        int limit = Math.min(cap, found.size());
        for (int i = 0; i < limit; i++) {
            hits.add(toSymbol(found.get(i)));
        }
        return List.copyOf(hits);
    }

    /**
     * Computes a rename but does not write files. Unbound dynamic calls
     * produce an empty result.
     *
     * @param path source file
     * @param line 0-based line
     * @param character 0-based character
     * @param newName replacement identifier
     * @return edits
     */
    public Rename rename(final Path path, final int line, final int character, final String newName) {
        TextDocument document = documentAt(path);
        WorkspaceEdit edit = rename.rename(document, context.getSnapshot(),
                new Position(line, character), newName, encoding());
        Map<String, List<TextEdit>> changes = edit == null || edit.getChanges() == null
                ? Map.of() : edit.getChanges();
        List<Change> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, List<TextEdit>> entry : changes.entrySet()) {
            URI uri = Uris.parse(entry.getKey());
            for (TextEdit textEdit : entry.getValue()) {
                Range range = textEdit.getRange();
                result.add(new Change(uri, range.getStart().getLine(), range.getStart().getCharacter(),
                        range.getEnd().getLine(), range.getEnd().getCharacter(),
                        textEdit.getNewText() == null ? "" : textEdit.getNewText()));
                count++;
            }
        }
        return new Rename(newName == null ? "" : newName, count, List.copyOf(result));
    }

    @Override
    public void close() {
        context.close();
    }

    private TextDocument documentAt(final Path path) {
        URI uri = open(path);
        TextDocument document = context.documentFor(uri.toString());
        if (document == null) {
            throw new IllegalStateException("not compiled: " + path);
        }
        return document;
    }

    private PositionEncoding encoding() {
        return context.getPositionEncoding();
    }

    private Hit toHit(final URI uri, final Diagnostic diagnostic) {
        Range range = diagnostic.getRange();
        Position start = range == null ? new Position(0, 0) : range.getStart();
        Position end = range == null ? new Position(0, 0) : range.getEnd();
        String severity = diagnostic.getSeverity() == null ? "Error" : diagnostic.getSeverity().name();
        String message = DiagnosticConverter.diagnosticMessage(diagnostic);
        return new Hit(uri, start.getLine(), start.getCharacter(), end.getLine(), end.getCharacter(),
                message == null ? "" : message, severity);
    }

    private Site toSite(final LocationLink link) {
        URI uri = Uris.parse(link.getTargetUri());
        Range range = link.getTargetSelectionRange() == null ? link.getTargetRange() : link.getTargetSelectionRange();
        return toSite(uri, range);
    }

    private Site toSite(final Location location) {
        return toSite(Uris.parse(location.getUri()), location.getRange());
    }

    private Site toSite(final URI uri, final Range range) {
        Range use = range == null ? new Range(new Position(0, 0), new Position(0, 0)) : range;
        String text = context.documentText(uri);
        if (text == null) {
            CompiledDocument compiled = context.getSnapshot().get(uri);
            text = compiled == null ? "" : compiled.getText();
        }
        Position start = use.getStart();
        Position end = use.getEnd();
        return new Site(uri, start.getLine(), start.getCharacter(), end.getLine(), end.getCharacter(),
                snippet(text, start.getLine(), end.getLine()));
    }

    private Symbol toSymbol(final WorkspaceSymbol symbol) {
        Location location = symbol.getLocation() != null && symbol.getLocation().isLeft()
                ? symbol.getLocation().getLeft() : null;
        Site site = location == null
                ? new Site(URI.create("file:///"), 0, 0, 0, 0, "")
                : toSite(location);
        String kind = symbol.getKind() == null ? "Object" : symbol.getKind().name();
        return new Symbol(symbol.getName(), kind, site);
    }

    static String snippet(final String text, final int startLine, final int endLine) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int from = Math.max(0, startLine - SNIPPET_CONTEXT_LINES);
        int to = Math.max(from, endLine + SNIPPET_CONTEXT_LINES);
        StringBuilder builder = new StringBuilder();
        for (int line = from; line <= to; line++) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(Positions.lineText(text, line + 1));
        }
        return builder.toString();
    }

    /**
     * A compiler diagnostic in 0-based UTF-16 coordinates.
     */
    public record Hit(URI uri, int startLine, int startCharacter, int endLine, int endCharacter,
                      String message, String severity) {
    }

    /**
     * A navigation target plus surrounding source.
     */
    public record Site(URI uri, int startLine, int startCharacter, int endLine, int endCharacter,
                       String snippet) {
    }

    /**
     * Paginated references.
     */
    public record Refs(int total, boolean truncated, List<Site> locations) {
    }

    /**
     * A workspace symbol hit.
     */
    public record Symbol(String name, String kind, Site location) {
    }

    /**
     * One text replacement. The engine does not write files.
     */
    public record Change(URI uri, int startLine, int startCharacter, int endLine, int endCharacter,
                         String newText) {
    }

    /**
     * Rename edits for the caller to apply.
     */
    public record Rename(String newName, int changeCount, List<Change> changes) {
    }
}
