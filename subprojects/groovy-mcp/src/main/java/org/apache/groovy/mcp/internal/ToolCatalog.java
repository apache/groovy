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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The tool list the server advertises. Every tool is read-only on the
 * user's files: rename, format and organize-imports return edits.
 */
public final class ToolCatalog {

    private static final Set<String> SOURCE = Set.of("text", "path", "uri");
    private static final Set<String> POSITION = Set.of("text", "path", "uri", "line", "character");
    private static final Set<String> REFERENCES = Set.of("text", "path", "uri", "line", "character", "maxResults");
    private static final Set<String> SYMBOLS = Set.of("text", "path", "uri", "query", "maxResults");
    private static final Set<String> RENAME = Set.of("text", "path", "uri", "line", "character", "newName");
    private static final Set<String> FORMAT = Set.of("text", "path", "uri", "tabSize", "insertSpaces");
    private static final Set<String> CLOSE = Set.of("uri");

    private final List<Tool> tools;
    private final Map<String, Tool> byName;

    /**
     * @param bridge engine adapter
     */
    public ToolCatalog(final EngineBridge bridge) {
        List<Tool> listed = List.of(
                tool("groovy_diagnostics", "Compiler diagnostics for one Groovy buffer. Does not run the script.",
                        "Diagnostics", schema(SOURCE, false, true), SOURCE, bridge::diagnostics),
                tool("groovy_describe", "Hover markdown at a 0-based UTF-16 position.",
                        "Describe", schema(POSITION, true, true), POSITION, bridge::describe),
                tool("groovy_definition", "Definition sites at a 0-based UTF-16 position.",
                        "Definition", schema(POSITION, true, true), POSITION, bridge::definition),
                tool("groovy_implementations", "Implementation sites at a 0-based UTF-16 position.",
                        "Implementations", schema(POSITION, true, true), POSITION, bridge::implementations),
                tool("groovy_references", "References at a 0-based UTF-16 position. maxResults is 1 to 200.",
                        "References", schema(REFERENCES, true, true), REFERENCES, bridge::references),
                tool("groovy_symbols", "Symbols in open buffers. query is a substring or CamelHumps.",
                        "Symbols", schema(SYMBOLS, false, false), SYMBOLS, bridge::symbols),
                tool("groovy_complete", "Completion candidates at a 0-based UTF-16 position.",
                        "Complete", schema(POSITION, true, true), POSITION, bridge::complete),
                tool("groovy_signatures", "Signature help at a 0-based UTF-16 position.",
                        "Signatures", schema(POSITION, true, true), POSITION, bridge::signatures),
                tool("groovy_tokens", "Semantic tokens as absolute 0-based UTF-16 spans.",
                        "Tokens", schema(SOURCE, false, true), SOURCE, bridge::tokens),
                tool("groovy_format", "Indent edits for one buffer. Does not write the file.",
                        "Format", schema(FORMAT, false, true), FORMAT, bridge::format),
                tool("groovy_organize_imports", "Organize-imports edits. Does not write the file.",
                        "Organize imports", schema(SOURCE, false, true), SOURCE, bridge::organizeImports),
                tool("groovy_rename",
                        "Rename edits when the compiler can prove the binding. Empty when it cannot. Does not write.",
                        "Rename", schema(RENAME, true, true), RENAME, bridge::rename),
                tool("groovy_close", "Drops an open buffer. Does not delete a file.",
                        "Close buffer", schema(CLOSE, false, false), CLOSE, bridge::close)
        );
        this.tools = listed;
        Map<String, Tool> index = new LinkedHashMap<>();
        for (Tool tool : listed) {
            index.put(tool.name, tool);
        }
        this.byName = Map.copyOf(index);
    }

    /**
     * @return tool definitions in advertised order
     */
    public List<Map<String, Object>> definitions() {
        List<Map<String, Object>> listed = new ArrayList<>();
        for (Tool tool : tools) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", tool.name);
            item.put("title", tool.title);
            item.put("description", tool.description);
            item.put("inputSchema", tool.schema);
            item.put("outputSchema", tool.output);
            item.put("annotations", annotations(tool.title));
            listed.add(Collections.unmodifiableMap(item));
        }
        return List.copyOf(listed);
    }

    /**
     * @param name tool name
     * @param arguments argument object, or {@code null}
     * @return structured tool output
     */
    public Map<String, Object> call(final String name, final Object arguments) {
        Tool tool = byName.get(name);
        if (tool == null) {
            // A model can recover from a tool error. A protocol error looks like a broken client.
            throw new ToolFailure("unknown tool");
        }
        return tool.action.apply(ArgumentMap.of(arguments, tool.allowed));
    }

    private static Tool tool(final String name, final String description, final String title,
                             final Map<String, Object> schema, final Set<String> allowed,
                             final Function<ArgumentMap, Map<String, Object>> action) {
        checkToolName(name);
        return new Tool(name, description, title, schema, outputSchema(name), allowed, action);
    }

    private static Map<String, Object> schema(final Set<String> names, final boolean positioned,
                                              final boolean requireSource) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (names.contains("text")) {
            properties.put("text", stringProp("Groovy source. Optional uri names the buffer. Do not pass path."));
        }
        if (names.contains("path")) {
            properties.put("path", stringProp("Source file under the server --root."));
        }
        if (names.contains("uri")) {
            properties.put("uri", stringProp("groovy-buffer URI, or a file URI already opened under --root."));
        }
        if (names.contains("line")) {
            properties.put("line", intProp("0-based line."));
        }
        if (names.contains("character")) {
            properties.put("character", intProp("0-based UTF-16 code unit."));
        }
        if (names.contains("maxResults")) {
            properties.put("maxResults", intProp("Maximum hits, from 1 to 200."));
        }
        if (names.contains("query")) {
            properties.put("query", stringProp("Substring or CamelHumps. Empty lists every open symbol."));
        }
        if (names.contains("newName")) {
            properties.put("newName", stringProp("Replacement identifier."));
        }
        if (names.contains("tabSize")) {
            properties.put("tabSize", intProp("Spaces per indent level, from 1 to 16. Default 4."));
        }
        if (names.contains("insertSpaces")) {
            properties.put("insertSpaces", Map.of("type", "boolean", "description", "Use spaces. Default true."));
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("$schema", JSON_SCHEMA);
        schema.put("properties", Collections.unmodifiableMap(properties));
        schema.put("additionalProperties", Boolean.FALSE);
        List<String> required = new ArrayList<>();
        if (names.equals(CLOSE)) {
            required.add("uri");
        }
        if (positioned) {
            required.add("line");
            required.add("character");
        }
        if (names.contains("newName")) {
            required.add("newName");
        }
        if (!required.isEmpty()) {
            schema.put("required", List.copyOf(required));
        }
        // text may name its groovy-buffer with uri; path and uri each stand alone
        if (requireSource) {
            schema.put("oneOf", sourceChoices(false));
        } else if (names.contains("text")) {
            schema.put("oneOf", sourceChoices(true));
        }
        return Collections.unmodifiableMap(schema);
    }

    private static List<Map<String, Object>> sourceChoices(final boolean allowNone) {
        List<Map<String, Object>> choices = new ArrayList<>();
        if (allowNone) {
            Map<String, Object> none = new LinkedHashMap<>();
            none.put("not", absentSchema(List.of("text", "path", "uri")));
            choices.add(Collections.unmodifiableMap(none));
        }
        choices.add(sourceBranch(List.of("text"), List.of("path")));
        choices.add(sourceBranch(List.of("path"), List.of("text", "uri")));
        choices.add(sourceBranch(List.of("uri"), List.of("text", "path")));
        return List.copyOf(choices);
    }

    private static Map<String, Object> sourceBranch(final List<String> required, final List<String> absent) {
        Map<String, Object> branch = new LinkedHashMap<>();
        branch.put("required", List.copyOf(required));
        branch.put("not", absentSchema(absent));
        return Collections.unmodifiableMap(branch);
    }

    private static Map<String, Object> absentSchema(final List<String> absent) {
        if (absent.size() == 1) {
            return Map.of("required", List.copyOf(absent));
        }
        List<Map<String, Object>> any = new ArrayList<>();
        for (String name : absent) {
            any.add(Map.of("required", List.of(name)));
        }
        return Map.of("anyOf", List.copyOf(any));
    }

    private static Map<String, Object> stringProp(final String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> intProp(final String description) {
        return Map.of("type", "integer", "description", description);
    }

    private static Map<String, Object> annotations(final String title) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("readOnlyHint", Boolean.TRUE);
        map.put("destructiveHint", Boolean.FALSE);
        map.put("idempotentHint", Boolean.TRUE);
        map.put("openWorldHint", Boolean.FALSE);
        return Collections.unmodifiableMap(map);
    }

    /**
     * SEP-986 name: ASCII letters, digits, and {@code _.-}, at most 128
     * characters, not opening or closing on {@code .} or {@code -}.
     * A bad built-in name is a programming error, so the message does not
     * repeat the name.
     *
     * @param name tool name
     */
    static void checkToolName(final String name) {
        if (name == null || name.isEmpty() || name.length() > 128 || badToolChar(name)
                || name.charAt(0) == '-' || name.charAt(0) == '.'
                || name.charAt(name.length() - 1) == '-' || name.charAt(name.length() - 1) == '.') {
            throw new IllegalStateException("invalid tool name");
        }
    }

    private static boolean badToolChar(final String name) {
        for (int i = 0; i < name.length(); ) {
            char c = name.charAt(i);
            boolean allowed = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.';
            if (!allowed) {
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     * @param name tool name
     * @return the output schema, including the error object a failed call returns
     */
    static Map<String, Object> outputSchema(final String name) {
        Map<String, Object> schema = OUTPUT.get(name);
        if (schema == null) {
            throw new IllegalStateException("invalid tool name");
        }
        return schema;
    }

    private static final String JSON_SCHEMA = "https://json-schema.org/draft/2020-12/schema";

    private static final Map<String, Map<String, Object>> OUTPUT = outputSchemas();

    private static Map<String, Map<String, Object>> outputSchemas() {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        map.put("groovy_diagnostics", resultSchema(pageShape("diagnostics", hitSchema())));
        map.put("groovy_describe", resultSchema(shape(Map.of("markdown", nullableString("Hover markdown.")), List.of("markdown"))));
        map.put("groovy_definition", resultSchema(pageShape("definitions", siteSchema())));
        map.put("groovy_implementations", resultSchema(pageShape("implementations", siteSchema())));
        map.put("groovy_references", resultSchema(pageShape("locations", siteSchema())));
        map.put("groovy_symbols", resultSchema(shape(symbolProperties(), List.of("symbols", "truncated"))));
        map.put("groovy_complete", resultSchema(pageShape("completions", candidateSchema())));
        map.put("groovy_signatures", resultSchema(shape(signatureProperties(), List.of("activeSignature", "activeParameter", "signatures"))));
        map.put("groovy_tokens", resultSchema(pageShape("tokens", tokenSchema())));
        Shape edits = pageShape("changes", changeSchema());
        map.put("groovy_format", resultSchema(edits));
        map.put("groovy_organize_imports", resultSchema(edits));
        map.put("groovy_rename", resultSchema(shape(renameProperties(), List.of("newName", "changeCount", "changes", "truncated"))));
        map.put("groovy_close", resultSchema(shape(Map.of("closed", boolProp("Whether a buffer was open.")), List.of("closed"))));
        return Map.copyOf(map);
    }

    private static Map<String, Object> resultSchema(final Shape success) {
        Map<String, Object> properties = new LinkedHashMap<>(success.properties);
        properties.put("error", stringProp("Why the call failed. Absent on success."));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", JSON_SCHEMA);
        schema.put("type", "object");
        schema.put("additionalProperties", Boolean.FALSE);
        schema.put("properties", Collections.unmodifiableMap(properties));
        schema.put("oneOf", List.of(
                sourceBranch(success.required, List.of("error")),
                sourceBranch(List.of("error"), success.required)));
        return Collections.unmodifiableMap(schema);
    }

    private static Shape pageShape(final String key, final Map<String, Object> item) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(key, arrayProp(item));
        properties.put("total", intProp("Count before the cap."));
        properties.put("truncated", boolProp("True when a hit was dropped."));
        return new Shape(properties, List.of(key, "total", "truncated"));
    }

    private static Map<String, Object> symbolProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("symbols", arrayProp(symbolSchema()));
        properties.put("truncated", boolProp("True when a hit was dropped."));
        return properties;
    }

    private static Map<String, Object> signatureProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("activeSignature", intProp("Index of the active signature."));
        properties.put("activeParameter", intProp("Index of the active parameter."));
        properties.put("signatures", arrayProp(signatureSchema()));
        return properties;
    }

    private static Map<String, Object> renameProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("newName", nullableString("Replacement identifier."));
        properties.put("changeCount", intProp("Edits the compiler proved."));
        properties.put("changes", arrayProp(changeSchema()));
        properties.put("truncated", boolProp("True when an edit was dropped."));
        return properties;
    }

    private static Map<String, Object> hitSchema() {
        return withSpan(Map.of(
                "severity", nullableString("Diagnostic severity."),
                "message", nullableString("Diagnostic message.")),
                List.of("severity", "message"));
    }

    private static Map<String, Object> siteSchema() {
        return withSpan(Map.of("snippet", nullableString("Source around the site.")), List.of("snippet"));
    }

    private static Map<String, Object> changeSchema() {
        return withSpan(Map.of("newText", nullableString("Replacement text.")), List.of("newText"));
    }

    private static Map<String, Object> symbolSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", nullableString("Symbol name."));
        properties.put("kind", nullableString("Symbol kind."));
        properties.put("location", siteSchema());
        return objectSchema(properties, List.of("name", "kind", "location"));
    }

    private static Map<String, Object> candidateSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("label", nullableString("Completion label."));
        properties.put("kind", nullableString("Completion kind."));
        properties.put("detail", nullableString("Completion detail."));
        properties.put("insertText", nullableString("Text to insert."));
        properties.put("documentation", nullableString("Completion documentation."));
        return objectSchema(properties, List.of("label", "kind", "detail", "insertText", "documentation"));
    }

    private static Map<String, Object> signatureSchema() {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", List.of("array", "null"));
        parameters.put("items", nullableString("Parameter label."));
        parameters.put("description", "Parameter labels.");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("label", nullableString("Signature label."));
        properties.put("documentation", nullableString("Signature documentation."));
        properties.put("parameters", Collections.unmodifiableMap(parameters));
        return objectSchema(properties, List.of("label", "documentation", "parameters"));
    }

    private static Map<String, Object> tokenSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("line", intProp("0-based line."));
        properties.put("character", intProp("0-based UTF-16 start."));
        properties.put("length", intProp("UTF-16 length."));
        properties.put("type", nullableString("Token type."));
        properties.put("modifiers", intProp("Modifier bits."));
        return objectSchema(properties, List.of("line", "character", "length", "type", "modifiers"));
    }

    private static Map<String, Object> withSpan(final Map<String, Object> extra, final List<String> extraRequired) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("uri", stringProp("Buffer URI. Empty when the engine has none."));
        properties.put("startLine", intProp("0-based start line."));
        properties.put("startCharacter", intProp("0-based UTF-16 start."));
        properties.put("endLine", intProp("0-based end line."));
        properties.put("endCharacter", intProp("0-based UTF-16 end."));
        properties.putAll(extra);
        List<String> required = new ArrayList<>();
        required.add("uri");
        required.add("startLine");
        required.add("startCharacter");
        required.add("endLine");
        required.add("endCharacter");
        required.addAll(extraRequired);
        return objectSchema(properties, required);
    }

    private static Shape shape(final Map<String, Object> properties, final List<String> required) {
        return new Shape(properties, required);
    }

    private static Map<String, Object> objectSchema(final Map<String, Object> properties, final List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Collections.unmodifiableMap(new LinkedHashMap<>(properties)));
        schema.put("required", List.copyOf(required));
        schema.put("additionalProperties", Boolean.FALSE);
        return Collections.unmodifiableMap(schema);
    }

    private static Map<String, Object> arrayProp(final Map<String, Object> items) {
        return Map.of("type", "array", "items", items);
    }

    private static Map<String, Object> boolProp(final String description) {
        return Map.of("type", "boolean", "description", description);
    }

    private static Map<String, Object> nullableString(final String description) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", List.of("string", "null"));
        map.put("description", description);
        return Collections.unmodifiableMap(map);
    }

    private record Shape(Map<String, Object> properties, List<String> required) {
    }

    private record Tool(String name, String description, String title, Map<String, Object> schema,
                        Map<String, Object> output, Set<String> allowed,
                        Function<ArgumentMap, Map<String, Object>> action) {
    }
}
