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
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.Reader;
import java.util.List;

/**
 *  A converter for converting YAML to JSON, vice versa
 *  @since 3.0.0
 */
public final class YamlConverter {
    // Jackson 3 lowered the default nesting depth, for reading and writing, from 1000 to 500 and
    // raised the default string length from 20,000,000 to 100,000,000. Jackson 2's limits are kept:
    // documents that parsed before still parse, oversized ones still fail, and nesting stays
    // bounded at the 1000 levels JsonSlurper and XmlParser allow.
    private static final StreamReadConstraints READ_CONSTRAINTS = StreamReadConstraints.builder()
            .maxNestingDepth(1000)
            .maxStringLength(20_000_000)
            .build();
    private static final StreamWriteConstraints WRITE_CONSTRAINTS = StreamWriteConstraints.builder()
            .maxNestingDepth(1000)
            .build();

    // Jackson 2's defaults keep the conversion producing what it did before Jackson 3;
    // both mappers are thread-safe once built, so they are shared
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder(JsonFactory.builder()
                    .streamReadConstraints(READ_CONSTRAINTS)
                    .streamWriteConstraints(WRITE_CONSTRAINTS)
                    .build())
            .configureForJackson2()
            .build();
    private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder(YAMLFactory.builder()
                    .streamReadConstraints(READ_CONSTRAINTS)
                    .streamWriteConstraints(WRITE_CONSTRAINTS)
                    .build())
            .configureForJackson2()
            .build();

    /**
     * Convert yaml to json
     * @param yamlReader the reader of yaml
     * @return the text of json
     */
    public static String convertYamlToJson(Reader yamlReader) {
        try {
            // an explicit parser keeps a root-level sequence intact: an ObjectReader would treat it
            // as a wrapped sequence of values and drop the outer list
            List<Object> resultList = YAML_MAPPER.readValues(YAML_MAPPER.createParser(yamlReader), Object.class).readAll();
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
