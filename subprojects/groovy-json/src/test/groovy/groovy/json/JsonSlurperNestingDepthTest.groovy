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
import static org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Verifies that {@link JsonSlurper} (all parser types) and {@link JsonSlurperClassic} bound the
 * nesting depth of arrays/objects, turning a small but deeply-nested document into a clean
 * {@link JsonException} rather than a {@link StackOverflowError}.
 */
class JsonSlurperNestingDepthTest {

    private static String nestedArray(int depth) {
        '[' * depth + ']' * depth
    }

    private static String nestedObject(int depth) {
        '{"a":' * depth + '1' + '}' * depth
    }

    private static final String PROP = 'groovy.json.maxNestingDepth'

    @Test
    void testDefaultMaxNestingDepth() {
        assertEquals(1000, BaseJsonParser.DEFAULT_MAX_NESTING_DEPTH)
        // Isolate from any ambient value of the system property so the default-construction
        // assertion is deterministic regardless of how the build JVM is configured.
        def saved = System.getProperty(PROP)
        System.clearProperty(PROP)
        try {
            assertEquals(BaseJsonParser.DEFAULT_MAX_NESTING_DEPTH, new JsonSlurper().maxNestingDepth)
            assertEquals(BaseJsonParser.DEFAULT_MAX_NESTING_DEPTH, new JsonSlurperClassic().maxNestingDepth)
        } finally {
            if (saved == null) System.clearProperty(PROP) else System.setProperty(PROP, saved)
        }
    }

    @Test
    void testSystemPropertyHonored() {
        def saved = System.getProperty(PROP)
        System.setProperty(PROP, '50')
        try {
            // Both slurpers must pick up the global override at construction time.
            assertEquals(50, new JsonSlurper().maxNestingDepth)
            assertEquals(50, new JsonSlurperClassic().maxNestingDepth)
            assertNotNull(new JsonSlurper().parseText(nestedArray(50)))
            shouldFail(JsonException) {
                new JsonSlurper().parseText(nestedArray(51))
            }
        } finally {
            if (saved == null) System.clearProperty(PROP) else System.setProperty(PROP, saved)
        }
    }

    @Test
    void testAllParserTypesRejectDeeplyNestedArrays() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type).setMaxNestingDepth(50)
            assertNotNull(slurper.parseText(nestedArray(50)), "depth 50 should parse for $type")
            shouldFail(JsonException) {
                slurper.parseText(nestedArray(51))
            }
        }
    }

    @Test
    void testAllParserTypesRejectDeeplyNestedObjects() {
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type).setMaxNestingDepth(50)
            assertNotNull(slurper.parseText(nestedObject(50)), "depth 50 should parse for $type")
            shouldFail(JsonException) {
                slurper.parseText(nestedObject(51))
            }
        }
    }

    @Test
    void testHugeDepthThrowsJsonExceptionNotStackOverflow() {
        // With the default cap, a pathological document fails fast with JsonException and never
        // reaches a StackOverflowError.
        JsonParserType.values().each { type ->
            def slurper = new JsonSlurper().setType(type)
            shouldFail(JsonException) {
                slurper.parseText(nestedArray(100000))
            }
        }
    }

    @Test
    void testCheckCanBeDisabled() {
        // A value <= 0 disables the bound; a depth just above the default must then parse.
        // Use the minimal proof (DEFAULT + 1) since with the guard off this is real recursion
        // and a larger value risks a StackOverflowError on JVMs with a small thread stack.
        def slurper = new JsonSlurper().setMaxNestingDepth(0)
        assertNotNull(slurper.parseText(nestedArray(BaseJsonParser.DEFAULT_MAX_NESTING_DEPTH + 1)))
    }

    @Test
    void testClassicRejectsDeeplyNested() {
        def slurper = new JsonSlurperClassic()
        slurper.maxNestingDepth = 50
        assertNotNull(slurper.parseText(nestedArray(50)))
        assertNotNull(slurper.parseText(nestedObject(50)))
        shouldFail(JsonException) {
            slurper.parseText(nestedArray(51))
        }
        shouldFail(JsonException) {
            slurper.parseText(nestedObject(51))
        }
    }

    @Test
    void testClassicHugeDepthThrowsJsonExceptionNotStackOverflow() {
        def slurper = new JsonSlurperClassic()
        shouldFail(JsonException) {
            slurper.parseText(nestedArray(100000))
        }
    }
}
