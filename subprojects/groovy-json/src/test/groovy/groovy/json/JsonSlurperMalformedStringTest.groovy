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
 * <p>
 * The same decoder was also lenient about the four hexadecimal digits that RFC 8259 requires a
 * {@code \\u} escape to carry. {@link JsonTokenType#STRING} validates exactly that for the lexer, so
 * {@link JsonSlurperClassic} rejects a malformed escape; the decoder shared by every
 * {@link JsonParserType} did not. It read the digits with {@code Integer.parseInt(hex, 16)}, which
 * also accepts a leading sign and non-ASCII Unicode digits, and skipped the escape altogether when
 * fewer than four characters remained. So the same document that the lexer refused was accepted by
 * {@link JsonSlurper} and decoded to text the document never specified: {@code "\\u12"} to the
 * literal {@code 12}, {@code "\\u+041"} to {@code A}.
 */
class JsonSlurperMalformedStringTest {

    // Each ends in a lone (odd) trailing backslash with no closing quote.
    private static final List<String> DANGLING = ['"\\', '{"a":"x\\']

    // The Arabic-Indic digits for 1234; Integer.parseInt accepts these as hex, a JSON parser must not.
    private static final String ARABIC_INDIC_DIGITS = '١٢٣٤'

    private static final Map<String, String> MALFORMED_ESCAPE = [
            'truncated by the closing quote': '{"k":"\\u12"}',
            'no digits at all'              : '{"k":"\\u"}',
            'leading plus sign'             : '{"k":"\\u+041"}',
            'leading minus sign'            : '{"k":"\\u-001"}',
            'non-ASCII digits'              : '{"k":"\\u' + ARABIC_INDIC_DIGITS + '"}',
            'not hexadecimal at all'        : '{"k":"\\uZZZZ"}',
    ]

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

    @Test
    void testEveryParserTypeRejectsMalformedUnicodeEscape() {
        JsonParserType.values().each { type ->
            MALFORMED_ESCAPE.each { description, doc ->
                assertRejected("parser type $type", description) {
                    // toJson forces the lazy overlay parsers to materialize (decode) their values.
                    JsonOutput.toJson(new JsonSlurper().setType(type).parseText(doc))
                }
            }
        }
    }

    @Test
    void testClassicSlurperRejectsMalformedUnicodeEscape() {
        MALFORMED_ESCAPE.each { description, doc ->
            assertRejected('JsonSlurperClassic', description) {
                new JsonSlurperClassic().parseText(doc)
            }
        }
    }

    @Test
    void testValidUnicodeEscapesStillDecodeForEveryParserType() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type)
            assertValidUnicodeEscapes("parser type $type") { String doc -> slurper.parseText(doc).k }
        }
    }

    @Test
    void testValidUnicodeEscapesStillDecodeForClassicSlurper() {
        def slurper = new JsonSlurperClassic()
        assertValidUnicodeEscapes('JsonSlurperClassic') { String doc -> slurper.parseText(doc).k }
    }

    /**
     * GROOVY-12272: the lexer used to re-validate the whole accumulated token at every unescaped
     * quote, so a string holding an invalid escape followed by many quotes cost O(n^2). The
     * escape is now checked where it is read, which both bounds the work and reports the
     * position of the offending escape rather than of the end of the document.
     */
    @Test
    void testInvalidEscapeFollowedByManyQuotesStaysLinear() {
        def document = { int quotes -> '{"k":"\\q' + ('"' * quotes) + '"}' }

        // Warm up so the measurement is not dominated by class loading and JIT.
        3.times { parseIgnoringFailure(document(2000)) }

        long small = timeParse(document(4000))
        long large = timeParse(document(16000))

        // Four times the input. Quadratic cost would be about sixteen times the work; allow a
        // wide margin so the test is about the growth curve, not the absolute speed.
        assert large < Math.max(small, 5) * 8,
                "parse time grew from ${small}ms to ${large}ms for a 4x larger document"
    }

    @Test
    void testInvalidEscapeIsReportedWhereItOccurs() {
        def e = shouldFail(JsonException) {
            new JsonSlurperClassic().parseText('{"k":"a\\qb"}')
        }
        // The report names the escape, not the whole remaining document.
        assert e.message.contains('\\q')
    }

    private static void parseIgnoringFailure(String text) {
        try {
            new JsonSlurperClassic().parseText(text)
        } catch (JsonException ignored) {
        }
    }

    private static long timeParse(String text) {
        long start = System.nanoTime()
        parseIgnoringFailure(text)
        (System.nanoTime() - start) / 1_000_000L
    }

    private static void assertRejected(String context, String description, Closure parse) {
        def thrown = null
        try {
            parse()
        } catch (Throwable t) {
            thrown = t
        }
        assert thrown instanceof JsonException,
                "$context must reject a \\u escape $description, but got " +
                        (thrown == null ? 'no failure' : thrown.getClass().name)
    }

    private static void assertValidUnicodeEscapes(String context, Closure<String> parse) {
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
