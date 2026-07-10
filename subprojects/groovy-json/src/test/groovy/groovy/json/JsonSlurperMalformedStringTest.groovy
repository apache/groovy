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
package groovy.json

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * A JSON string whose escape sequence is truncated by the end of the document used to run
 * {@link org.apache.groovy.json.internal.CharBuf#decodeJsonString} one character off the end of the
 * slice: the escape branch was guarded by {@code index < to}, always true inside the loop, so a
 * backslash in the final position did {@code index++} and read {@code chars[to]}. On the default
 * {@link JsonSlurper} that surfaced as a raw {@link ArrayIndexOutOfBoundsException} instead of the
 * documented {@link JsonException}, so a caller guarding untrusted input with {@code catch
 * (JsonException)} would not catch it.
 */
class JsonSlurperMalformedStringTest {

    // Each ends in a lone (odd) trailing backslash with no closing quote.
    private static final List<String> DANGLING = ['"\\', '{"a":"x\\']

    @Test
    void testDefaultParserRejectsDanglingEscape() {
        // The default (CHAR_BUFFER) parser decodes strings eagerly, so this used to throw
        // ArrayIndexOutOfBoundsException; it must now fail as a JsonException.
        DANGLING.each { doc ->
            shouldFail(JsonException) {
                new JsonSlurper().parseText(doc)
            }
        }
    }

    @Test
    void testNoParserTypeLeaksOutOfBoundsError() {
        // The security-relevant invariant: no parser type may surface a raw out-of-bounds error for
        // this malformed input; it must be a JsonException or a value, never an IndexOutOfBoundsException.
        JsonParserType.values().each { type ->
            DANGLING.each { doc ->
                def thrown = null
                try {
                    // toJson forces the lazy overlay parsers to materialize (decode) their values.
                    JsonOutput.toJson(new JsonSlurper().setType(type).parseText(doc))
                } catch (Throwable t) {
                    thrown = t
                }
                assert !(thrown instanceof IndexOutOfBoundsException),
                        "$type leaked ${thrown.getClass().name} for '$doc'"
            }
        }
    }

    @Test
    void testValidEscapesStillDecodeForAllParserTypes() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type)
            assertEquals('a\\b', slurper.parseText('{"k":"a\\\\b"}').k, "backslash escape for $type")
            assertEquals('a\nb', slurper.parseText('{"k":"a\\nb"}').k, "newline escape for $type")
            assertEquals('A', slurper.parseText('{"k":"\\u0041"}').k, "unicode escape for $type")
        }
    }
}
