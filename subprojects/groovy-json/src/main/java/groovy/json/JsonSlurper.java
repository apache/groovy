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
package groovy.json;

import org.apache.groovy.json.internal.JsonFastParser;
import org.apache.groovy.json.internal.JsonParserCharArray;
import org.apache.groovy.json.internal.JsonParserLax;
import org.apache.groovy.json.internal.JsonParserUsingCharacterSource;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

/**
 * This has the same interface as the original JsonSlurper written for version 1.8.0, but its
 * implementation has completely changed. It is now up to 20x faster than before, and its speed
 * competes and often substantially exceeds popular common JSON parsers circa Jan, 2014.
 * <p />
 * JSON slurper parses text or reader content into a data structure of lists and maps.
 * <p>
 * Example usage:
 * <code><pre class="groovyTestCase">
 * def slurper = new groovy.json.JsonSlurper()
 * def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 *
 * assert result.person.name == "Guillaume"
 * assert result.person.age == 33
 * assert result.person.pets.size() == 2
 * assert result.person.pets[0] == "dog"
 * assert result.person.pets[1] == "cat"
 * </pre></code>
 *
 * JsonSlurper can use several types of JSON parsers. Please read the documentation for
 * JsonParserType. There are relaxed mode parsers, large file parser, and index overlay parsers.
 * Don't worry, it is all groovy. JsonSlurper will just work, but understanding the different parser
 * types may allow you to drastically improve the performance of your JSON parsing.
 * <p />
 *
 * Index overlay parsers (INDEX_OVERLAY and LAX) are the fastest JSON parsers.
 * However they are not the default for a good reason.
 * Index overlay parsers  has pointers (indexes really) to original char buffer.
 * Care must be used if putting parsed maps into a long term cache as members of map
 * maybe index overlay objects pointing to original buffer.
 * You can mitigate these risks by using chop and lazy chop properties.
 * <p />
 * Chop eagerly dices up the buffer so each Value element points to a small copy of the original buffer.
 * <p />
 * Lazy Chop dices up the buffer when a list get or map get is called so if an GPath expression or
 * such is applied.
 * <p />
 * You do not need chop or lazy chop if you are NOT putting the map into a long term cache.
 * You do not need chop or lazy chop if you are doing object de-serialization.
 * Recommendation is to use INDEX_OVERLAY for JSON buffers under 2MB.
 * The maxSizeForInMemory is set to 2MB and any file over 2MB will use a parser designed for
 * large files, which is slower than the INDEX_OVERLAY, LAX, and CHAR_BUFFER parsers, but
 * faster than most commonly used JSON parsers on the JVM for most use cases circa January 2014.
 * <p />
 * To enable the INDEX_OVERLAY parser do this:
 *
 * <code><pre>
 *             parser = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY);
 * </pre></code>
 *
 * @see groovy.json.JsonParserType
 * @since 1.8.0
 */
public class JsonSlurper {

    private int maxSizeForInMemory = 2000000;
    private boolean chop = false;
    private boolean lazyChop = true;
    private boolean checkDates = true;

    private JsonParserType type = JsonParserType.CHAR_BUFFER;

    /**
     * Max size before Slurper starts to use windowing buffer parser.
     * @return size of file/buffer
     * @since 2.3
     */
    public int getMaxSizeForInMemory() {
        return maxSizeForInMemory;
    }

    /**
     * Max size before Slurper starts to use windowing buffer parser.
     * @since 2.3
     * @return JsonSlurper
     */
    public JsonSlurper setMaxSizeForInMemory(int maxSizeForInMemory) {
        this.maxSizeForInMemory = maxSizeForInMemory;
        return this;
    }

    /** Parser type.
     * @since 2.3
     * @see groovy.json.JsonParserType
     * @return  type
     */
    public JsonParserType getType() {
        return type;
    }

    /** Parser type.
     * @since 2.3
     * @see groovy.json.JsonParserType
     * @return  JsonSlurper
     */
    public JsonSlurper setType(JsonParserType type) {
        this.type = type;
        return this;
    }

    /** Turns on buffer chopping for index overlay.
     * @since 2.3
     * @see groovy.json.JsonParserType
     * @return  chop on or off
     */
    public boolean isChop() {
        return chop;
    }

    /** Turns on buffer chopping for index overlay.
     * @since 2.3
     * @see groovy.json.JsonParserType
     * @return  JsonSlurper
     */
    public JsonSlurper setChop(boolean chop) {
        this.chop = chop;
        return this;
    }

    /** Turns on buffer lazy chopping for index overlay.
     * @see groovy.json.JsonParserType
     * @return  on or off
     * @since 2.3
     */
    public boolean isLazyChop() {
        return lazyChop;
    }

    /** Turns on buffer lazy chopping for index overlay.
     * @see groovy.json.JsonParserType
     * @return  JsonSlurper
     * @since 2.3
     */
    public JsonSlurper setLazyChop(boolean lazyChop) {
        this.lazyChop = lazyChop;
        return this;
    }

    /**
     * Determine if slurper will automatically parse strings it recognizes as dates. Index overlay only.
     * @return on or off
     * @since 2.3
     */
    public boolean isCheckDates() {
        return checkDates;
    }

    /**
     * Determine if slurper will automatically parse strings it recognizes as dates. Index overlay only.
     * @return on or off
     * @since 2.3
     */
    public JsonSlurper setCheckDates(boolean checkDates) {
        this.checkDates = checkDates;
        return this;
    }

    /**
     * Parse a text representation of a JSON data structure
     *
     * @param text JSON text to parse
     * @return a data structure of lists and maps
     */
    public Object parseText(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text must not be null or empty");
        }
        return createParser().parse(text);
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    public Object parse(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null");
        }

        Object content;
        JsonParser parser = createParser();
        content = parser.parse(reader);
        return content;
    }

    /**
     * Parse a JSON data structure from content from an inputStream
     *
     * @param inputStream stream over a JSON content
     * @return a data structure of lists and maps
     * @since 2.3
     */
    public Object parse(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }

        Object content;
        JsonParser parser = createParser();
        content = parser.parse(inputStream);
        return content;
    }

    /**
     * Parse a JSON data structure from content from an inputStream
     *
     * @param inputStream stream over a JSON content
     * @param charset charset
     * @return a data structure of lists and maps
     * @since 2.3
     */
    public Object parse(InputStream inputStream, String charset) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null");
        }

        Object content;
        content = createParser().parse(inputStream, charset);
        return content;
    }

    /**
     * Parse a JSON data structure from content from a byte array.
     *
     * @param bytes buffer of JSON content
     * @param charset charset
     * @return a data structure of lists and maps
     * @since 2.3
     */
    public Object parse(byte [] bytes, String charset) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null");
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null");
        }

        Object content;
        content = createParser().parse(bytes, charset);
        return content;
    }

    /**
     * Parse a JSON data structure from content from a byte array.
     *
     * @param bytes buffer of JSON content
     * @return a data structure of lists and maps
     * @since 2.3
     */
    public Object parse(byte [] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null");
        }

        Object content;
        content = createParser().parse(bytes);
        return content;
    }

    /**
     * Parse a JSON data structure from content from a char array.
     *
     * @param chars buffer of JSON content
     * @return a data structure of lists and maps
     * @since 2.3
     */
    public Object parse(char [] chars) {
        if (chars == null) {
            throw new IllegalArgumentException("chars must not be null");
        }

        Object content;
        content = createParser().parse(chars);
        return content;
    }

    private JsonParser createParser() {
        switch (type) {
            case LAX:
                return new JsonParserLax(false, chop, lazyChop, checkDates);

            case CHAR_BUFFER:
                return new JsonParserCharArray();

            case CHARACTER_SOURCE:
                return new JsonParserUsingCharacterSource();


            case INDEX_OVERLAY:
                return new JsonFastParser(false, chop, lazyChop, checkDates);

            default:
                return new JsonParserCharArray();
        }
    }

    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file) {
        return parseFile(file, null);
    }

    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file, String charset) {
        return parseFile(file, charset);
    }

    private Object parseFile(File file, String charset) {
        if (file.length() < maxSizeForInMemory) {
            return createParser().parse(file, charset);
        } else {
            return new JsonParserUsingCharacterSource().parse(file, charset);
        }
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url) {
        return parseURL(url, null);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params) {
        return parseURL(url, params);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url) {
        return parseURL(url, params);
    }

    private Object parseURL(URL url, Map params) {
        Reader reader = null;
        try {
            if (params == null || params.isEmpty()) {
                reader = ResourceGroovyMethods.newReader(url);
            } else {
                reader = ResourceGroovyMethods.newReader(url, params);
            }
            return createParser().parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process url: " + url.toString(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, String charset) {
        return parseURL(url, null, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params, String charset) {
        return parseURL(url, params, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url, String charset) {
        return parseURL(url, params, charset);
    }

    private Object parseURL(URL url, Map params, String charset) {
        Reader reader = null;
        try {
            if (params == null || params.isEmpty()) {
                reader = ResourceGroovyMethods.newReader(url, charset);
            } else {
                reader = ResourceGroovyMethods.newReader(url, params, charset);
            }
            return parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process url: " + url.toString(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }
}
