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
package groovy.toml;

import groovy.json.JsonSlurper;
import org.apache.groovy.lang.annotation.Incubating;
import org.apache.groovy.toml.util.TomlConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *  Represents a TOML parser
 *
 *  @since 4.0.0
 */
@Incubating
public class TomlSlurper {
    private final JsonSlurper jsonSlurper;

    public TomlSlurper() {
        this.jsonSlurper = new JsonSlurper();
    }

    /**
     * Parse the content of the specified toml into a tree of Nodes.
     *
     * @param toml the content of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parseText(String toml) {
        return this.parse(new StringReader(toml));
    }

    /**
     * Parse the content of the specified reader into a tree of Nodes.
     *
     * @param reader the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(Reader reader) {
        return jsonSlurper.parse(new StringReader(TomlConverter.convertTomlToJson(reader)));
    }

    /**
     * Parse the content of the specified reader into a tree of Nodes.
     *
     * @param stream the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(InputStream stream) {
        return parse(new InputStreamReader(stream));
    }

    /**
     * Parse the content of the specified file into a tree of Nodes.
     *
     * @param file the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(java.io.File file) throws IOException {
        return parse(file.toPath());
    }

    /**
     * Parse the content of the specified path into a tree of Nodes.
     *
     * @param path the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(Path path) throws IOException {
        // note: convert to an input stream to allow the support of foreign file objects
        return parse(Files.newInputStream(path));
    }
}
