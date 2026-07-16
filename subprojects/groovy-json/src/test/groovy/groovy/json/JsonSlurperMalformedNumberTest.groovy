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

/**
 * A leading digit or minus makes the parser enter number decoding, but a malformed exponent or a
 * lone sign/point is only rejected when the token is finally handed to {@code new BigDecimal(...)}.
 * The overlay parsers already wrapped that {@link NumberFormatException} in a {@link JsonException}
 * (see {@code NumberValue.bigDecimalValue}); the default CHAR_BUFFER path
 * ({@code CharScanner.parseJsonNumber}) and the CHARACTER_SOURCE path
 * ({@code JsonParserUsingCharacterSource.decodeNumber}) previously let it escape raw, so a caller
 * guarding untrusted input with {@code catch (JsonException)} would not catch it. GROOVY-12168 wraps
 * those two sites as well; these tests pin that every parser type now surfaces a {@link JsonException}.
 */
class JsonSlurperMalformedNumberTest {

    // Each starts with a digit or '-' so number decoding begins, but the token is not a valid number.
    private static final List<String> MALFORMED = ['1e', '1E', '1e+', '1e-', '-.', '[1e]', '{"a":1e}']

    @Test
    void testDefaultParserRejectsMalformedNumber() {
        // The default (CHAR_BUFFER) parser decodes numbers eagerly, so this used to throw
        // NumberFormatException; it must now fail as a JsonException.
        MALFORMED.each { doc ->
            shouldFail(JsonException) {
                new JsonSlurper().parseText(doc)
            }
        }
    }

    @Test
    void testNoParserTypeLeaksNumberFormatError() {
        // The security-relevant invariant: no parser type may surface a raw number-format error for
        // this malformed input; it must be a JsonException or a value, never a NumberFormatException.
        JsonParserType.values().each { type ->
            MALFORMED.each { doc ->
                def thrown = null
                try {
                    // toJson forces the lazy overlay parsers to materialize (decode) their values.
                    JsonOutput.toJson(new JsonSlurper().setType(type).parseText(doc))
                } catch (Throwable t) {
                    thrown = t
                }
                assert !(thrown instanceof NumberFormatException),
                        "$type leaked ${thrown.getClass().name} for '$doc'"
            }
        }
    }

    @Test
    void testValidNumbersStillDecodeForAllParserTypes() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type)
            assert slurper.parseText('{"k":12}').k == 12, "integer for $type"
            assert slurper.parseText('{"k":1.5}').k == 1.5, "decimal for $type"
            assert slurper.parseText('{"k":1.5e3}').k == 1500, "exponent for $type"
        }
    }
}
