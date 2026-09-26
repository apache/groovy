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
package org.apache.groovy.yaml.util;

import groovy.yaml.YamlRuntimeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.Reader;
import java.util.List;

/**
 *  A converter for converting YAML to JSON, vice versa
 *  @since 3.0.0
 */
public final class YamlConverter {
    // Jackson 2's defaults keep the conversion producing what it did before Jackson 3;
    // both mappers are thread-safe once built, so they are shared
    private static final JsonMapper JSON_MAPPER = JsonMapper.builderWithJackson2Defaults().build();
    private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().configureForJackson2().build();

    /**
     * Convert yaml to json
     * @param yamlReader the reader of yaml
     * @return the text of json
     */
    public static String convertYamlToJson(Reader yamlReader) {
        try {
            List<Object> resultList = YAML_MAPPER.readerFor(Object.class).readValues(yamlReader).readAll();
            Object yaml = 1 == resultList.size() ? resultList.get(0) : resultList;
            return JSON_MAPPER.writeValueAsString(yaml);
        } catch (JacksonException e) {
            throw new YamlRuntimeException(e);
        }
    }

    /**
     * Convert json to yaml
     * @param jsonReader the reader of json
     * @return the text of yaml
     */
    public static String convertJsonToYaml(Reader jsonReader) {
        try {
            JsonNode json = JSON_MAPPER.readTree(jsonReader);

            return YAML_MAPPER.writeValueAsString(json);
        } catch (JacksonException e) {
            throw new YamlRuntimeException(e);
        }
    }

    private YamlConverter() {}
}
