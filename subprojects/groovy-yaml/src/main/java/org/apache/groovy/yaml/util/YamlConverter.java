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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import groovy.yaml.YamlRuntimeException;

import java.io.IOException;
import java.io.Reader;

/**
 *  A converter for converting YAML to JSON, vice versa
 *  @since 3.0.0
 */
public class YamlConverter {
    /**
     * Convert yaml to json
     * @param yamlReader the reader of yaml
     * @return the text of json
     */
    public static String convertYamlToJson(Reader yamlReader) {
        try (Reader reader = yamlReader) {
            Object yaml = new ObjectMapper(new YAMLFactory()).readValue(reader, Object.class);

            return new ObjectMapper().writeValueAsString(yaml);
        } catch (IOException e) {
            throw new YamlRuntimeException(e);
        }
    }

    /**
     * Convert json to yaml
     * @param jsonReader the reader of json
     * @return the text of yaml
     */
    public static String convertJsonToYaml(Reader jsonReader) {
        try (Reader reader = jsonReader) {
            JsonNode json = new ObjectMapper().readTree(reader);

            return new YAMLMapper().writeValueAsString(json);
        } catch (IOException e) {
            throw new YamlRuntimeException(e);
        }
    }

    private YamlConverter() {}
}
