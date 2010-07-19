/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.powerassert

import static AssertionTestUtil.*

/**
 * Tests rendering of individual values.
 *
 * @author Peter Niederwieser
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
       foo
        """, {
            def x = "foo"
            assert x == null
        }
    }

    void testMultiLineStringValue() {
        isRendered """
assert null == x
            |  |
            |  one
            |  two
            |  three
            |  four
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
       [one, two]
        """, {
            def x = ["one", "two"] as String[]
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
        try {
            def cl = {assert x == "zzz"} 
            cl()
            fail("assertion should have failed but didn't")
        } catch (PowerAssertionError e) {
            def emsg = e.message
            if (!emsg.contains(x.objectToString()) || !emsg.contains("(toString() == null)")) {
                fail("assertion should have been rendered with message '${x.objectToString()} (toString() == null)'")
            }
        }
    }

    void testEmptyToString() {
        def x = new EmptyToString()
        try {
            def cl = {assert x == "zzz"} 
            cl()
            fail("assertion should have failed but didn't")
        } catch (PowerAssertionError e) {
            def emsg = e.message
            if (!emsg.contains(x.objectToString()) || !emsg.contains("(toString() == \"\")")) {
                fail("assertion should have been rendered with message '${x.objectToString()} (toString() == \"\")'")
            }
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

    void testRenderingEmptyString() {
        verifyEmptyStringRendering "java.lang.String",  {
            def val = new String() 
            assert val == "xxx"
        }
    }
    
    void testRenderingEmptyStringBuilder() {
        verifyEmptyStringRendering "java.lang.StringBuilder",  {
            def val = new StringBuilder() 
            assert val == "xxx"
        }
    }
    
    void testRenderingEmptyStringBuffer() {
        verifyEmptyStringRendering "java.lang.StringBuffer",  {
            def val = new StringBuffer() 
            assert val == "xxx"
        }
    }
    
    void verifyEmptyStringRendering(className, cl) {
        try {
            cl()
            fail("assertion should have failed but didn't")
        } catch (PowerAssertionError e) {
            def emsg = e.message
            if (emsg.contains("$className@") || !emsg.contains("\"\"")) {
                fail("($className) - assertion should have rendered empty string as \"\"")
            }
        }
    }
}

private class SingleLineToString {
    String toString() {
        "single line"
    }
}

private class MultiLineToString {
    String toString() {
        "mul\ntiple\n   lines"
    }
}

private class NullToString {
    String objectToString() {
        super.toString()
    }

    String toString() { null }
}

private class EmptyToString {
    String objectToString() {
        super.toString()
    }

    String toString() { "" }
}

private class ThrowingToString {
    String objectToString() {
        super.toString()
    }

    String toString() {
        throw new UnsupportedOperationException()
    }
}
