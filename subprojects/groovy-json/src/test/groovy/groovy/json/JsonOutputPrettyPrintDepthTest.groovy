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
 * {@code prettyPrint} indents each nesting level, so every enclosed line grows with the depth
 * around it and an unbounded document expands quadratically in its own nesting. It lexes rather
 * than parses, so an unbalanced run of opening brackets is indistinguishable from real nesting and
 * is bounded the same way.
 */
class JsonOutputPrettyPrintDepthTest {

    private static final int DEFAULT = BaseJsonParser.DEFAULT_MAX_NESTING_DEPTH
    private static final String PROPERTY = 'groovy.json.maxNestingDepth'

    private static String nested(int depth) {
        '[' * depth + ']' * depth
    }

    private static withMaxNestingDepth(Object value, Closure body) {
        String previous = System.getProperty(PROPERTY)
        if (value == null) System.clearProperty(PROPERTY) else System.setProperty(PROPERTY, value.toString())
        try {
            body()
        } finally {
            if (previous == null) System.clearProperty(PROPERTY) else System.setProperty(PROPERTY, previous)
        }
    }

    // GROOVY-12330
    @Test
    void testDocumentAtTheDefaultLimitStillPrints() {
        String pretty = JsonOutput.prettyPrint(nested(DEFAULT))
        assertEquals(DEFAULT, pretty.count('['))
        assertEquals(DEFAULT, pretty.count(']'))
    }

    // GROOVY-12330
    @Test
    void testDocumentPastTheDefaultLimitIsRejected() {
        def e = shouldFail(JsonException) { JsonOutput.prettyPrint(nested(DEFAULT + 1)) }
        assertTrue(e.message.contains("Maximum JSON nesting depth of ${DEFAULT} exceeded"), e.message)
    }

    // GROOVY-12330
    @Test
    void testUnbalancedOpeningRunIsRejectedRatherThanAmplified() {
        // 16KB of '[' used to expand into an OutOfMemoryError; the lexer cannot tell an unclosed
        // run from genuine nesting, so the depth bound is what stops it
        shouldFail(JsonException) { JsonOutput.prettyPrint('[' * 16384) }
        shouldFail(JsonException) { JsonOutput.prettyPrint('{' * 16384) }
    }

    // GROOVY-12330
    @Test
    void testDepthIsCountedPerNestingNotPerToken() {
        // siblings repeatedly enter and leave depth 2 -- only concurrent nesting counts
        String siblings = '[' + (['[1,2]'] * 500).join(',') + ']'
        String pretty = JsonOutput.prettyPrint(siblings)
        assertEquals(501, pretty.count('['))
    }

    // GROOVY-12330
    @Test
    void testLimitIsConfigurableBySystemProperty() {
        withMaxNestingDepth(10) {
            assertEquals(10, JsonOutput.prettyPrint(nested(10)).count('['))
            shouldFail(JsonException) { JsonOutput.prettyPrint(nested(11)) }
        }
    }

    // GROOVY-12330
    @Test
    void testCheckIsDisabledByNonPositiveLimit() {
        withMaxNestingDepth(0) {
            assertEquals(DEFAULT + 50, JsonOutput.prettyPrint(nested(DEFAULT + 50)).count('['))
        }
    }

    // GROOVY-12330
    @Test
    void testPrettyPrintAcceptsWhateverTheSlurperParses() {
        // the two bounds are the same knob, so the formatter never rejects a document the parser took
        withMaxNestingDepth(25) {
            String document = nested(25)
            new JsonSlurper().parseText(document)
            JsonOutput.prettyPrint(document)

            shouldFail(JsonException) { new JsonSlurper().parseText(nested(26)) }
            shouldFail(JsonException) { JsonOutput.prettyPrint(nested(26)) }
        }
    }

    // GROOVY-12330
    @Test
    void testOrdinaryDocumentIsFormattedUnchanged() {
        String pretty = JsonOutput.prettyPrint('{"a":[1,2,{"b":"c"}],"d":null}')
        assertEquals('''{
    "a": [
        1,
        2,
        {
            "b": "c"
        }
    ],
    "d": null
}'''.replace('\r\n', '\n'), pretty.replace('\r\n', '\n'))
    }
}
