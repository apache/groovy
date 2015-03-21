/*
 * Copyright 2003-2015 the original author or authors.
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
package org.codehaus.groovy.runtime

class NullObjectTest extends GroovyTestCase {
    void testCallingMethod() {
        def foo = null
        shouldFail(NullPointerException) {
          println foo.bar
        }
    }
    
    void testtoStringMethod() {
        def foo = null
        assert foo.toString() == "null"
    }

    void testEquals() {
        def a = [1]
        assert a[3] == a[4]
        assert a[2].equals(a[4])
    }
    
    void testAsExpression() {
      assert null as String == null
    }
    
    void testIs(){
      assert null.is(null)
    }
    
    void testCategory() {
        def n = null

        assert "a $n b" == "a null b"
            assert n.toString() == "null"
            assert n + " is a null value" == "null is a null value"
            assert "this is a null value " + null == "this is a null value null"

            use (MyCategory) {
                assert "a $n b" == "a  b"
                assert n.toString() == ""
                assert n + " is a null value" == " is a null value"
                assert "this is a null value " + null == "this is a null value "
            }
        }

    void testClone() {
        def foo = null
        shouldFail(NullPointerException) {
            foo.clone()    
        }
    }
    
    void testEMC() {
        def oldMC = null.getMetaClass()
        NullObject.metaClass.hello = { -> "Greeting from null" }
        assert null.hello() == "Greeting from null"
        null.setMetaClass(oldMC)
    }

    void testNullPlusNull() {
        shouldFail(NullPointerException) {
            null+null
        }
    }

    void testNullWith() {
        def map = [ a:1, b:2 ]
        map.c.with { c ->
            assert c == null
        }
        def a = null.with {
            assert !(it instanceof NullObject)
            2
        }
        assert a == 2
    }
}

class MyCategory {
    public static String toString(NullObject obj) {
        return ""
    }
}

