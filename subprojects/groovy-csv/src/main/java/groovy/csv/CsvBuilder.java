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

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import groovy.lang.Writable;
import org.apache.groovy.lang.annotation.Incubating;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

/**
 * Builds CSV output from collections of maps or typed objects.
 * <p>
 * Example with maps:
 * <pre><code class="groovyTestCase">
 * def data = [[name: 'Alice', age: 30], [name: 'Bob', age: 25]]
 * def csv = groovy.csv.CsvBuilder.toCsv(data)
 * assert csv.contains('name,age')
 * assert csv.contains('Alice,30')
 * </code></pre>
 *
 * @since 6.0.0
 */
@Incubating
public class CsvBuilder implements Writable {
    private final CsvMapper mapper;
    private char separator = ',';
    private char quoteChar = '"';
    private String content;

    public CsvBuilder() {
        this.mapper = new CsvMapper();
    }

    /**
     * Set the column separator character (default: comma).
     *
     * @param separator the separator character
     * @return this builder for chaining
     */
    public CsvBuilder setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    /**
     * Set the quote character (default: double-quote).
     *
     * @param quoteChar the quote character
     * @return this builder for chaining
     */
    public CsvBuilder setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    /**
     * Convert a collection of maps to CSV.
     * The keys of the first map are used as column headers.
     *
     * @param data the collection of maps
     * @return the CSV string
     */
    public static String toCsv(Collection<? extends Map<String, ?>> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        CsvMapper csvMapper = new CsvMapper();
        Map<String, ?> first = data.iterator().next();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String key : first.keySet()) {
            schemaBuilder.addColumn(key);
        }
        CsvSchema schema = schemaBuilder.build().withHeader();
        try {
            return csvMapper.writer(schema).writeValueAsString(data);
        } catch (IOException e) {
            throw new CsvRuntimeException(e);
        }
    }

    /**
     * Convert a collection of typed objects to CSV using Jackson databinding.
     * Supports {@code @JsonProperty} and {@code @JsonFormat} annotations.
     *
     * @param data the collection of objects
     * @param type the object type (used to derive the schema)
     * @param <T> the object type
     * @return the CSV string
     */
    public static <T> String toCsv(Collection<T> data, Class<T> type) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(type).withHeader();
        try {
            return csvMapper.writer(schema).writeValueAsString(data);
        } catch (IOException e) {
            throw new CsvRuntimeException(e);
        }
    }

    /**
     * Build CSV from a collection of maps.
     *
     * @param data the collection of maps
     * @return this builder
     */
    public CsvBuilder call(Collection<? extends Map<String, ?>> data) {
        if (data == null || data.isEmpty()) {
            this.content = "";
            return this;
        }
        Map<String, ?> first = data.iterator().next();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder()
                .setColumnSeparator(separator)
                .setQuoteChar(quoteChar);
        for (String key : first.keySet()) {
            schemaBuilder.addColumn(key);
        }
        CsvSchema schema = schemaBuilder.build().withHeader();
        try {
            this.content = mapper.writer(schema).writeValueAsString(data);
        } catch (IOException e) {
            throw new CsvRuntimeException(e);
        }
        return this;
    }

    @Override
    public String toString() {
        return content != null ? content : "";
    }

    @Override
    public Writer writeTo(Writer out) throws IOException {
        return out.append(toString());
    }
}
