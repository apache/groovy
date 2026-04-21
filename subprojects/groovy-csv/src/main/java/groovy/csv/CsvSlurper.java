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
package groovy.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.groovy.lang.annotation.Incubating;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents a CSV parser.
 * <p>
 * Usage:
 * <pre><code class="language-groovy groovyTestCase">
 * def csv = new groovy.csv.CsvSlurper().parseText('name,age\nAlice,30\nBob,25')
 * assert csv[0].name == 'Alice'
 * assert csv[1].age == '25'
 * </code></pre>
 *
 * @since 6.0.0
 */
@Incubating
public class CsvSlurper {
    private final CsvMapper mapper;
    private char separator = ',';
    private char quoteChar = '"';
    private boolean useHeader = true;

    public CsvSlurper() {
        this.mapper = new CsvMapper();
    }

    /**
     * Set the column separator character (default: comma).
     *
     * @param separator the separator character
     * @return this slurper for chaining
     */
    public CsvSlurper setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    /**
     * Set the quote character (default: double-quote).
     *
     * @param quoteChar the quote character
     * @return this slurper for chaining
     */
    public CsvSlurper setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    /**
     * Set whether the first row is a header row (default: true).
     *
     * @param useHeader true to treat the first row as headers
     * @return this slurper for chaining
     */
    public CsvSlurper setUseHeader(boolean useHeader) {
        this.useHeader = useHeader;
        return this;
    }

    /**
     * Parse the content of the specified CSV text.
     *
     * @param csv the CSV text
     * @return a list of maps (one per row), keyed by column headers
     */
    public List<Map<String, String>> parseText(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return parse(new StringReader(csv));
    }

    /**
     * Parse CSV from a reader.
     * When {@code useHeader} is true (the default), each row is returned as a map keyed
     * by column headers from the first row. When {@code useHeader} is false, maps are
     * keyed by auto-generated column names.
     *
     * @param reader the reader of CSV
     * @return a list of maps (one per row)
     */
    public List<Map<String, String>> parse(Reader reader) {
        try {
            CsvSchema schema = buildSchema();
            MappingIterator<Map<String, String>> it = mapper
                    .readerFor(Map.class)
                    .with(schema)
                    .readValues(reader);
            List<Map<String, String>> result = it.readAll();
            // Jackson may return a single empty map for header-only input
            if (result.size() == 1 && result.get(0).isEmpty()) {
                result.clear();
            }
            return result;
        } catch (IOException e) {
            throw new CsvRuntimeException(e);
        }
    }

    /**
     * Parse CSV from an input stream. The caller is responsible for closing the stream.
     *
     * @param stream the input stream of CSV
     * @return a list of maps (one per row)
     */
    public List<Map<String, String>> parse(InputStream stream) {
        return parse(new InputStreamReader(stream));
    }

    /**
     * Parse CSV from a file.
     *
     * @param file the CSV file
     * @return a list of maps (one per row), keyed by column headers
     */
    public List<Map<String, String>> parse(File file) throws IOException {
        return parse(file.toPath());
    }

    /**
     * Parse CSV from a path.
     *
     * @param path the path to the CSV file
     * @return a list of maps (one per row), keyed by column headers
     */
    public List<Map<String, String>> parse(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return parse(new InputStreamReader(stream));
        }
    }

    /**
     * Parse CSV into typed objects using Jackson databinding.
     * Supports {@code @JsonProperty} and {@code @JsonFormat} annotations for
     * column mapping and type conversion.
     *
     * @param type the target type
     * @param csv the CSV text
     * @param <T> the target type
     * @return a list of typed objects
     */
    public <T> List<T> parseAs(Class<T> type, String csv) {
        return parseAs(type, new StringReader(csv));
    }

    /**
     * Parse CSV from a reader into typed objects.
     *
     * @param type the target type
     * @param reader the reader of CSV
     * @param <T> the target type
     * @return a list of typed objects
     */
    public <T> List<T> parseAs(Class<T> type, Reader reader) {
        try {
            // Use empty schema with header — Jackson matches columns by name
            // rather than by position, allowing CSV column order to differ from field order
            CsvSchema schema = CsvSchema.emptySchema();
            if (useHeader) {
                schema = schema.withHeader();
            }
            schema = schema.rebuild()
                    .setColumnSeparator(separator)
                    .setQuoteChar(quoteChar)
                    .build();
            MappingIterator<T> it = mapper
                    .readerFor(type)
                    .with(schema)
                    .readValues(reader);
            return it.readAll();
        } catch (IOException e) {
            throw new CsvRuntimeException(e);
        }
    }

    /**
     * Parse CSV from a file into typed objects.
     *
     * @param type the target type
     * @param file the CSV file
     * @param <T> the target type
     * @return a list of typed objects
     */
    public <T> List<T> parseAs(Class<T> type, File file) throws IOException {
        return parseAs(type, file.toPath());
    }

    /**
     * Parse CSV from a path into typed objects.
     *
     * @param type the target type
     * @param path the path to the CSV file
     * @param <T> the target type
     * @return a list of typed objects
     */
    public <T> List<T> parseAs(Class<T> type, Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return parseAs(type, new InputStreamReader(stream));
        }
    }

    private CsvSchema buildSchema() {
        CsvSchema.Builder builder = CsvSchema.builder()
                .setColumnSeparator(separator)
                .setQuoteChar(quoteChar);
        CsvSchema schema = builder.build();
        if (useHeader) {
            schema = schema.withHeader();
        }
        return schema;
    }
}
