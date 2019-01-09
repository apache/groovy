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

import static AssertionTestUtil.*

/**
 * Tests rendering of individual values.
 */

class ValueRenderingTest extends GroovyTestCase {
    void testNullValue() {
        isRendered """
assert x
       |
       null
        """, {
            def x = null
            assert x
        }
    }

    void testCharValue() {
        isRendered """
assert x == null
       | |
       c false
        """, {
            def x = "c" as char
            assert x == null
        }
    }

    void testStringValue() {
        isRendered """
assert x == null
       | |
       | false
       'foo'
        """, {
            def x = "foo"
            assert x == null
        }
    }

    void testMultiLineStringValue() {
        isRendered """
assert null == x
            |  |
            |  'one\\ntwo\\rthree\\r\\nfour'
            false
        """, {
            def x = "one\ntwo\rthree\r\nfour"
            assert null == x
        }
    }

    void testPrimitiveArrayValue() {
        isRendered """
assert x == null
       | |
       | false
       [1, 2]
        """, {
            def x = [1, 2] as int[]
            assert x == null
        }
    }

    void testObjectArrayValue() {
        isRendered """
assert x == null
       | |
       | false
       ['one', 'two']
        """, {
            def x = ["one", "two"] as String[]
            assert x == null
        }
    }

    void testEmptyStringValue() {
        def x = new String()

        isRendered '''
assert x == null
       | |
       | false
       ''
        ''', {
            assert x == null
        }
    }

    void testEmptyStringBuilderValue() {
        def x = new StringBuilder()

        isRendered '''
assert x == null
       | |
       | false
       ""
        ''', {
            assert x == null
        }
    }

    void testEmptyStringBufferValue() {
        def x = new StringBuffer()

        isRendered '''
assert x == null
       | |
       | false
       ""
        ''', {
            assert x == null
        }
    }

    void testSingleLineToString() {
        isRendered """
assert x == null
       | |
       | false
       single line
        """, {
            def x = new SingleLineToString()
            assert x == null
        }
    }

    void testMultiLineToString() {
        isRendered """
assert x == null
       | |
       | false
       mul
       tiple
          lines
        """, {
            def x = new MultiLineToString()
            assert x == null
        }
    }

    void testNullToString() {
        def x = new NullToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() == null)
        """, {
            assert x == null
        }
    }

    void testEmptyToString() {
        def x = new EmptyToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() == \"\")
        """, {
            assert x == null
        }
    }

    void testThrowingToString() {
        def x = new ThrowingToString()

        isRendered """
assert x == null
       | |
       | false
       ${x.objectToString()} (toString() threw java.lang.UnsupportedOperationException)
        """, {
            assert x == null
        }
    }
}

@groovy.transform.PackageScope class SingleLineToString {
    String toString() {
        "single line"
    }
}

@groovy.transform.PackageScope class MultiLineToString {
    String toString() {
        "mul\ntiple\n   lines"
    }
}

@groovy.transform.PackageScope class NullToString {
    String objectToString() {
        super.toString()
    }

    String toString() { null }
}

@groovy.transform.PackageScope class EmptyToString {
    String objectToString() {
        super.toString()
    }

    String toString() { "" }
}

@groovy.transform.PackageScope class ThrowingToString {
    String objectToString() {
        super.toString()
    }

    String toString() {
        throw new UnsupportedOperationException()
    }
}
