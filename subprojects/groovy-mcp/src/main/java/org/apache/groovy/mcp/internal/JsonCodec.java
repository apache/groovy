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
import groovy.json.JsonSlurper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-RPC bodies on top of {@link JsonSlurper} and {@link JsonOutput}.
 * Nesting is capped. Output is compact, so a message stays one stdio line.
 */
public final class JsonCodec {

    /** MCP payloads are shallow. Deeper input is rejected before it expands. */
    public static final int MAX_NESTING = 32;

    private JsonCodec() {
    }

    /**
     * @param text one JSON value
     * @return the slurped value
     */
    public static Object parse(final String text) {
        try {
            return new JsonSlurper().setMaxNestingDepth(MAX_NESTING).parseText(text);
        } catch (RuntimeException ex) {
            throw new ProtocolException(-32700, "Parse error");
        }
    }

    /**
     * @param id request id
     * @param value result object
     * @return one JSON line, without a trailing newline
     */
    public static String result(final Object id, final Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("jsonrpc", "2.0");
        map.put("id", id);
        map.put("result", value);
        return JsonOutput.toJson(map);
    }

    /**
     * @param id request id, or {@code null} when the id is unknown
     * @param code JSON-RPC error code
     * @param message short message
     * @return one JSON line, without a trailing newline
     */
    public static String error(final Object id, final int code, final String message) {
        return error(id, code, message, null);
    }

    /**
     * @param id request id, or {@code null} when the id is unknown
     * @param code JSON-RPC error code
     * @param message short message
     * @param data optional {@code error.data}, omitted when {@code null}
     * @return one JSON line, without a trailing newline
     */
    public static String error(final Object id, final int code, final String message, final Map<String, Object> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message == null ? "" : message);
        if (data != null) {
            body.put("data", data);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("jsonrpc", "2.0");
        map.put("id", id);
        map.put("error", body);
        return JsonOutput.toJson(map);
    }
}
