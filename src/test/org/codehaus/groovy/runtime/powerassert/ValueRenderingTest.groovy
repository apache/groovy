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
package org.codehaus.groovy.runtime.powerassert

import org.junit.Test

import static org.codehaus.groovy.runtime.powerassert.AssertionTestUtil.isRendered

/**
 * Tests rendering of individual values.
 */
final class ValueRenderingTest {

    @Test
    void testNullValue() {
        isRendered '''
assert x
       |
       null
        ''', { ->
            def x = null
            assert x
        }
    }

    @Test
    void testCharValue() {
        isRendered '''
assert x == null
       | |
       c false
        ''', { ->
            def x = "c" as char
            assert x == null
        }
    }

    @Test
    void testStringValue() {
        isRendered '''
assert x == null
       | |
       | false
       'foo'
        ''', { ->
            def x = "foo"
            assert x == null
        }
    }

    @Test
    void testMultiLineStringValue() {
        isRendered '''
assert null == x
            |  |
            |  'one\\ntwo\\rthree\\r\\nfour'
            false
        ''', { ->
            def x = "one\ntwo\rthree\r\nfour"
            assert null == x
        }
    }

    @Test
    void testPrimitiveArrayValue() {
        isRendered '''
assert x == null
       | |
       | false
       [1, 2]
        ''', { ->
            def x = [1, 2] as int[]
            assert x == null
        }
    }

    @Test
    void testObjectArrayValue() {
        isRendered '''
assert x == null
       | |
       | false
       ['one', 'two']
        ''', { ->
            def x = ["one", "two"] as String[]
            assert x == null
        }
    }

    @Test
    void testEmptyStringValue() {
        def x = new String()

        isRendered '''
assert x == null
       | |
       | false
       ''
        ''', { ->
            assert x == null
        }
    }

    @Test
    void testEmptyStringBuilderValue() {
        def x = new StringBuilder()

        isRendered '''
assert x == null
       | |
       | false
       ""
        ''', { ->
            assert x == null
        }
    }

    @Test
    void testEmptyStringBufferValue() {
        def x = new StringBuffer()

        isRendered '''
assert x == null
       | |
       | false
       ""
        ''', { ->
            assert x == null
        }
    }

    @Test
    void testSingleLineToString() {
        isRendered '''
assert x == null
       | |
       | false
       single line
        ''', { ->
            def x = new SingleLineToString()
            assert x == null
        }
    }

    @Test
    void testMultiLineToString() {
        isRendered '''
assert x == null
       | |
       | false
       mul
       tiple
          lines
        ''', { ->
            def x = new MultiLineToString()
            assert x == null
        }
    }

    @Test
    void testNullToString() {
        def x = new NullToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() == null)
        """, { ->
            assert x == null
        }
    }

    @Test
    void testEmptyToString() {
        def x = new EmptyToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() == \"\")
        """, { ->
            assert x == null
        }
    }

    @Test
    void testThrowingToString() {
        def x = new ThrowingToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() threw java.lang.UnsupportedOperationException)
        """, { ->
            assert x == null
        }
    }

    //--------------------------------------------------------------------------

    private static class SingleLineToString {
        @Override
        String toString() {
            'single line'
        }
    }

    private static class MultiLineToString {
        @Override
        String toString() {
            'mul\ntiple\n   lines'
        }
    }

    private static class NullToString {
        String objectToString() {
            super.toString()
        }

        @Override
        String toString() { null }
    }

    private static class EmptyToString {
        String objectToString() {
            super.toString()
        }

        @Override
        String toString() { '' }
    }

    private static class ThrowingToString {
        String objectToString() {
            super.toString()
        }

        @Override
        String toString() {
            throw new UnsupportedOperationException()
        }
    }
}
