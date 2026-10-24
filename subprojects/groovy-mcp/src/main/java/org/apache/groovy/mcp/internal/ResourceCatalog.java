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

import groovy.json.JsonOutput;
import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine;
import org.apache.groovy.mcp.McpServerConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only MCP resources. Keyword and import lists come from the
 * compiler, not from a table kept in this module.
 */
public final class ResourceCatalog {

    static final String KEYWORDS = "groovy://language/keywords";
    static final String IMPORTS = "groovy://language/default-imports";
    static final String LIMITS = "groovy://server/limits";

    private final GroovyLanguageEngine engine;
    private final McpServerConfig config;

    /**
     * @param engine session whose settings are reported
     * @param config launch limits
     */
    public ResourceCatalog(final GroovyLanguageEngine engine, final McpServerConfig config) {
        this.engine = engine;
        this.config = config;
    }

    /**
     * @return resource descriptors
     */
    public List<Map<String, Object>> list() {
        return List.of(
                descriptor(KEYWORDS, "keywords", "Keywords", "Lexer keywords and primitive type names.", 0.9d),
                descriptor(IMPORTS, "default-imports", "Default imports",
                        "Default import package prefixes, each with a trailing dot.", 0.7d),
                descriptor(LIMITS, "limits", "Server limits",
                        "Server limits. execution, grapeEnabled and astTestEnabled stay false.", 0.5d)
        );
    }

    /**
     * @param uri resource URI
     * @return a {@code resources/read} result
     */
    public Map<String, Object> read(final String uri) {
        String text = body(uri);
        if (text.length() > config.maxResultChars()) {
            throw new ProtocolException(-32602, "resource exceeds the size limit");
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("uri", uri);
        content.put("mimeType", "application/json");
        content.put("text", text);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contents", List.of(content));
        return result;
    }

    private String body(final String uri) {
        if (KEYWORDS.equals(uri)) {
            return JsonOutput.toJson(GroovyLanguageEngine.keywords());
        }
        if (IMPORTS.equals(uri)) {
            return JsonOutput.toJson(GroovyLanguageEngine.defaultImports());
        }
        if (LIMITS.equals(uri)) {
            return JsonOutput.toJson(limits());
        }
        throw new ProtocolException(-32602, "unknown resource");
    }

    private Map<String, Object> limits() {
        CompilerSettings settings = engine.context().getSettings();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("execution", Boolean.FALSE);
        map.put("grapeEnabled", settings.isGrapeEnabled());
        map.put("astTestEnabled", settings.isAstTestEnabled());
        map.put("classpathEntries", settings.getClasspath().size());
        map.put("root", config.root() == null ? "" : config.root().toString());
        map.put("maxMessageBytes", config.maxMessageBytes());
        map.put("maxSourceChars", config.maxSourceChars());
        map.put("maxResultChars", config.maxResultChars());
        map.put("snippetUri", EngineBridge.SNIPPET.toString());
        return map;
    }

    private static Map<String, Object> descriptor(final String uri, final String name, final String title,
                                                  final String description, final double priority) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uri", uri);
        map.put("name", name);
        map.put("title", title);
        map.put("description", description);
        map.put("mimeType", "application/json");
        map.put("annotations", audience(priority));
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Object> audience(final double priority) {
        Map<String, Object> annotations = new LinkedHashMap<>();
        annotations.put("audience", List.of("assistant"));
        annotations.put("priority", priority);
        return Collections.unmodifiableMap(annotations);
    }
}
