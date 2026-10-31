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
import org.apache.groovy.lsp.internal.compile.ScriptNames;
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter;
import org.apache.groovy.lsp.internal.feature.LanguageFeatures;
import org.apache.groovy.lsp.internal.feature.SemanticTokensService;
import org.apache.groovy.lsp.internal.position.PositionEncoding;
import org.apache.groovy.lsp.internal.position.Positions;
import org.apache.groovy.lsp.internal.util.GroovyKeywords;
import org.apache.groovy.lsp.internal.util.GroovySourceTokens;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ResolveVisitor;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * In-process facade over the same compile snapshot the LSP handlers use.
 * {@link #open(Path)} reads a file; {@link #openBuffer} takes any URI.
 * Queries take that URI and 0-based UTF-16 positions and return JDK
 * types (no JSON-RPC, no LSP4J in the result records).
 * A non-{@code file:} URI keeps the compiler from treating the buffer as
 * a path. The engine does not scan workspace folders and does not reserve
 * buffer names.
 * <p>
 * Query methods that take the buffer text replace it first. The same text
 * keeps the document version, so the snapshot can be reused.
 * {@link #setHostConfiguration} drops that cache on every call. Hosts
 * invoke it when the configuration is replaced or edited, not before
 * every query. {@link #lexicalTokens(String)} is the live highlighter:
 * no compile, safe on the Swing EDT. {@link #tokens(URI)} needs a
 * compiled snapshot.
 */
public final class GroovyLanguageEngine implements AutoCloseable {

    private static final int SNIPPET_CONTEXT_LINES = 3;
    private static final String LANGUAGE_GROOVY = "groovy";
    private static final Pattern SNIPPET_PLACEHOLDER = Pattern.compile("\\$\\{\\d+:([^}]*)}");
    private static final Pattern SNIPPET_TABSTOP = Pattern.compile("\\$\\d+");
    /**
     * Semantic-token legend names. Same list the protocol advertises.
     */
    public static final List<String> TOKEN_TYPES = List.copyOf(SemanticTokensService.TOKEN_TYPES);

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
     * Replaces buffer text without compiling. The same text keeps the
     * current version, so the next query can reuse the snapshot.
     * {@link #compileNow()} and {@link #scheduleCompile(long)} remain
     * available for callers that debounce.
     *
     * @param uri buffer identity
     * @param text source text, or {@code null} for empty
     */
    public void updateBuffer(final URI uri, final String text) {
        URI normalized = requireUri(uri);
        TextDocument existing = context.getDocuments().get(normalized);
        String next = text == null ? "" : text;
        if (existing != null && next.equals(existing.getText())) {
            return;
        }
        int version = existing == null ? 1 : existing.getVersion() + 1;
        String language = existing == null || existing.getLanguageId() == null || existing.getLanguageId().isEmpty()
                ? LANGUAGE_GROOVY : existing.getLanguageId();
        context.getDocuments().open(new TextDocumentItem(normalized.toString(), language, version, next));
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
     * Parent loader for the next compile. A different loader drops the
     * cached compile. An in-place {@code URLClassLoader#addURL} stays
     * visible because the fingerprint includes that loader's URLs.
     *
     * @param parentLoader loader, or {@code null} for the session default
     */
    public void setParentLoader(final ClassLoader parentLoader) {
        context.setParentLoader(parentLoader);
    }

    /**
     * Compiler configuration from Console or groovysh. Import customizers
     * and AST transforms are copied into the tooling compile. Every call
     * drops the cached compile, including a repeat with the same instance,
     * so an in-place customizer edit is visible on the next query. Do not
     * call this before every query.
     *
     * @param configuration host configuration, or {@code null}
     */
    public void setHostConfiguration(final CompilerConfiguration configuration) {
        context.setHostConfiguration(configuration);
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
     * Stores an opaque extra setting that participates in the compile
     * fingerprint. In-place {@code addURL} is already visible through
     * the loader URL list.
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
     * Replaces the buffer text, then returns diagnostics.
     *
     * @param uri buffer identity
     * @param text source text
     * @return diagnostics, never {@code null}
     */
    public List<Hit> diagnostics(final URI uri, final String text) {
        updateBuffer(uri, text);
        return diagnostics(uri);
    }

    /**
     * Script-level names the compiler already introduced in an open buffer.
     * See {@link ScriptNames}. REPL hosts use this so binding declarations
     * are not duplicated.
     *
     * @param uri buffer identity
     * @return names, never {@code null}
     */
    public Set<String> declaredNames(final URI uri) {
        TextDocument document = documentAt(uri);
        CompiledDocument compiled = context.getSnapshot().get(document);
        if (compiled == null || compiled.getModule() == null) {
            return Set.of();
        }
        return ScriptNames.of(compiled.getModule());
    }

    /**
     * Replaces the buffer text, then returns script-level names.
     *
     * @param uri buffer identity
     * @param text source text
     * @return names, never {@code null}
     */
    public Set<String> declaredNames(final URI uri, final String text) {
        updateBuffer(uri, text);
        return declaredNames(uri);
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
     * Replaces the buffer text, then returns hover markdown.
     *
     * @param uri buffer identity
     * @param text source text
     * @param line 0-based line
     * @param character 0-based character
     * @return markdown, never {@code null}
     */
    public String describe(final URI uri, final String text, final int line, final int character) {
        updateBuffer(uri, text);
        return describe(uri, line, character);
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
        Map<String, List<TextEdit>> changes = edit.getChanges();
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
                new Position(line, character), encoding(), context.getParentLoader(), context.getHostConfiguration(),
                context.getSettings());
        List<CompletionItem> items = list.getItems();
        return items.stream()
                .filter(Objects::nonNull)
                .map(GroovyLanguageEngine::toCandidate)
                .toList();
    }

    /**
     * Replaces the buffer text, then returns completions.
     *
     * @param uri buffer identity
     * @param text source text
     * @param line 0-based line
     * @param character 0-based character
     * @return candidates, never {@code null}
     */
    public List<Candidate> complete(final URI uri, final String text, final int line, final int character) {
        updateBuffer(uri, text);
        return complete(uri, line, character);
    }

    /**
     * Lenient lexical semantic tokens. No session and no compile, so a live
     * highlighter can call this on the EDT. Incomplete input still tokenizes.
     * Identifiers are {@code variable} until {@link #tokens(URI)} overlays
     * declaration and use-site kinds.
     *
     * @param text source text
     * @return tokens, never {@code null}
     */
    public static List<Token> lexicalTokens(final String text) {
        return lexicalTokens(text, lexerTokens(text));
    }

    /**
     * Lenient lexer walk behind {@link #lexicalTokens(String)}. Hosts that
     * also need raw tokens (brace matching, string delimiters) call this
     * once and pass the list to {@link #lexicalTokens(String, List)}.
     *
     * @param text source text
     * @return ANTLR tokens, never {@code null}
     */
    public static List<org.antlr.v4.runtime.Token> lexerTokens(final String text) {
        return GroovySourceTokens.tokenizeLenient(text);
    }

    /**
     * Sorted lexer keywords plus primitive type names, {@code true},
     * {@code false} and {@code null}.
     *
     * @return keywords, never {@code null}
     */
    public static List<String> keywords() {
        return GroovyKeywords.KEYWORDS;
    }

    /**
     * Lenient lexical semantic tokens from an already-tokenized walk.
     *
     * @param text source text
     * @param lexerTokens ANTLR tokens, possibly {@code null}
     * @return tokens, never {@code null}
     */
    public static List<Token> lexicalTokens(final String text, final List<org.antlr.v4.runtime.Token> lexerTokens) {
        return toEngineTokens(SemanticTokensService.lexicalTokenSpans(text, lexerTokens, PositionEncoding.UTF16));
    }

    /**
     * Semantic tokens as absolute spans (not LSP delta encoding).
     * Needs a compiled snapshot; do not call this on the Swing EDT.
     *
     * @param uri buffer identity
     * @return tokens, never {@code null}
     */
    public List<Token> tokens(final URI uri) {
        TextDocument document = documentAt(uri);
        CompiledDocument compiled = context.getSnapshot().get(document);
        if (compiled == null || compiled.getModule() == null) {
            return List.of();
        }
        return toEngineTokens(features.semanticTokens().tokenSpans(compiled, encoding()));
    }

    private static List<Token> toEngineTokens(final List<SemanticTokensService.TokenSpan> spans) {
        List<Token> tokens = new ArrayList<>(spans.size());
        for (SemanticTokensService.TokenSpan span : spans) {
            int type = span.type();
            String typeName = TOKEN_TYPES.get(type);
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
     * Replaces the buffer text, then returns signatures.
     *
     * @param uri buffer identity
     * @param text source text
     * @param line 0-based line
     * @param character 0-based character
     * @return signatures, never {@code null}
     */
    public SignatureSet signatures(final URI uri, final String text, final int line, final int character) {
        updateBuffer(uri, text);
        return signatures(uri, line, character);
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
     * Whether a buffer is open. Does not compile.
     *
     * @param uri buffer identity
     * @return {@code true} when {@code uri} is in the document store
     */
    public boolean isOpen(final URI uri) {
        if (uri == null) {
            return false;
        }
        return context.getDocuments().get(uri) != null;
    }

    /**
     * Default import prefixes from {@link ResolveVisitor#DEFAULT_IMPORTS}.
     * Each entry is a package prefix with a trailing dot, not an
     * {@code import} statement.
     *
     * @return immutable prefixes, never {@code null}
     */
    public static List<String> defaultImports() {
        return List.copyOf(Arrays.asList(ResolveVisitor.DEFAULT_IMPORTS));
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
        Objects.requireNonNull(document);
        // recompile() no-ops when the fingerprint matches the cached key.
        // updateBuffer keeps that key when the text is unchanged.
        // setHostConfiguration drops it.
        context.recompile();
    }

    private static URI requireUri(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri");
        }
        return Uris.normalize(uri);
    }

    /**
     * Maps a UTF-16 document offset to a 0-based line and character.
     * CR, LF, and CRLF are line breaks.
     *
     * @param text document text
     * @param offset UTF-16 offset
     * @return {@code {line, character}}
     */
    public static int[] positionOf(final String text, final int offset) {
        return TextDocument.positionOf(text, offset);
    }

    /**
     * Maps a 0-based line and character to a UTF-16 document offset.
     * The character is clamped to the requested line. CR, LF, and CRLF
     * are line breaks.
     *
     * @param text document text
     * @param line 0-based line
     * @param character 0-based character
     * @return offset in {@code text}
     */
    public static int offsetOf(final String text, final int line, final int character) {
        return TextDocument.offsetOf(text, new Position(Math.max(line, 0), Math.max(character, 0)),
                PositionEncoding.UTF16);
    }

    /**
     * Strips LSP snippet placeholders from completion insert text.
     *
     * @param insert snippet or plain insert text
     * @return flattened text, never {@code null}
     */
    public static String flattenSnippet(final String insert) {
        if (insert == null || insert.isEmpty()) {
            return "";
        }
        return SNIPPET_TABSTOP.matcher(SNIPPET_PLACEHOLDER.matcher(insert).replaceAll("$1")).replaceAll("");
    }

    /**
     * {@link #flattenSnippet(String)} truncated at the first newline.
     * Line-oriented hosts (groovysh) use this.
     *
     * @param insert snippet or plain insert text
     * @return one line, never {@code null}
     */
    public static String flattenSnippetLine(final String insert) {
        String flattened = flattenSnippet(insert);
        int nl = flattened.indexOf('\n');
        return nl < 0 ? flattened : flattened.substring(0, nl);
    }

    /**
     * Maps a diagnostic range onto UTF-16 offsets. A zero-width or inverted
     * range becomes one character so a highlighter has something to paint.
     * A range at EOF paints the last character of a non-empty document.
     *
     * @param text document text
     * @param startLine 0-based start line
     * @param startCharacter 0-based start character
     * @param endLine 0-based end line
     * @param endCharacter 0-based end character
     * @return {@code {startOffset, endOffset}}
     */
    public static int[] spanOf(final String text, final int startLine, final int startCharacter,
                               final int endLine, final int endCharacter) {
        return spanOf(text, startLine, startCharacter, endLine, endCharacter, true);
    }

    /**
     * @param paintLastAtEof when {@code true}, a zero-width range at the end
     *                       of a non-empty document paints the last character.
     *                       When {@code false}, that range is dropped.
     * @return {@code {startOffset, endOffset}}
     */
    public static int[] spanOf(final String text, final int startLine, final int startCharacter,
                               final int endLine, final int endCharacter, final boolean paintLastAtEof) {
        int start = offsetOf(text, startLine, startCharacter);
        int end = offsetOf(text, endLine, endCharacter);
        int length = text == null ? 0 : text.length();
        if (end <= start) {
            if (start >= length) {
                if (paintLastAtEof && length > 0) {
                    start = length - 1;
                    end = length;
                } else {
                    return new int[] {length, length};
                }
            } else {
                end = Math.min(start + 1, length);
            }
        }
        return new int[] {start, end};
    }

    /**
     * @param text document text
     * @param hit diagnostic
     * @return {@code {startOffset, endOffset}}
     */
    public static int[] spanOf(final String text, final Hit hit) {
        if (hit == null) {
            return new int[] {0, 0};
        }
        return spanOf(text, hit.startLine(), hit.startCharacter(), hit.endLine(), hit.endCharacter());
    }

    /**
     * Maps a semantic token onto UTF-16 offsets. Tokens are single-line
     * (multiline comments and strings are already split).
     *
     * @param text document text
     * @param token semantic token
     * @return {@code {startOffset, endOffset}}
     */
    public static int[] spanOf(final String text, final Token token) {
        if (token == null) {
            return new int[] {0, 0};
        }
        int start = offsetOf(text, token.line(), token.character());
        int length = text == null ? 0 : text.length();
        int end = Math.min(start + Math.max(token.length(), 0), length);
        return new int[] {start, end};
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
                flattenSnippet(insert),
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
