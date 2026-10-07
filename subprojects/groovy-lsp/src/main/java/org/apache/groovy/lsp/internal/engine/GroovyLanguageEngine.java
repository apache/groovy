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
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter;
import org.apache.groovy.lsp.internal.feature.LanguageFeatures;
import org.apache.groovy.lsp.internal.feature.SemanticTokensService;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * In-process facade over the same compile snapshot the LSP handlers use.
 * {@link #open(Path)} reads a file; {@link #openBuffer} takes any URI.
 * Queries take that URI and 0-based UTF-16 positions and return JDK
 * types (no JSON-RPC, no LSP4J in the result records).
 * A non-{@code file:} URI keeps the compiler from treating the buffer as
 * a path. The engine does not scan workspace folders and does not reserve
 * buffer names.
 */
public final class GroovyLanguageEngine implements AutoCloseable {

    private static final int SNIPPET_CONTEXT_LINES = 3;
    private static final String LANGUAGE_GROOVY = "groovy";

    private final LanguageServerContext context;
    private final LanguageFeatures features;

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
        this(context, new LanguageFeatures());
    }

    /**
     * @param context existing session
     * @param features shared feature services
     */
    public GroovyLanguageEngine(final LanguageServerContext context, final LanguageFeatures features) {
        this.context = context == null ? new LanguageServerContext() : context;
        this.features = features == null ? new LanguageFeatures() : features;
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
        String language = Uris.isJavaFileName(path.getFileName().toString()) ? "java" : LANGUAGE_GROOVY;
        TextDocument existing = context.getDocuments().get(uri);
        int version = existing == null ? 1 : existing.getVersion() + 1;
        context.getDocuments().open(new TextDocumentItem(uri.toString(), language, version, text));
        context.recompile();
        return uri;
    }

    /**
     * Opens or replaces an in-memory buffer and compiles it. The URI is the
     * caller's. It need not be a {@code file:} URI.
     *
     * @param uri buffer identity
     * @param languageId {@code groovy} or {@code java}, or {@code null} for groovy
     * @param text source text
     * @return the normalized URI
     */
    public URI openBuffer(final URI uri, final String languageId, final String text) {
        URI normalized = requireUri(uri);
        TextDocument existing = context.getDocuments().get(normalized);
        int version = existing == null ? 1 : existing.getVersion() + 1;
        String language = languageId == null || languageId.isEmpty() ? LANGUAGE_GROOVY : languageId;
        context.getDocuments().open(new TextDocumentItem(normalized.toString(), language, version,
                text == null ? "" : text));
        context.recompile();
        return normalized;
    }

    /**
     * Replaces buffer text without compiling. The next query compiles when
     * the open version does not match the snapshot; {@link #compileNow()}
     * and {@link #scheduleCompile(long)} remain available for callers that
     * debounce.
     *
     * @param uri buffer identity
     * @param text source text
     */
    public void updateBuffer(final URI uri, final String text) {
        URI normalized = requireUri(uri);
        TextDocument existing = context.getDocuments().get(normalized);
        int version = existing == null ? 1 : existing.getVersion() + 1;
        String language = existing == null || existing.getLanguageId() == null ? LANGUAGE_GROOVY : existing.getLanguageId();
        context.getDocuments().open(new TextDocumentItem(normalized.toString(), language, version,
                text == null ? "" : text));
    }

    /**
     * Drops a buffer and recompiles the remaining documents.
     *
     * @param uri buffer identity
     */
    public void closeBuffer(final URI uri) {
        if (uri == null) {
            return;
        }
        context.getDocuments().close(uri.toString());
        context.recompile();
    }

    /**
     * Compiles open buffers immediately.
     */
    public void compileNow() {
        context.recompile();
    }

    /**
     * Compiles after {@code delayMs} of quiet on the session compile thread.
     *
     * @param delayMs debounce delay
     */
    public void scheduleCompile(final long delayMs) {
        context.scheduleRecompile(delayMs);
    }

    /**
     * Parent loader for the next compile.
     *
     * @param parentLoader loader, or {@code null} for the session default
     */
    public void setParentLoader(final ClassLoader parentLoader) {
        context.setParentLoader(parentLoader);
    }

    /**
     * Replaces the compile classpath. Empty or {@code null} clears it.
     *
     * @param entries jar and directory paths
     */
    public void setClasspath(final List<String> entries) {
        context.setSettings(context.getSettings().withClasspath(entries));
    }

    /**
     * Stores an opaque extra setting so the compile fingerprint changes
     * after a parent loader is mutated in place. Loader identity alone
     * does not see those mutations.
     *
     * @param key setting name
     * @param value setting value, or {@code null} to remove
     */
    public void putExtra(final String key, final String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        CompilerSettings current = context.getSettings();
        Map<String, String> extra = new LinkedHashMap<>(current.getExtra());
        if (value == null) {
            extra.remove(key);
        } else {
            extra.put(key, value);
        }
        context.setSettings(current.withExtra(extra));
    }

    /**
     * Diagnostics for an already open buffer. Does not re-read disk.
     *
     * @param uri buffer identity
     * @return diagnostics, never {@code null}
     */
    public List<Hit> diagnostics(final URI uri) {
        URI normalized = requireUri(uri);
        TextDocument open = context.getDocuments().get(normalized);
        if (open != null) {
            ensureFresh(open);
        }
        CompiledDocument compiled = context.getSnapshot().get(normalized);
        return context.diagnosticsFor(compiled).stream()
                .map(diagnostic -> toHit(normalized, diagnostic))
                .toList();
    }

    /**
     * Hover markdown for an open buffer.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @return markdown, never {@code null}
     */
    public String describe(final URI uri, final int line, final int character) {
        TextDocument document = documentAt(uri);
        return features.hovers().markdown(document, context.getSnapshot(),
                new Position(line, character), encoding());
    }

    /**
     * Definition targets in an open buffer.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @return locations, never {@code null}
     */
    public List<Site> definition(final URI uri, final int line, final int character) {
        TextDocument document = documentAt(uri);
        return features.navigation().definition(document, context.getSnapshot(),
                new Position(line, character), encoding()).stream()
                .map(this::toSite)
                .toList();
    }

    /**
     * Implementations in an open buffer.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @return locations, never {@code null}
     */
    public List<Site> implementations(final URI uri, final int line, final int character) {
        TextDocument document = documentAt(uri);
        return features.navigation().implementation(document, context.getSnapshot(),
                new Position(line, character), encoding()).stream()
                .map(this::toSite)
                .toList();
    }

    /**
     * References in an open buffer.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @param maxResults maximum locations to return
     * @return references
     */
    public Refs references(final URI uri, final int line, final int character, final int maxResults) {
        TextDocument document = documentAt(uri);
        List<Location> locations = features.navigation().references(document, context.getSnapshot(),
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
        List<WorkspaceSymbol> found = features.symbols().workspaceSymbols(
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
     * Computes a rename on an open buffer. Does not write files.
     * Unbound dynamic calls produce an empty result.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @param newName replacement identifier
     * @return edits
     */
    public Rename rename(final URI uri, final int line, final int character, final String newName) {
        TextDocument document = documentAt(uri);
        WorkspaceEdit edit = features.rename().rename(document, context.getSnapshot(),
                new Position(line, character), newName, encoding());
        Map<String, List<TextEdit>> changes = edit == null || edit.getChanges() == null
                ? Map.of() : edit.getChanges();
        List<Change> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, List<TextEdit>> entry : changes.entrySet()) {
            URI target = Uris.parse(entry.getKey());
            for (TextEdit textEdit : entry.getValue()) {
                Range range = textEdit.getRange();
                result.add(new Change(target, range.getStart().getLine(), range.getStart().getCharacter(),
                        range.getEnd().getLine(), range.getEnd().getCharacter(),
                        textEdit.getNewText() == null ? "" : textEdit.getNewText()));
                count++;
            }
        }
        return new Rename(newName == null ? "" : newName, count, List.copyOf(result));
    }

    /**
     * Completions at a 0-based UTF-16 position in an open buffer.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @return candidates, never {@code null}
     */
    public List<Candidate> complete(final URI uri, final int line, final int character) {
        TextDocument document = documentAt(uri);
        CompletionList list = features.completions().complete(document, context.getSnapshot(),
                new Position(line, character), encoding());
        if (list == null || list.getItems() == null) {
            return List.of();
        }
        return list.getItems().stream()
                .filter(Objects::nonNull)
                .map(GroovyLanguageEngine::toCandidate)
                .toList();
    }

    /**
     * Semantic tokens as absolute spans (not LSP delta encoding).
     *
     * @param uri buffer identity
     * @return tokens, never {@code null}
     */
    public List<Token> tokens(final URI uri) {
        TextDocument document = documentAt(uri);
        CompiledDocument compiled = context.getSnapshot().get(document);
        List<Token> tokens = new ArrayList<>();
        for (SemanticTokensService.TokenSpan span : features.semanticTokens().tokenSpans(compiled, encoding())) {
            int type = span.type();
            String typeName = type >= 0 && type < SemanticTokensService.TOKEN_TYPES.size()
                    ? SemanticTokensService.TOKEN_TYPES.get(type) : "";
            tokens.add(new Token(span.line(), span.character(), span.length(), typeName, span.modifiers()));
        }
        return tokens;
    }

    /**
     * Call signatures at a 0-based UTF-16 position.
     *
     * @param uri buffer identity
     * @param line 0-based line
     * @param character 0-based character
     * @return signatures, never {@code null}
     */
    public SignatureSet signatures(final URI uri, final int line, final int character) {
        TextDocument document = documentAt(uri);
        SignatureHelp help = features.signatureHelp().signatureHelp(document, context.getSnapshot(),
                new Position(line, character), encoding());
        if (help == null || help.getSignatures() == null || help.getSignatures().isEmpty()) {
            return new SignatureSet(0, 0, List.of());
        }
        List<Signature> signatures = help.getSignatures().stream()
                .map(GroovyLanguageEngine::toSignature)
                .toList();
        int activeSignature = help.getActiveSignature() == null ? 0 : help.getActiveSignature();
        int activeParameter = help.getActiveParameter() == null ? 0 : help.getActiveParameter();
        return new SignatureSet(activeSignature, activeParameter, List.copyOf(signatures));
    }

    /**
     * Indent-only format of an open buffer.
     *
     * @param uri buffer identity
     * @param tabSize spaces per indent level
     * @param insertSpaces whether to use spaces
     * @return edits, never {@code null}
     */
    public List<Change> format(final URI uri, final int tabSize, final boolean insertSpaces) {
        TextDocument document = documentAt(uri);
        return toChanges(uri, features.formatting().format(document, null, tabSize, insertSpaces, encoding()));
    }

    /**
     * Organize-imports edits for an open buffer. Does not write files.
     *
     * @param uri buffer identity
     * @return edits, never {@code null}
     */
    public List<Change> organizeImports(final URI uri) {
        URI normalized = requireUri(uri);
        TextDocument open = context.getDocuments().get(normalized);
        if (open != null) {
            ensureFresh(open);
        }
        CompiledDocument compiled = context.getSnapshot().get(normalized);
        return toChanges(normalized, features.codeActions().organizeImports(compiled, encoding()));
    }

    @Override
    public void close() {
        context.close();
    }

    private TextDocument documentAt(final URI uri) {
        URI normalized = requireUri(uri);
        TextDocument document = context.documentFor(normalized.toString());
        if (document == null) {
            throw new IllegalStateException("not compiled: " + normalized);
        }
        ensureFresh(document);
        return document;
    }

    private void ensureFresh(final TextDocument document) {
        CompiledDocument compiled = context.getSnapshot().get(document);
        if (compiled == null || compiled.getVersion() != document.getVersion()) {
            context.recompile();
        }
    }

    private static URI requireUri(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri");
        }
        return Uris.normalize(uri);
    }

    private PositionEncoding encoding() {
        return context.getPositionEncoding();
    }

    private List<Change> toChanges(final URI uri, final List<TextEdit> edits) {
        List<Change> result = new ArrayList<>();
        if (edits == null) {
            return result;
        }
        URI normalized = Uris.normalize(uri);
        for (TextEdit edit : edits) {
            if (edit == null || edit.getRange() == null) {
                continue;
            }
            Range range = edit.getRange();
            result.add(new Change(normalized, range.getStart().getLine(), range.getStart().getCharacter(),
                    range.getEnd().getLine(), range.getEnd().getCharacter(),
                    edit.getNewText() == null ? "" : edit.getNewText()));
        }
        return result;
    }

    private static Candidate toCandidate(final CompletionItem item) {
        String kind = item.getKind() == null ? "" : item.getKind().name();
        String insert = item.getInsertText() == null ? item.getLabel() : item.getInsertText();
        return new Candidate(item.getLabel() == null ? "" : item.getLabel(), kind,
                item.getDetail() == null ? "" : item.getDetail(),
                insert == null ? "" : insert,
                markupOrString(item.getDocumentation()));
    }

    private static Signature toSignature(final SignatureInformation information) {
        List<String> parameters = new ArrayList<>();
        if (information.getParameters() != null) {
            for (ParameterInformation parameter : information.getParameters()) {
                if (parameter == null || parameter.getLabel() == null) {
                    parameters.add("");
                } else {
                    parameters.add(parameterLabel(parameter));
                }
            }
        }
        return new Signature(information.getLabel() == null ? "" : information.getLabel(),
                markupOrString(information.getDocumentation()),
                List.copyOf(parameters));
    }

    private static String parameterLabel(final ParameterInformation parameter) {
        Either<String, ?> label = parameter.getLabel();
        if (label == null || !label.isLeft() || label.getLeft() == null) {
            return "";
        }
        return label.getLeft();
    }

    private static String markupOrString(final Either<String, MarkupContent> either) {
        if (either == null) {
            return "";
        }
        if (either.isLeft()) {
            return either.getLeft() == null ? "" : either.getLeft();
        }
        MarkupContent content = either.getRight();
        return content == null || content.getValue() == null ? "" : content.getValue();
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

    /**
     * A completion candidate. {@code kind} is the LSP completion-item kind name.
     */
    public record Candidate(String label, String kind, String detail, String insertText, String documentation) {
    }

    /**
     * An absolute semantic token span in 0-based UTF-16 coordinates.
     */
    public record Token(int line, int character, int length, String type, int modifiers) {
    }

    /**
     * One call signature.
     */
    public record Signature(String label, String documentation, List<String> parameters) {
    }

    /**
     * Signature help at the caret.
     */
    public record SignatureSet(int activeSignature, int activeParameter, List<Signature> signatures) {
    }
}
