/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.json;

import groovy.json.internal.JsonFastParser;
import groovy.json.internal.JsonParserCharArray;
import groovy.json.internal.JsonParserLax;
import groovy.json.internal.JsonParserUsingCharacterSource;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**
 * JSON slurper which parses text or reader content into a data structure of lists and maps.
 * <p>
 * Example usage:
 * <code><pre>
 * def slurper = new JsonSlurper()
 * def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 *
 * assert result.person.name == "Guillaume"
 * assert result.person.age == 33
 * assert result.person.pets.size() == 2
 * assert result.person.pets[0] == "dog"
 * assert result.person.pets[1] == "cat"
 * </pre></code>
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
public class JsonSlurper {


    private int maxSizeForInMemory = 2000000;
    private boolean chop = false;
    private boolean lazyChop = true;
    private boolean checkDates = true;

    private JsonParserType type = JsonParserType.CHAR_BUFFER;

    public int getMaxSizeForInMemory() {
        return maxSizeForInMemory;
    }

    public JsonSlurper setMaxSizeForInMemory( int maxSizeForInMemory ) {
        this.maxSizeForInMemory = maxSizeForInMemory;
        return this;
    }

    public JsonParserType getType() {
        return type;
    }

    public JsonSlurper setType( JsonParserType type ) {
        this.type = type;
        return this;
    }

    public boolean isChop() {
        return chop;
    }

    public JsonSlurper setChop( boolean chop ) {
        this.chop = chop;
        return this;
    }

    public boolean isLazyChop() {
        return lazyChop;
    }

    public JsonSlurper setLazyChop( boolean lazyChop ) {
        this.lazyChop = lazyChop;
        return this;
    }

    public boolean isCheckDates() {
        return checkDates;
    }

    public JsonSlurper setCheckDates( boolean checkDates ) {
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
        if (text == null || "".equals ( text )) {
            throw new IllegalArgumentException ( "Text must not be null"  );
        }
        return createParser().parse( text );
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    public Object parse(Reader reader) {
        Object content;
        JsonParser parser = createParser();
        content = parser.parse(reader);
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

        if (file.length() < maxSizeForInMemory)  {
            return createParser ().parse(file, charset);
        } else {
            return new JsonParserUsingCharacterSource ().parse ( file, charset );
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
            return createParser ().parse ( reader );
        } catch(IOException ioe) {
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
        } catch(IOException ioe) {
            throw new JsonException("Unable to process url: " + url.toString(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }
}
