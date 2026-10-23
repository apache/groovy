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
import org.apache.groovy.lang.annotation.Incubating;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.toml.TomlMapper;

import java.io.Reader;

/**
 * Converts between TOML and JSON text representations.
 *
 *  @since 4.0.0
 */
@Incubating
public final class TomlConverter {
    // Jackson 2's defaults keep the conversion producing what it did before Jackson 3;
    // both mappers are thread-safe once built, so they are shared
    private static final JsonMapper JSON_MAPPER = JsonMapper.builderWithJackson2Defaults().build();
    private static final TomlMapper TOML_MAPPER = TomlMapper.builder().configureForJackson2().build();

    /**
     * Converts TOML content from the supplied reader into JSON text.
     *
     * @param tomlReader the reader that provides TOML content
     * @return the equivalent JSON text
     */
    public static String convertTomlToJson(Reader tomlReader) {
        try {
            Object toml = TOML_MAPPER.readValue(tomlReader, Object.class);

            return JSON_MAPPER.writeValueAsString(toml);
        } catch (JacksonException e) {
            throw new TomlRuntimeException(e);
        }
    }

    /**
     * Converts JSON content from the supplied reader into TOML text.
     *
     * @param jsonReader the reader that provides JSON content
     * @return the equivalent TOML text
     */
    public static String convertJsonToToml(Reader jsonReader) {
        try {
            JsonNode json = JSON_MAPPER.readTree(jsonReader);

            return TOML_MAPPER.writeValueAsString(json);
        } catch (JacksonException e) {
            throw new TomlRuntimeException(e);
        }
    }

    private TomlConverter() {}
}
