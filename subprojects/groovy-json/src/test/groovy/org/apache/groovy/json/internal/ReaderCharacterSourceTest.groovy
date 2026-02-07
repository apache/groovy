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
package org.apache.groovy.json.internal

import org.apache.groovy.json.internal.Exceptions.JsonInternalException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals

class ReaderCharacterSourceTest {

    public static final int QUOTE_CHAR = (int)'"'.charAt(0)
    public static final int BACKSLASH_CHAR = (int)'\\'.charAt(0)

    @Test
    void testFindNextChar() {
        def testCases = [
                [ input: '""', expected: '' ],
                [ input: '"word"', expected: 'word' ],
                [ input: '"\\u0026value"', expected: '\\u0026value' ],
                [ input: '"value\\u0026"', expected: 'value\\u0026' ],
                [ input: '"double\\"quote"', expected: 'double\\"quote' ],
                [ input: '"\\"\\"\\"\\""', expected: '\\"\\"\\"\\"' ],
                [ input: '"\\\\\\\\\\\\"', expected: '\\\\\\\\\\\\' ]
        ]

        testCases.each {
            boolean containsEscape = it.input.contains('\\')
            // Test all possible buffer sizes
            for (int i = 1; i < it.input.length() + 1; i++) {
                ReaderCharacterSource rcs = new ReaderCharacterSource(new StringReader(it.input), i)
                rcs.nextChar() // Read the first double quote as if JSON parsing

                String result = new String(rcs.findNextChar(QUOTE_CHAR, BACKSLASH_CHAR))

                assertEquals(it.expected, result, "Buffer size ${i}".toString())
                assertEquals(containsEscape, rcs.hadEscape(), "Expected escape character in ${it.input}, buffer size ${i}".toString())
            }
        }
    }

    @Test
    void testFindNextCharException() {
        shouldFail(JsonInternalException) {
            ReaderCharacterSource rcs = new ReaderCharacterSource(new StringReader('"missing end quote'))
            rcs.nextChar() // Read the first double quote as if JSON parsing
            rcs.findNextChar(QUOTE_CHAR, BACKSLASH_CHAR)
        }
    }

    @Test
    void testFindNextCharExceptionWithEscapedEnding() {
        shouldFail(JsonInternalException) {
            ReaderCharacterSource rcs = new ReaderCharacterSource(new StringReader('"missing end quote ending with escape \\'))
            rcs.nextChar() // Read the first double quote as if JSON parsing
            rcs.findNextChar(QUOTE_CHAR, BACKSLASH_CHAR)
        }
    }

    @Test
    void testFindNextCharExceptionWithEscapedQuote() {
        shouldFail(JsonInternalException) {
            ReaderCharacterSource rcs = new ReaderCharacterSource(new StringReader('"missing end quote with escaped quote \\"'))
            rcs.nextChar() // Read the first double quote as if JSON parsing
            rcs.findNextChar(QUOTE_CHAR, BACKSLASH_CHAR)
        }
    }
}
