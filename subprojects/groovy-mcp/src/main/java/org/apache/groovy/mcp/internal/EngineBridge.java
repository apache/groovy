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
package org.apache.groovy.mcp.internal;

import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Candidate;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Change;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Hit;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Refs;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Rename;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Signature;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.SignatureSet;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Site;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Symbol;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine.Token;
import org.apache.groovy.lsp.internal.util.Uris;
import org.apache.groovy.mcp.McpServerConfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns MCP arguments into {@link GroovyLanguageEngine} queries.
 * Edits are returned to the client. Nothing is written.
 */
public final class EngineBridge {

    /** Default in-memory buffer. Not a file URI, so the compiler does not treat it as a path. */
    public static final URI SNIPPET = URI.create("groovy-buffer:snippet");

    static final int DIAGNOSTIC_CAP = 200;
    static final int SITE_CAP = 50;
    static final int COMPLETION_CAP = 200;
    static final int TOKEN_CAP = 4000;
    static final int EDIT_CAP = 2000;
    static final int MAX_LINE = 1_000_000;
    static final int MAX_CHARACTER = 100_000;
    static final int MAX_IDENTIFIER = 200;
    static final int DEFAULT_MAX_RESULTS = 50;
    static final int MAX_RESULTS = 200;
    private static final int MAX_URI_PART = 200;

    private final GroovyLanguageEngine engine;
    private final McpServerConfig config;
    private final PathPolicy paths;

    /**
     * @param engine session owned by the server
     * @param config launch limits
     */
    public EngineBridge(final GroovyLanguageEngine engine, final McpServerConfig config) {
        this.engine = engine;
        this.config = config;
        this.paths = new PathPolicy(config.root());
    }

    /**
     * @param args tool arguments
     * @return diagnostics for one buffer
     */
    public Map<String, Object> diagnostics(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        List<Map<String, Object>> hits = new ArrayList<>();
        for (Hit hit : engine.diagnostics(uri)) {
            hits.add(hit(hit));
        }
        return page("diagnostics", hits, DIAGNOSTIC_CAP);
    }

    /**
     * @param args tool arguments
     * @return hover markdown
     */
    public Map<String, Object> describe(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        return ordered("markdown", engine.describe(uri, line(args), character(args)));
    }

    /**
     * @param args tool arguments
     * @return definition sites
     */
    public Map<String, Object> definition(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        return sites("definitions", engine.definition(uri, line(args), character(args)));
    }

    /**
     * @param args tool arguments
     * @return implementation sites
     */
    public Map<String, Object> implementations(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        return sites("implementations", engine.implementations(uri, line(args), character(args)));
    }

    /**
     * @param args tool arguments
     * @return references, already capped by the engine
     */
    public Map<String, Object> references(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        int max = args.optionalInt("maxResults", DEFAULT_MAX_RESULTS, 1, MAX_RESULTS);
        Refs refs = engine.references(uri, line(args), character(args), max);
        List<Map<String, Object>> locations = new ArrayList<>();
        for (Site site : refs.locations()) {
            locations.add(site(site));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", refs.total());
        map.put("truncated", refs.truncated());
        map.put("locations", List.copyOf(locations));
        return map;
    }

    /**
     * @param args tool arguments
     * @return symbol hits over the buffers that are open
     */
    public Map<String, Object> symbols(final ArgumentMap args) {
        openForQuery(args, false);
        String query = args.optionalString("query");
        int max = args.optionalInt("maxResults", DEFAULT_MAX_RESULTS, 1, MAX_RESULTS);
        List<Symbol> found = engine.symbols(query == null ? "" : query, max + 1);
        boolean truncated = found.size() > max;
        int limit = truncated ? max : found.size();
        List<Map<String, Object>> hits = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            hits.add(symbol(found.get(i)));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("symbols", List.copyOf(hits));
        map.put("truncated", truncated);
        return map;
    }

    /**
     * @param args tool arguments
     * @return completion candidates
     */
    public Map<String, Object> complete(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Candidate candidate : engine.complete(uri, line(args), character(args))) {
            items.add(candidate(candidate));
        }
        return page("completions", items, COMPLETION_CAP);
    }

    /**
     * @param args tool arguments
     * @return signature help
     */
    public Map<String, Object> signatures(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        SignatureSet set = engine.signatures(uri, line(args), character(args));
        List<Map<String, Object>> signatures = new ArrayList<>();
        for (Signature signature : set.signatures()) {
            signatures.add(signature(signature));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("activeSignature", set.activeSignature());
        map.put("activeParameter", set.activeParameter());
        map.put("signatures", List.copyOf(signatures));
        return map;
    }

    /**
     * @param args tool arguments
     * @return absolute semantic-token spans
     */
    public Map<String, Object> tokens(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        List<Map<String, Object>> tokens = new ArrayList<>();
        for (Token token : engine.tokens(uri)) {
            tokens.add(token(token));
        }
        return page("tokens", tokens, TOKEN_CAP);
    }

    /**
     * @param args tool arguments
     * @return format edits
     */
    public Map<String, Object> format(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        int tab = args.optionalInt("tabSize", 4, 1, 16);
        boolean spaces = args.optionalBoolean("insertSpaces", true);
        return edits(engine.format(uri, tab, spaces));
    }

    /**
     * @param args tool arguments
     * @return organize-imports edits
     */
    public Map<String, Object> organizeImports(final ArgumentMap args) {
        return edits(engine.organizeImports(openForQuery(args, true)));
    }

    /**
     * @param args tool arguments
     * @return rename edits, empty when the binding is not proven
     */
    public Map<String, Object> rename(final ArgumentMap args) {
        URI uri = openForQuery(args, true);
        String name = args.requiredString("newName");
        if (!identifier(name)) {
            throw new ToolFailure("newName must be an identifier");
        }
        Rename rename = engine.rename(uri, line(args), character(args), name);
        List<Map<String, Object>> changes = new ArrayList<>();
        for (Change change : rename.changes()) {
            changes.add(change(change));
        }
        Map<String, Object> paged = page("changes", changes, EDIT_CAP);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("newName", rename.newName());
        map.put("changeCount", rename.changeCount());
        map.put("changes", paged.get("changes"));
        map.put("truncated", paged.get("truncated"));
        return map;
    }

    /**
     * Drops a buffer. Does not delete a file.
     *
     * @param args tool arguments
     * @return whether a buffer was open
     */
    public Map<String, Object> close(final ArgumentMap args) {
        URI uri = existingUri(args.requiredString("uri"));
        boolean open = engine.isOpen(uri);
        if (open) {
            engine.closeBuffer(uri);
        }
        return ordered("closed", open);
    }

    private List<Map<String, Object>> siteMaps(final List<Site> sites) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Site site : sites) {
            maps.add(site(site));
        }
        return maps;
    }

    private Map<String, Object> sites(final String key, final List<Site> sites) {
        return page(key, siteMaps(sites), SITE_CAP);
    }

    private Map<String, Object> edits(final List<Change> changes) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Change change : changes) {
            maps.add(change(change));
        }
        return page("changes", maps, EDIT_CAP);
    }

    private URI openForQuery(final ArgumentMap args, final boolean required) {
        boolean text = args.has("text");
        boolean path = args.has("path");
        boolean uri = args.has("uri");
        if (text && path) {
            throw new ToolFailure("pass text or path, not both");
        }
        if (path && uri) {
            throw new ToolFailure("pass path or uri, not both");
        }
        if (text) {
            String source = args.requiredString("text");
            if (source.length() > config.maxSourceChars()) {
                throw new ToolFailure("text exceeds the source limit");
            }
            URI id = uri ? bufferUri(args.requiredString("uri")) : SNIPPET;
            return engine.openBuffer(id, "groovy", source);
        }
        if (path) {
            Path file = paths.resolveFile(args.requiredString("path"), config.maxSourceChars());
            return engine.open(file);
        }
        if (uri) {
            URI id = existingUri(args.requiredString("uri"));
            if (!engine.isOpen(id)) {
                throw new ToolFailure("uri is not open");
            }
            return id;
        }
        if (!required) {
            return null;
        }
        throw new ToolFailure("text, path, or uri is required");
    }

    private URI bufferUri(final String text) {
        URI uri = parseUri(text);
        if (!"groovy-buffer".equals(uri.getScheme())) {
            throw new ToolFailure("in-memory uri must use the groovy-buffer scheme");
        }
        String part = uri.getSchemeSpecificPart();
        if (part == null || part.isBlank() || part.length() > MAX_URI_PART) {
            throw new ToolFailure("uri is not valid");
        }
        return uri;
    }

    private URI existingUri(final String text) {
        URI uri = parseUri(text);
        if ("groovy-buffer".equals(uri.getScheme())) {
            return bufferUri(text);
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            Path path;
            try {
                path = Uris.toPath(uri);
            } catch (IllegalArgumentException ex) {
                throw new ToolFailure("uri is not valid");
            }
            Path real = paths.requireInside(path);
            // open(Path) stores the real path, not the URI the client spelled.
            return Uris.normalize(real.toUri());
        }
        throw new ToolFailure("uri scheme is not supported");
    }

    private static URI parseUri(final String text) {
        try {
            return URI.create(text);
        } catch (IllegalArgumentException ex) {
            throw new ToolFailure("uri is not valid");
        }
    }

    private static int line(final ArgumentMap args) {
        return args.requiredInt("line", 0, MAX_LINE);
    }

    private static int character(final ArgumentMap args) {
        return args.requiredInt("character", 0, MAX_CHARACTER);
    }

    static boolean identifier(final String name) {
        if (name == null || name.isEmpty() || name.length() > MAX_IDENTIFIER) {
            return false;
        }
        int first = name.codePointAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }
        for (int i = Character.charCount(first); i < name.length(); ) {
            int point = name.codePointAt(i);
            if (!Character.isJavaIdentifierPart(point)) {
                return false;
            }
            i += Character.charCount(point);
        }
        return true;
    }

    private static Map<String, Object> page(final String key, final List<Map<String, Object>> items, final int cap) {
        int total = items.size();
        int limit = Math.min(total, cap);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, List.copyOf(items.subList(0, limit)));
        map.put("total", total);
        map.put("truncated", total > cap);
        return map;
    }

    private static Map<String, Object> ordered(final String key, final Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private static Map<String, Object> hit(final Hit hit) {
        Map<String, Object> map = span(hit.uri(), hit.startLine(), hit.startCharacter(), hit.endLine(), hit.endCharacter());
        map.put("severity", hit.severity());
        map.put("message", hit.message());
        return map;
    }

    private static Map<String, Object> site(final Site site) {
        Map<String, Object> map = span(site.uri(), site.startLine(), site.startCharacter(), site.endLine(), site.endCharacter());
        map.put("snippet", site.snippet());
        return map;
    }

    private static Map<String, Object> change(final Change change) {
        Map<String, Object> map = span(change.uri(), change.startLine(), change.startCharacter(),
                change.endLine(), change.endCharacter());
        map.put("newText", change.newText());
        return map;
    }

    private static Map<String, Object> span(final URI uri, final int startLine, final int startCharacter,
                                            final int endLine, final int endCharacter) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uri", uri == null ? "" : uri.toString());
        map.put("startLine", startLine);
        map.put("startCharacter", startCharacter);
        map.put("endLine", endLine);
        map.put("endCharacter", endCharacter);
        return map;
    }

    private static Map<String, Object> symbol(final Symbol symbol) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", symbol.name());
        map.put("kind", symbol.kind());
        map.put("location", site(symbol.location()));
        return map;
    }

    private static Map<String, Object> candidate(final Candidate candidate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", candidate.label());
        map.put("kind", candidate.kind());
        map.put("detail", candidate.detail());
        map.put("insertText", candidate.insertText());
        map.put("documentation", candidate.documentation());
        return map;
    }

    private static Map<String, Object> signature(final Signature signature) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", signature.label());
        map.put("documentation", signature.documentation());
        map.put("parameters", signature.parameters());
        return map;
    }

    private static Map<String, Object> token(final Token token) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("line", token.line());
        map.put("character", token.character());
        map.put("length", token.length());
        map.put("type", token.type());
        map.put("modifiers", token.modifiers());
        return map;
    }
}
