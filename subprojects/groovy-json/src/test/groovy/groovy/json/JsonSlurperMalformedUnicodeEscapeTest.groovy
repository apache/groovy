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
 * RFC 8259 requires a {@code \\u} escape to carry exactly four hexadecimal digits, and
 * {@link JsonTokenType#STRING} validates exactly that for the lexer, so {@link JsonSlurperClassic}
 * rejects a malformed escape. {@link org.apache.groovy.json.internal.CharBuf#decodeJsonString}, the
 * decoder shared by every {@link JsonParserType}, did not: it read the digits with
 * {@code Integer.parseInt(hex, 16)}, which also accepts a leading sign and non-ASCII Unicode digits,
 * and skipped the escape altogether when fewer than four characters remained. So the same document
 * that the lexer refused was accepted by {@link JsonSlurper} and decoded to text the document never
 * specified: {@code "\\u12"} to the literal {@code 12}, {@code "\\u+041"} to {@code A}.
 */
class JsonSlurperMalformedUnicodeEscapeTest {

    // The Arabic-Indic digits for 1234; Integer.parseInt accepts these as hex, a JSON parser must not.
    private static final String ARABIC_INDIC_DIGITS = '١٢٣٤'

    private static final Map<String, String> MALFORMED = [
            'truncated by the closing quote': '{"k":"\\u12"}',
            'no digits at all'              : '{"k":"\\u"}',
            'leading plus sign'             : '{"k":"\\u+041"}',
            'leading minus sign'            : '{"k":"\\u-001"}',
            'non-ASCII digits'              : '{"k":"\\u' + ARABIC_INDIC_DIGITS + '"}',
            'not hexadecimal at all'        : '{"k":"\\uZZZZ"}',
    ]

    @Test
    void testEveryParserTypeRejectsMalformedUnicodeEscape() {
        JsonParserType.values().each { type ->
            MALFORMED.each { description, doc ->
                shouldFail(JsonException) {
                    // toJson forces the lazy overlay parsers to materialize (decode) their values.
                    JsonOutput.toJson(new JsonSlurper().setType(type).parseText(doc))
                }
            }
        }
    }

    @Test
    void testClassicSlurperRejectsMalformedUnicodeEscape() {
        MALFORMED.each { description, doc ->
            shouldFail(RuntimeException) {
                new JsonSlurperClassic().parseText(doc)
            }
        }
    }

    @Test
    void testValidUnicodeEscapesStillDecodeForEveryParserType() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type)
            assertValidEscapes("parser type $type") { String doc -> slurper.parseText(doc).k }
        }
    }

    @Test
    void testValidUnicodeEscapesStillDecodeForClassicSlurper() {
        def slurper = new JsonSlurperClassic()
        assertValidEscapes('JsonSlurperClassic') { String doc -> slurper.parseText(doc).k }
    }

    private static void assertValidEscapes(String context, Closure<String> parse) {
        assertEquals('A', parse('{"k":"\\u0041"}'), "basic escape for $context")
        assertEquals(0xE9, (int) parse('{"k":"\\u00e9"}').charAt(0), "lower-case digits for $context")
        assertEquals(0xE9, (int) parse('{"k":"\\u00E9"}').charAt(0), "upper-case digits for $context")
        assertEquals(0x0000, (int) parse('{"k":"\\u0000"}').charAt(0), "lowest code unit for $context")
        assertEquals(0xFFFF, (int) parse('{"k":"\\uffff"}').charAt(0), "highest code unit for $context")

        // a surrogate pair is two escapes, and ordinary text may follow the fourth digit
        String pair = parse('{"k":"\\ud83d\\ude00ef"}')
        assertEquals(4, pair.length(), "pair length for $context")
        assertEquals(0x1F600, pair.codePointAt(0), "pair code point for $context")
        assertEquals('ef', pair.substring(2), "text after escape for $context")
    }
}
