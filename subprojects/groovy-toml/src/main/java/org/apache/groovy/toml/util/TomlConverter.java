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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import groovy.toml.TomlRuntimeException;
import org.apache.groovy.lang.annotation.Incubating;

import java.io.IOException;
import java.io.Reader;

/**
 *  A converter for converting TOML to JSON, vice versa
 *  @since 4.0.0
 */
@Incubating
public final class TomlConverter {
    /**
     * Convert toml to json
     * @param tomlReader the reader of toml
     * @return the text of json
     */
    public static String convertTomlToJson(Reader tomlReader) {
        try (Reader reader = tomlReader) {
            Object yaml = new ObjectMapper(new TomlFactory()).readValue(reader, Object.class);

            return new ObjectMapper().writeValueAsString(yaml);
        } catch (IOException e) {
            throw new TomlRuntimeException(e);
        }
    }

    /**
     * Convert json to toml
     * @param jsonReader the reader of json
     * @return the text of toml
     */
    public static String convertJsonToToml(Reader jsonReader) {
        try (Reader reader = jsonReader) {
            JsonNode json = new ObjectMapper().readTree(reader);

            return new TomlMapper().writeValueAsString(json);
        } catch (IOException e) {
            throw new TomlRuntimeException(e);
        }
    }

    private TomlConverter() {}
}
