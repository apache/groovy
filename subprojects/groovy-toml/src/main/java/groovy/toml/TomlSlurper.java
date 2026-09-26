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

import groovy.json.JsonDateHandling;
import groovy.json.JsonParserType;
import groovy.json.JsonSlurper;
import org.apache.groovy.lang.annotation.Incubating;
import org.apache.groovy.toml.util.TomlConverter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.dataformat.toml.TomlFactory;
import tools.jackson.dataformat.toml.TomlMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
    private JsonDateHandling dateHandling = JsonDateHandling.STRING;

    /**
     * Creates a TOML parser that produces standard Groovy data structures.
     */
    public TomlSlurper() {
        this.jsonSlurper = new JsonSlurper();
    }

    /**
     * Returns how a date-like string is handled when parsing.
     *
     * @return the current date handling, {@link JsonDateHandling#STRING} by default
     * @see #setDateHandling(JsonDateHandling)
     * @since 6.0.0
     */
    public JsonDateHandling getDateHandling() {
        return dateHandling;
    }

    /**
     * Chooses what a string in full ISO-8601 or JSON-date form becomes, matching the
     * date handling {@link JsonSlurper} offers. The default is {@link JsonDateHandling#STRING}:
     * a date-like string is left as a {@code String}, which is what this slurper has always
     * produced. A date carrying no time, such as {@code "2026-09-04"}, is left as a
     * {@code String} whatever is chosen here.
     * <p>
     * Choosing anything other than {@link JsonDateHandling#STRING} switches the underlying
     * parser to {@link JsonParserType#INDEX_OVERLAY} so the conversion takes effect; as a
     * consequence, the iteration order of a map with more than a handful of entries is no
     * longer the document order. Pass {@code null} or {@link JsonDateHandling#STRING} to
     * restore the default parser and string handling.
     *
     * @param dateHandling what a date-like string should become
     * @return this slurper, for chaining
     * @since 6.0.0
     */
    public TomlSlurper setDateHandling(JsonDateHandling dateHandling) {
        this.dateHandling = dateHandling == null ? JsonDateHandling.STRING : dateHandling;
        if (this.dateHandling == JsonDateHandling.STRING) {
            jsonSlurper.setType(JsonParserType.CHAR_BUFFER);
        } else {
            jsonSlurper.setType(JsonParserType.INDEX_OVERLAY);
            jsonSlurper.setDateHandling(this.dateHandling);
        }
        return this;
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
        return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Parse the content of the specified file into a tree of Nodes.
     *
     * @param file the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(File file) throws IOException {
        return parse(file.toPath());
    }

    /**
     * Parse the content of the specified path into a tree of Nodes.
     *
     * @param path the reader of toml
     * @return the root node of the parsed tree of Nodes
     */
    public Object parse(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    /**
     * Parse the content of the specified TOML text into a typed object using Jackson databinding.
     * Supports {@code @JsonProperty} and {@code @JsonFormat} annotations for
     * property mapping and type conversion.
     *
     * @param type the target type
     * @param toml the content of TOML
     * @param <T> the target type
     * @return a typed object
     * @since 6.0.0
     */
    public <T> T parseTextAs(Class<T> type, String toml) {
        return parseAs(type, new StringReader(toml));
    }

    /**
     * Parse TOML from a reader into a typed object.
     *
     * @param type the target type
     * @param reader the reader of TOML
     * @param <T> the target type
     * @return a typed object
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, Reader reader) {
        try {
            return mapper().readValue(reader, type);
        } catch (JacksonException e) {
            throw new TomlRuntimeException(e);
        }
    }

    // the same limits as TomlConverter, which handles the untyped parse path: Jackson 2's, not
    // Jackson 3's lower nesting depth and higher string length
    private static final StreamReadConstraints READ_CONSTRAINTS = StreamReadConstraints.builder()
            .maxNestingDepth(1000)
            .maxStringLength(20_000_000)
            .build();
    private static final StreamWriteConstraints WRITE_CONSTRAINTS = StreamWriteConstraints.builder()
            .maxNestingDepth(1000)
            .build();

    // TomlMapper is thread-safe once configured, so a single shared instance is reused.
    // Jackson 2's defaults keep databinding behaving as it did before Jackson 3
    // (for example failing on an unknown property); java.time support is built in.
    private static final TomlMapper MAPPER = TomlMapper.builder(TomlFactory.builder()
                    .streamReadConstraints(READ_CONSTRAINTS)
                    .streamWriteConstraints(WRITE_CONSTRAINTS)
                    .build())
            .configureForJackson2()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .build();

    static TomlMapper mapper() {
        return MAPPER;
    }

    /**
     * Parse TOML from an input stream into a typed object.
     *
     * @param type the target type
     * @param stream the input stream of TOML
     * @param <T> the target type
     * @return a typed object
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, InputStream stream) {
        return parseAs(type, new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Parse TOML from a file into a typed object.
     *
     * @param type the target type
     * @param file the TOML file
     * @param <T> the target type
     * @return a typed object
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, File file) throws IOException {
        return parseAs(type, file.toPath());
    }

    /**
     * Parse TOML from a path into a typed object.
     *
     * @param type the target type
     * @param path the path to the TOML file
     * @param <T> the target type
     * @return a typed object
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return parseAs(type, new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }
}
