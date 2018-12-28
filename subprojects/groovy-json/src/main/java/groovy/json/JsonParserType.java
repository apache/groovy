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

/**
 * Allows selection of parser type for new new JsonSlurper.
 * <p />
 * To enable the INDEX_OVERLAY parser do this:
 *
 * <code><pre>
 *             parser = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY);
 * </pre></code>
 *
 * INDEX_OVERLAY should be your parser of choice.
 * <p />
 * CHAR_BUFFER is the parser of choice due to element of least surprise and need to
 * mimic existing Slurper behavior as much as possible.
 * <p />
 * Use CHARACTER_SOURCE for large file parsing.
 * <p />
 * Use LAX if you want to enable relaxed JSON parsing, i.e., allow comments, no quote strings, etc.
 * <p />
 * Use CHAR_BUFFER for a non-fancy but super fast parser.
 *
 * <p>
 *     Parser speed in order: INDEX_OVERLAY, LAX, CHAR_BUFFER, CHARACTER_SOURCE.
 * </p>
 *
 * Use Cases:
 * <p />
 *
 * <p>
 *     Use LAX for config files as it allows comments.
 *     Use INDEX_OVERLAY for REST calls, WebSocket messages, AJAX, inter process communication, etc.
 *     Use CHAR_BUFFER if eager parsing of ints, dates, longs, are appealing.
 *     Use CHARACTER_SOURCE if you are dealing with large JSON files over 2MB.
 *     INDEX_OVERLAY is highly tuned for object deserialization from JSON.
 * </p>
 * @since 2.3.0
 */
public enum JsonParserType {

    /**
     * Fastest parser, but has pointers (indexes really) to original char buffer.
     * Care must be used if putting parse maps into a long term cache as members of map
     * maybe index overlay objects pointing to original buffer.
     * You can mitigate these risks by using chop and lazy chop.
     * Chop eagerly dices up the buffer so each Value element points to a small copy of the original buffer.
     *
     * Lazy Chop dices up the buffer when a list get or map get is called so if an GPath expression or
     * such is applied.
     *
     * You do not need chop or lazy chop if you are not putting the map into a long term cache.
     * You do not need chop or lazy chop if you are doing object de-serialization.
     * Recommendation is to use this for JSON buffers under 2MB.
     */
    INDEX_OVERLAY,
    /**
     * Parser uses an abstraction that allows it to handle any size file by using a char [] windowing,
     * built on top or Reader. This parser is slower than INDEX_OVERLAY and CHAR_BUFFER, but
     * can handle really large files without OOM exceptions.
     * Although slower than other groovy parsers it is as fast as many common JSON parsers.
     * Recommendation is to use this for JSON buffers over 2MB.
     */
    CHARACTER_SOURCE,
    /**
     * LAX mode allows keys with no quotes, keys with single quotes,
     * strings elements in JSON with no quotes or single quotes.
     * It also allows comments //, # and even multi-line /* comments.
     * LAX is an INDEX_OVERLAY parser.
     * Its speed is comparable to INDEX_OVERLAY. It is slightly slower than INDEX_OVERLAY, but faster than
     * the other options.
     */
    LAX,
    /**
     * This is a basic parser with no index overlay. It is wicked fast, but not as fast at the
     * INDEX_OVERLAY. It should be on average the fastest known JSON parser on the JVM circa Jan 2014.
     * But not as fast as INDEX_OVERLAY.
     */
    CHAR_BUFFER
}
