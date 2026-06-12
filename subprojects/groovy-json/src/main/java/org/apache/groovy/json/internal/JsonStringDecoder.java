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
package org.apache.groovy.json.internal;

/**
 * Decodes JSON string escape sequences from character buffers.
 */
public class JsonStringDecoder {

    /**
     * Decodes a JSON string slice, avoiding extra work when no escapes are present.
     *
     * @param chars source character buffer
     * @param start inclusive start index
     * @param to exclusive end index
     * @return decoded string value
     */
    public static String decode(char[] chars, int start, int to) {
        if (!Chr.contains(chars, '\\', start, to - start)) {
            return new String(chars, start, to - start);
        }
        return decodeForSure(chars, start, to);
    }

    /**
     * Decodes a JSON string slice assuming escape processing may be required.
     *
     * @param chars source character buffer
     * @param start inclusive start index
     * @param to exclusive end index
     * @return decoded string value
     */
    public static String decodeForSure(char[] chars, int start, int to) {
        // consider wrapping in a try with resources block if CharBuf is ever refactored to have a non-empty close()
        CharBuf builder = CharBuf.create(to - start);
        builder.decodeJsonString(chars, start, to);
        return builder.toString();
    }
}
