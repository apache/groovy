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
package org.apache.groovy.toml.util;

import groovy.toml.TomlRuntimeException;
import groovy.transform.Internal;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.toml.TomlMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads and writes a TOML root that is not a non-empty table.
 * <p>
 * Jackson 3.3 accepts only a non-empty table at the document root. This module
 * has always written every other root as an empty key ({@code  = [1, 2]}).
 * The value is handed to Jackson under a carrier key, and that key is removed
 * from the text. On read, text that starts with the empty key gets the carrier
 * back for parsing, and the carrier is not part of the result.
 * <p>
 * Internal to the module. Application code uses {@code TomlBuilder} and {@code TomlConverter}.
 */
@Internal
public final class TomlRootValues {
    // Bare key, so Jackson writes "v = ..." and dropping "v" leaves " = ...".
    private static final String ROOT_VALUE_KEY = "v";
    private static final String EMPTY_KEY_PREFIX = " = ";

    private TomlRootValues() {
    }

    /**
     * Writes {@code value} with {@code mapper}. A non-empty table is written as
     * itself, so a bean keeps the mapper's own scalar formatting. Every other
     * value is written as an empty key.
     *
     * @param mapper the mapper whose formatting should be preserved
     * @param value the document root
     * @return TOML text
     * @throws JacksonException when {@code value} cannot be serialized
     * @throws TomlRuntimeException when the carrier key is not written as a bare key
     */
    public static String write(TomlMapper mapper, Object value) throws JacksonException {
        // valueToTree yields a NullNode for null, so the node itself is never null.
        JsonNode node = mapper.valueToTree(value);
        if (node.isObject() && !node.isEmpty()) {
            return mapper.writeValueAsString(value);
        }
        return writeWithEmptyKey(mapper, value);
    }

    /**
     * Reads one document. A leading empty key marks the historical root-value
     * form. Any other key in that document is rejected, so the carrier used to
     * parse it cannot leak into the result.
     *
     * @param mapper the mapper that parses TOML
     * @param reader the document source; it is closed
     * @return the document root
     * @throws JacksonException when the text is not TOML this mapper accepts
     * @throws TomlRuntimeException when the source cannot be read, or an empty
     *         key is followed by another key
     */
    public static Object read(TomlMapper mapper, Reader reader) throws JacksonException {
        String toml = readFully(reader);
        if (!toml.startsWith(EMPTY_KEY_PREFIX)) {
            return mapper.readValue(toml, Object.class);
        }
        Object value = mapper.readValue(ROOT_VALUE_KEY + toml, Object.class);
        if (value instanceof Map<?, ?> map && map.size() == 1 && map.containsKey(ROOT_VALUE_KEY)) {
            return map.get(ROOT_VALUE_KEY);
        }
        throw new TomlRuntimeException("Cannot read a TOML value at the document root");
    }

    // Package-private so a test can pass a table. write() does not wrap one,
    // and Jackson then emits "v.a = ..." instead of the bare carrier.
    static String writeWithEmptyKey(TomlMapper mapper, Object value) throws JacksonException {
        // Map.of rejects null, and a root null is a supported document.
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put(ROOT_VALUE_KEY, value);
        String written = mapper.writeValueAsString(wrapped);
        if (!written.startsWith(ROOT_VALUE_KEY + EMPTY_KEY_PREFIX)) {
            throw new TomlRuntimeException("Cannot write a TOML value at the document root");
        }
        return written.substring(ROOT_VALUE_KEY.length());
    }

    private static String readFully(Reader reader) {
        char[] buffer = new char[2048];
        StringBuilder text = new StringBuilder();
        try (Reader in = reader) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                text.append(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new TomlRuntimeException(e);
        }
        return text.toString();
    }
}
