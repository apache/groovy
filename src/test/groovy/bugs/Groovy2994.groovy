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
package bugs

import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-2994: String and GString agree on the result type of plus().
 */
final class Groovy2994 {

    @Test
    void testOperatorAndDirectCall() {
        assertScript '''
            def v = 'foo'
            def gs = "${v}"
            assert gs + 'bar' instanceof String
            assert gs.plus('bar') instanceof String
            assert 'bar' + gs instanceof String
            assert 'bar'.plus(gs) instanceof String
            assert gs + 'bar' == 'foobar'
            assert gs.plus('bar') == 'foobar'
            assert 'bar' + gs == 'barfoo'
            assert 'bar'.plus(gs) == 'barfoo'
            assert (gs + '') == 'foo'
            assert (gs + 'bar' + "${'baz'}") instanceof String
            assert gs + 'bar' + "${'baz'}" == 'foobarbaz'
            assert "${1}" + 2 instanceof String
            assert "${1}" + 2 == '12'
            assert (1 + "${2}") instanceof String
            assert (1 + "${2}") == '12'
        '''
    }

    @Test
    void testRenderingDoesNotMutateReceiver() {
        def buf = new StringBuilder('cat')
        def gs = "the ${buf}"
        def rendered = gs + '!'
        assertInstanceOf(String, rendered)
        assertEquals('the cat!', rendered)
        buf.append('s')
        assertEquals('the cat!', rendered)
        assertEquals('the cats', gs.toString())
        assertEquals(1, gs.values.length)
        assertTrue(gs.values[0].is(buf))
    }

    @Test
    void testGStringPlusGStringStaysStructured() {
        def left = "a ${1}"
        def right = "b ${2} c"
        def sum = left + right
        assertInstanceOf(GString, sum)
        assertEquals('a 1b 2 c', sum.toString())
        assertEquals([1, 2], sum.values as List)
        assertEquals(['a ', 'b ', ' c'], sum.strings as List)
    }

    @Test
    void testNullAndEmptyStringArgument() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            String appendNull(GString g) {
                g.plus((String) null)
            }

            @CompileStatic
            String appendEmpty(GString g) {
                g + ''
            }

            def g = "${'x'}"
            assert appendNull(g) == 'xnull'
            assert appendNull(g) instanceof String
            assert appendEmpty(g) == 'x'
            assert appendEmpty(g) instanceof String
            assert GString.EMPTY + 'z' == 'z'
            assert (GString.EMPTY + 'z') instanceof String
        '''
    }

    @Test
    void testCompileStaticAgreesOnString() {
        assertEquals('12', StaticCases.gStringThenString())
        assertInstanceOf(String, StaticCases.gStringThenString())
        assertEquals('12', StaticCases.stringThenGString())
        assertInstanceOf(String, StaticCases.stringThenGString())
        assertEquals('1null', StaticCases.appendNull())
        assertEquals('ab', StaticCases.chain())
        assertInstanceOf(String, StaticCases.chain())
    }

    @CompileStatic
    private static class StaticCases {
        static String gStringThenString() {
            "${1}" + '2'
        }

        static String stringThenGString() {
            '1' + "${2}"
        }

        static String appendNull() {
            "${'1'}".plus((String) null)
        }

        static String chain() {
            "${'a'}" + 'b'
        }
    }
}
