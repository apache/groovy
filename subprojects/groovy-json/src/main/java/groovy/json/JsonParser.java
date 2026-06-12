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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * This is the parser interface that backs the new JsonSlurper.
 * It was derived from the Boon JSON parser.
 *
 * @since 2.3.0
 */
public interface JsonParser {

    /**
     * Parses JSON text from a {@link String}.
     *
     * @param jsonString the JSON text to parse
     * @return the parsed JSON data structure
     */
    Object parse(String jsonString);

    /**
     * Parses JSON text from a byte array.
     *
     * @param bytes the JSON bytes to parse
     * @return the parsed JSON data structure
     */
    Object parse(byte[] bytes);

    /**
     * Parses JSON text from a byte array using the supplied charset.
     *
     * @param bytes the JSON bytes to parse
     * @param charset the charset used to decode the bytes
     * @return the parsed JSON data structure
     */
    Object parse(byte[] bytes, String charset);

    /**
     * Parses JSON text from a character sequence.
     *
     * @param charSequence the JSON text to parse
     * @return the parsed JSON data structure
     */
    Object parse(CharSequence charSequence);

    /**
     * Parses JSON text from a character array.
     *
     * @param chars the JSON characters to parse
     * @return the parsed JSON data structure
     */
    Object parse(char[] chars);

    /**
     * Parses JSON text read from a {@link Reader}.
     *
     * @param reader the reader supplying JSON text
     * @return the parsed JSON data structure
     */
    Object parse(Reader reader);

    /**
     * Parses JSON content read from an input stream.
     *
     * @param input the input stream supplying JSON content
     * @return the parsed JSON data structure
     */
    Object parse(InputStream input);

    /**
     * Parses JSON content read from an input stream using the supplied charset.
     *
     * @param input the input stream supplying JSON content
     * @param charset the charset used to decode the stream
     * @return the parsed JSON data structure
     */
    Object parse(InputStream input, String charset);

    /**
     * Parses JSON content read from a file using the supplied charset.
     *
     * @param file the file supplying JSON content
     * @param charset the charset used to decode the file
     * @return the parsed JSON data structure
     */
    Object parse(File file, String charset);
}
