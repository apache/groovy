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

import org.apache.groovy.json.internal.BaseJsonParser
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A number token past {@code maxNumberLength} is rejected with a {@link JsonException} on every
 * parser variant, rather than being converted by a superlinear {@code BigInteger}/{@code BigDecimal}
 * conversion. The overlay parsers defer conversion to first access, so the check has to reject the
 * document at parse time, not when the value is later read.
 */
class JsonSlurperMaxNumberLengthTest {

    private static final int DEFAULT = BaseJsonParser.DEFAULT_MAX_NUMBER_LENGTH

    private static String digits(int n) {
        '9' * n
    }

    /**
     * The CHARACTER_SOURCE parser re-wraps any exception escaping its array/object decode in a
     * generic "Unexpected issue" JsonException, so the cap's own message can arrive as a cause.
     * The same is true of the existing nesting-depth cap. Look through the chain rather than
     * pinning which layer carries the text.
     */
    private static boolean reports(Throwable t, String text) {
        for (Throwable c = t; c != null; c = c.cause) {
            if (c.message?.contains(text)) return true
            if (c.cause === c) break
        }
        false
    }

    // GROOVY-12329
    @Test
    void testDefaultCapMatchesJacksonsDefault() {
        assertEquals(1000, DEFAULT)
        assertEquals(DEFAULT, new JsonSlurper().maxNumberLength)
        assertEquals(DEFAULT, new JsonSlurperClassic().maxNumberLength)
    }

    // GROOVY-12329
    @Test
    void testNumberAtTheLimitStillParses() {
        JsonParserType.values().each { type ->
            def parsed = new JsonSlurper(type: type).parseText("[${digits(DEFAULT)}]")
            assertEquals(DEFAULT, parsed[0].toString().length(), "type ${type}".toString())
        }
        assertEquals(DEFAULT, new JsonSlurperClassic().parseText("[${digits(DEFAULT)}]")[0].toString().length())
    }

    // GROOVY-12329
    @Test
    void testOverlongIntegerRejectedOnEveryParserType() {
        JsonParserType.values().each { type ->
            def e = shouldFail(JsonException) {
                // toString() would realise a deferred overlay value; the parse itself must fail
                new JsonSlurper(type: type).parseText("[${digits(DEFAULT + 1)}]")
            }
            assertTrue(reports(e, 'exceeds the maximum'), "type ${type}: ${e.message}".toString())
        }
    }

    // GROOVY-12329
    @Test
    void testOverlongDecimalRejectedOnEveryParserType() {
        // BigDecimal conversion is superlinear for the same reason BigInteger is
        JsonParserType.values().each { type ->
            shouldFail(JsonException) {
                new JsonSlurper(type: type).parseText("[1.${digits(DEFAULT + 1)}]")
            }
        }
    }

    // GROOVY-12329
    @Test
    void testOverlongNumberRejectedByClassicParser() {
        def e = shouldFail(JsonException) {
            new JsonSlurperClassic().parseText("[${digits(DEFAULT + 1)}]")
        }
        assertTrue(reports(e, 'exceeds the maximum'), e.message)
    }

    // GROOVY-12329
    @Test
    void testLimitIsConfigurablePerInstance() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper(type: type).setMaxNumberLength(10)
            shouldFail(JsonException) { slurper.parseText("[${digits(11)}]") }
            assertEquals(10, slurper.parseText("[${digits(10)}]")[0].toString().length())
        }

        def classic = new JsonSlurperClassic()
        classic.maxNumberLength = 10
        shouldFail(JsonException) { classic.parseText("[${digits(11)}]") }
        assertEquals(10, classic.parseText("[${digits(10)}]")[0].toString().length())
    }

    // GROOVY-12329
    @Test
    void testCheckIsDisabledByNonPositiveLimit() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper(type: type).setMaxNumberLength(0)
            assertEquals(DEFAULT + 50, slurper.parseText("[${digits(DEFAULT + 50)}]")[0].toString().length(),
                    "type ${type}".toString())
        }

        def classic = new JsonSlurperClassic()
        classic.maxNumberLength = -1
        assertEquals(DEFAULT + 50, classic.parseText("[${digits(DEFAULT + 50)}]")[0].toString().length())
    }

    // GROOVY-12329
    @Test
    void testOrdinaryNumbersAreUnaffected() {
        JsonParserType.values().each { type ->
            def parsed = new JsonSlurper(type: type).parseText('{"a":1,"b":-2.5,"c":6.02e23,"d":9223372036854775808}')
            assertEquals(1, parsed.a as int, "type ${type}".toString())
            assertEquals(-2.5, parsed.b as double, 0.0, "type ${type}".toString())
            assertEquals(6.02e23, parsed.c as double, 0.0, "type ${type}".toString())
            assertEquals(new BigInteger('9223372036854775808'), parsed.d as BigInteger, "type ${type}".toString())
        }
    }
}
