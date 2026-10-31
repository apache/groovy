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
package org.apache.groovy.mcp;

import groovy.json.JsonOutput;
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine;
import org.apache.groovy.mcp.internal.EngineBridge;
import org.apache.groovy.mcp.internal.JsonCodec;
import org.apache.groovy.mcp.internal.JsonValues;
import org.apache.groovy.mcp.internal.PromptCatalog;
import org.apache.groovy.mcp.internal.ProtocolException;
import org.apache.groovy.mcp.internal.ResourceCatalog;
import org.apache.groovy.mcp.internal.ToolCatalog;
import org.apache.groovy.mcp.internal.ToolFailure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * In-process MCP session. One thread should call {@link #exchange}.
 * The server takes the engine's compile thread and does not scan a
 * workspace. Stdio framing lives in {@link GroovyMcpLauncher}.
 */
public final class GroovyMcpServer implements AutoCloseable {

    /**
     * Latest protocol version this server speaks. A client that asks for
     * a version outside {@link #SUPPORTED_PROTOCOLS} is answered with this
     * one, which is what the specification asks the server to prefer.
     */
    public static final String PROTOCOL_VERSION = "2025-11-25";

    /** Protocol versions this server can answer through {@code initialize}. */
    public static final List<String> SUPPORTED_PROTOCOLS = List.of(
            "2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05");

    /**
     * Protocol version that uses per-request {@code _meta} instead of
     * {@code initialize}. A client selects it by sending this value in
     * {@code io.modelcontextprotocol/protocolVersion}.
     */
    public static final String MODERN_PROTOCOL = "2026-07-28";

    /** How long a client may treat an unchanging list or resource as fresh. */
    static final long LIST_TTL_MS = 3_600_000L;

    private static final String PROTOCOL_META = "io.modelcontextprotocol/protocolVersion";

    private static final String SERVER_INFO_META = "io.modelcontextprotocol/serverInfo";

    private static final String INSTRUCTIONS = "groovy-mcp answers questions about Groovy source using the "
            + "Apache Groovy compiler. It does not run scripts, and it does not enable @Grab or @ASTTest. "
            + "Positions are 0-based UTF-16. Pass source as text with a groovy-buffer URI, or as path under "
            + "the server --root. Rename, format and organize-imports return edits and do not write files. "
            + "An empty rename means the compiler could not prove the binding.";

    private enum Phase {
        NEW, INITIALIZED, READY
    }

    private final McpServerConfig config;
    private final Appendable log;
    private final GroovyLanguageEngine engine;
    private final ToolCatalog tools;
    private final ResourceCatalog resources;
    private final PromptCatalog prompts;
    private Phase phase = Phase.NEW;
    private boolean closed;

    /**
     * Opens a session with {@link McpServerConfig#defaults()}. Logs go to {@code System.err}.
     */
    public GroovyMcpServer() {
        this(null, null);
    }

    /**
     * Opens a session that owns a new engine. Logs go to {@code System.err}.
     *
     * @param config limits and classpath, or {@code null} for {@link McpServerConfig#defaults()}
     */
    public GroovyMcpServer(final McpServerConfig config) {
        this(config, null);
    }

    /**
     * @param config limits and classpath, or {@code null} for defaults
     * @param log stderr log, or {@code null} for {@code System.err}
     */
    public GroovyMcpServer(final McpServerConfig config, final Appendable log) {
        this.config = config == null ? McpServerConfig.defaults() : config;
        this.log = log == null ? System.err : log;
        this.engine = new GroovyLanguageEngine();
        if (!this.config.classpath().isEmpty()) {
            this.engine.setClasspath(this.config.classpath());
        }
        this.tools = new ToolCatalog(new EngineBridge(engine, this.config));
        this.resources = new ResourceCatalog(engine, this.config);
        this.prompts = new PromptCatalog(this.config);
    }

    /**
     * Manifest version, or {@code dev} when the jar has none.
     * Never the literal {@code "null"}.
     *
     * @return the version
     */
    public static String implementationVersion() {
        Package pkg = GroovyMcpServer.class.getPackage();
        String version = pkg == null ? null : pkg.getImplementationVersion();
        return version == null ? "dev" : version;
    }

    /**
     * Handles one JSON-RPC message.
     *
     * @param line one message, without the trailing newline
     * @return the response line, or {@code null} when no response is due
     */
    public synchronized String exchange(final String line) {
        ensureOpen();
        if (line == null) {
            return null;
        }
        String text = stripBom(line);
        if (text.isBlank()) {
            return null;
        }
        if (text.getBytes(StandardCharsets.UTF_8).length > config.maxMessageBytes()) {
            return JsonCodec.error(null, -32700, "message exceeds limit");
        }
        Object parsed;
        try {
            parsed = JsonCodec.parse(text);
        } catch (ProtocolException ex) {
            return JsonCodec.error(null, ex.code(), ex.getMessage());
        }
        if (parsed instanceof List) {
            return JsonCodec.error(null, -32600, "batches are not supported");
        }
        if (!(parsed instanceof Map<?, ?> map)) {
            return JsonCodec.error(null, -32600, "Invalid Request");
        }
        return dispatch(map);
    }

    /**
     * Response for a frame the reader discarded because it was too large.
     *
     * @return a parse error
     */
    public synchronized String tooLarge() {
        ensureOpen();
        return JsonCodec.error(null, -32700, "message exceeds limit");
    }

    /**
     * Response for a frame that was not UTF-8.
     *
     * @return a parse error
     */
    public synchronized String malformed() {
        ensureOpen();
        return JsonCodec.error(null, -32700, "Parse error");
    }

    /**
     * Runs one request body. Package-visible so tests can cover the
     * internal-error path without a tool that throws on purpose.
     *
     * @param id request id
     * @param action result supplier
     * @return one JSON line
     */
    synchronized String respond(final Object id, final Supplier<Object> action) {
        ensureOpen();
        try {
            return JsonCodec.result(id, action.get());
        } catch (ProtocolException ex) {
            return JsonCodec.error(id, ex.code(), ex.getMessage(), ex.data());
        } catch (RuntimeException ex) {
            warn(ex);
            return JsonCodec.error(id, -32603, "Internal error");
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        engine.close();
    }

    private String dispatch(final Map<?, ?> raw) {
        if (!"2.0".equals(raw.get("jsonrpc"))) {
            return errorFor(raw, -32600, "Invalid Request");
        }
        if (!(raw.get("method") instanceof String method) || method.isEmpty()) {
            return errorFor(raw, -32600, "Invalid Request");
        }
        boolean notification = !raw.containsKey("id");
        Object id = null;
        if (!notification) {
            try {
                id = JsonValues.canonicalId(raw.get("id"));
            } catch (ProtocolException ex) {
                return JsonCodec.error(null, ex.code(), ex.getMessage());
            }
        }
        Object params = raw.get("params");
        if (notification) {
            try {
                handle(method, params, true);
            } catch (RuntimeException ex) {
                warn(ex);
            }
            return null;
        }
        Object requestId = id;
        return respond(requestId, () -> handle(method, params, false));
    }

    private Object handle(final String method, final Object params, final boolean notification) {
        if ("initialize".equals(method)) {
            if (notification) {
                throw new ProtocolException(-32600, "Invalid Request");
            }
            return finish(initialize(params), false, false);
        }
        if ("notifications/initialized".equals(method)) {
            if (phase == Phase.INITIALIZED) {
                phase = Phase.READY;
            }
            return Map.of();
        }
        if (method.startsWith("notifications/")) {
            return Map.of();
        }
        if ("server/discover".equals(method)) {
            if (notification) {
                throw new ProtocolException(-32600, "Invalid Request");
            }
            readProtocol(params);
            return finish(discover(), true, true);
        }
        ProtocolChoice choice = readProtocol(params);
        if (choice.isLegacy()) {
            throw unsupportedVersion(choice.version());
        }
        if ("ping".equals(method)) {
            if (choice.isModern()) {
                throw new ProtocolException(-32601, "Method not found");
            }
            return Map.of();
        }
        if (!choice.isModern() && phase == Phase.NEW) {
            throw new ProtocolException(-32002, "Server not initialized");
        }
        if (!choice.isModern() && phase == Phase.INITIALIZED) {
            throw new ProtocolException(-32002, "initialized notification required");
        }
        return finish(dispatch(method, params), cacheable(method), choice.isModern());
    }

    /**
     * Reads the per-request protocol version once. A missing key is the
     * handshake era. {@link #MODERN_PROTOCOL} is the stateless era. A
     * handshake version is kept so {@code server/discover} can accept it
     * and every other method can reject it. Anything else is {@code -32022}.
     *
     * @param params request params, possibly not an object
     * @return the choice
     */
    private static ProtocolChoice readProtocol(final Object params) {
        if (!(params instanceof Map<?, ?> map)) {
            return ProtocolChoice.absent();
        }
        Object meta = map.get("_meta");
        if (!(meta instanceof Map<?, ?> metaMap) || !metaMap.containsKey(PROTOCOL_META)) {
            return ProtocolChoice.absent();
        }
        Object version = metaMap.get(PROTOCOL_META);
        if (version instanceof String text) {
            if (MODERN_PROTOCOL.equals(text)) {
                return ProtocolChoice.modern();
            }
            if (SUPPORTED_PROTOCOLS.contains(text)) {
                return ProtocolChoice.legacy(text);
            }
            throw unsupportedVersion(text);
        }
        throw unsupportedVersion(null);
    }

    private static ProtocolException unsupportedVersion(final String requested) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("supported", supportedVersions());
        if (requested != null) {
            data.put("requested", JsonValues.brief(requested));
        }
        return new ProtocolException(-32022, "Unsupported protocol version", data);
    }

    private static List<String> supportedVersions() {
        List<String> versions = new ArrayList<>();
        versions.add(MODERN_PROTOCOL);
        versions.addAll(SUPPORTED_PROTOCOLS);
        return List.copyOf(versions);
    }

    private Map<String, Object> discover() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("supportedVersions", supportedVersions());
        result.put("capabilities", capabilities());
        result.put("instructions", INSTRUCTIONS);
        return result;
    }

    /**
     * @param result payload for one successful request
     * @param cacheable list and resource reads carry a freshness hint
     * @param identify per-request era results name this server
     * @return the same map, with the envelope fields added
     */
    private Map<String, Object> finish(final Map<String, Object> result, final boolean cacheable,
                                       final boolean identify) {
        result.put("resultType", "complete");
        if (cacheable) {
            result.put("ttlMs", LIST_TTL_MS);
            result.put("cacheScope", "public");
        }
        if (identify) {
            result.put("_meta", Map.of(SERVER_INFO_META, serverInfo()));
        }
        return result;
    }

    private static boolean cacheable(final String method) {
        return switch (method) {
            case "tools/list", "resources/list", "resources/read", "resources/templates/list", "prompts/list" -> true;
            default -> false;
        };
    }

    private Map<String, Object> dispatch(final String method, final Object params) {
        return switch (method) {
            case "tools/list" -> listResult("tools", tools.definitions(), params);
            case "tools/call" -> toolsCall(params);
            case "resources/list" -> listResult("resources", resources.list(), params);
            case "resources/read" -> resourcesRead(params);
            case "resources/templates/list" -> emptyTemplates(params);
            case "prompts/list" -> listResult("prompts", prompts.list(), params);
            case "prompts/get" -> promptsGet(params);
            default -> throw new ProtocolException(-32601, "Method not found");
        };
    }

    private static Map<String, Object> serverInfo() {
        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", "groovy-mcp");
        serverInfo.put("title", "Groovy MCP");
        serverInfo.put("version", implementationVersion());
        serverInfo.put("description", "Compiler queries for Groovy source. Does not run scripts.");
        serverInfo.put("websiteUrl", "https://groovy.apache.org/");
        return serverInfo;
    }

    private Map<String, Object> initialize(final Object params) {
        if (phase != Phase.NEW) {
            throw new ProtocolException(-32600, "already initialized");
        }
        Map<?, ?> map = object(params);
        if (!(map.get("protocolVersion") instanceof String version) || version.isEmpty()) {
            throw new ProtocolException(-32602, "protocolVersion is required");
        }
        String selected = SUPPORTED_PROTOCOLS.contains(version) ? version : PROTOCOL_VERSION;
        phase = Phase.INITIALIZED;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", selected);
        result.put("capabilities", capabilities());
        result.put("serverInfo", serverInfo());
        result.put("instructions", INSTRUCTIONS);
        return result;
    }

    private static Map<String, Object> capabilities() {
        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("subscribe", Boolean.FALSE);
        resources.put("listChanged", Boolean.FALSE);
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", Map.of("listChanged", Boolean.FALSE));
        capabilities.put("resources", resources);
        capabilities.put("prompts", Map.of("listChanged", Boolean.FALSE));
        return capabilities;
    }

    private Map<String, Object> listResult(final String key, final Object value, final Object params) {
        requireParams(params);
        rejectCursor(params);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private Map<String, Object> emptyTemplates(final Object params) {
        requireParams(params);
        rejectCursor(params);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resourceTemplates", List.of());
        return result;
    }

    private Map<String, Object> toolsCall(final Object params) {
        Map<?, ?> map = object(params);
        if (!(map.get("name") instanceof String name) || name.isEmpty()) {
            throw new ProtocolException(-32602, "tool name is required");
        }
        if (map.containsKey("arguments") && map.get("arguments") != null && !(map.get("arguments") instanceof Map)) {
            throw new ProtocolException(-32602, "arguments must be an object");
        }
        try {
            return toolResult(tools.call(name, map.get("arguments")), false);
        } catch (ToolFailure ex) {
            return toolResult(Map.of("error", ex.getMessage()), true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return toolResult(Map.of("error", failureText(ex)), true);
        }
    }

    /**
     * Model-readable text for an engine rejection. A blank message is
     * replaced so the tool result still says what happened.
     *
     * @param failure the rejection
     * @return a non-blank message
     */
    static String failureText(final Throwable failure) {
        String message = failure == null ? null : failure.getMessage();
        if (message == null || message.isBlank()) {
            return "invalid request";
        }
        return message;
    }

    private Map<String, Object> toolResult(final Map<String, Object> structured, final boolean error) {
        Map<String, Object> body = structured;
        boolean failed = error;
        String json = JsonOutput.toJson(body);
        if (json.length() > config.maxResultChars()) {
            body = cappedError("result exceeds the size limit");
            json = JsonOutput.toJson(body);
            failed = true;
            if (json.length() > config.maxResultChars()) {
                body = cappedError("too large");
                json = JsonOutput.toJson(body);
            }
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", json);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(content));
        result.put("isError", failed);
        result.put("structuredContent", body);
        return result;
    }

    private static Map<String, Object> cappedError(final String message) {
        return Map.of("error", message);
    }

    private Map<String, Object> resourcesRead(final Object params) {
        Map<?, ?> map = object(params);
        if (!(map.get("uri") instanceof String uri) || uri.isEmpty()) {
            throw new ProtocolException(-32602, "uri is required");
        }
        return resources.read(uri);
    }

    private Map<String, Object> promptsGet(final Object params) {
        Map<?, ?> map = object(params);
        if (!(map.get("name") instanceof String name) || name.isEmpty()) {
            throw new ProtocolException(-32602, "prompt name is required");
        }
        if (map.containsKey("arguments") && map.get("arguments") != null && !(map.get("arguments") instanceof Map)) {
            throw new ProtocolException(-32602, "arguments must be an object");
        }
        try {
            return prompts.get(name, map.get("arguments"));
        } catch (ToolFailure ex) {
            throw new ProtocolException(-32602, ex.getMessage());
        }
    }

    private static void requireParams(final Object params) {
        if (params != null && !(params instanceof Map)) {
            throw new ProtocolException(-32602, "params must be an object");
        }
    }

    private static void rejectCursor(final Object params) {
        if (!(params instanceof Map<?, ?> map) || !map.containsKey("cursor")) {
            return;
        }
        Object cursor = map.get("cursor");
        if (cursor == null || (cursor instanceof String text && text.isEmpty())) {
            return;
        }
        throw new ProtocolException(-32602, "unknown cursor");
    }

    private static Map<?, ?> object(final Object params) {
        if (!(params instanceof Map<?, ?> map)) {
            throw new ProtocolException(-32602, "params must be an object");
        }
        return map;
    }

    private String errorFor(final Map<?, ?> raw, final int code, final String message) {
        Object id = null;
        if (raw.containsKey("id")) {
            try {
                id = JsonValues.canonicalId(raw.get("id"));
            } catch (ProtocolException ex) {
                id = null;
            }
        }
        return JsonCodec.error(id, code, message);
    }

    private void warn(final Throwable failure) {
        String detail = failure.getClass().getSimpleName();
        String message = failure.getMessage();
        if (message != null && !message.isBlank()) {
            detail = detail + ": " + JsonValues.brief(message);
        }
        try {
            log.append("groovy-mcp: ").append(detail).append('\n');
        } catch (IOException ignored) {
            // Logging must not break the protocol stream.
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("closed");
        }
    }

    private static String stripBom(final String line) {
        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    /**
     * The protocol era named by one request. {@code version} is set only
     * for a handshake version carried in {@code _meta}, so the caller can
     * repeat that text in {@code -32022} without reading the message again.
     */
    private record ProtocolChoice(Kind kind, String version) {

        private enum Kind {
            ABSENT, MODERN, LEGACY
        }

        private static ProtocolChoice absent() {
            return new ProtocolChoice(Kind.ABSENT, null);
        }

        private static ProtocolChoice modern() {
            return new ProtocolChoice(Kind.MODERN, null);
        }

        private static ProtocolChoice legacy(final String version) {
            return new ProtocolChoice(Kind.LEGACY, version);
        }

        private boolean isModern() {
            return kind == Kind.MODERN;
        }

        private boolean isLegacy() {
            return kind == Kind.LEGACY;
        }
    }
}
