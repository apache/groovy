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
package org.codehaus.groovy.runtime

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class NullObjectTest {

    // GROOVY-5769
    @Test
    void testInit1() {
        shouldFail(RuntimeException) {
            new NullObject()
        }
    }

    // GROOVY-5769
    @Test
    void testInit2() {
        shouldFail(RuntimeException) {
            NullObject.newInstance()
        }
    }

    // GROOVY-5769
    @Test
    void testInit3() {
        shouldFail(NoSuchMethodException) {
            NullObject.getConstructor()
        }
    }

    @Test
    void testClone() {
        def nil = null
        shouldFail(NullPointerException) {
            nil.clone()
        }
    }

    @Test
    void testEquals() {
        def nil = null
        assert nil == nil
        assert nil == null
        assert null == nil
        assert null == null
        assert !nil.equals(0)
        assert !nil.equals('')
        assert nil.equals(null)
        assert nil.equals(NullObject.getNullObject())
    }

    @Test
    void testHashCode() {
        def nil = null
        shouldFail(NullPointerException) {
            nil.hashCode()
        }
    }

    @Test
    void testToString() {
        def nil = null
        assert nil.toString() == 'null'
    }

    // GROOVY-4487
    @Test
    void testGetClass() {
        def nil = null
        assert nil.class === nil.getClass()
        assert nil.class.simpleName == 'NullObject'
        assert nil.getClass().getSimpleName() == 'NullObject'
    }

    @Test
    void testGetProperty() {
        def nil = null
        shouldFail(NullPointerException) {
            nil.foo
        }
    }

    @Test
    void testSetProperty() {
        def nil = null
        shouldFail(NullPointerException) {
            nil.foo = 'bar'
        }
    }

    @Test
    void testInvokeMethod() {
        def nil = null
        shouldFail(NullPointerException) {
            nil.foo()
        }
    }

    //

    @Test
    void testAsBool() {
        def nil = null
        assert !nil
        assert !nil.asBoolean()
    }

    @Test
    void testAsType1() {
        def nil = null
        assert (nil as Number) === null
        assert (nil as String) === null
    }

    @Test
    void testAsType2() {
        def nil = null
        assert nil.asType(String) === null
        shouldFail(IllegalArgumentException) {
            NullObject.getNullObject().asType(int)
        }
    }

    // GROOVY-7861
    @Test @CompileStatic
    void testAsType3() {
        def nil = null
        assert nil.asType(String) === null
    }

    @Test
    void testIs() {
        def nil = null
        assert nil.is(null)
        assert !nil.is(' ')
        assert nil.is(NullObject.getNullObject())
    }

    @Test
    void testIterator() {
        def nil = null
        assert !nil.iterator().hasNext()
    }

    @Test
    void testPlus1() {
        def err = shouldFail(NullPointerException) {
            null+null
        }
        assert err.message == 'Cannot execute null+null'
    }

    @Test
    void testPlus2() {
        def err = shouldFail(NullPointerException) {
            null+1
        }
        assert err.message == 'Cannot execute null+1'
    }

    @Test
    void testPlus3() {
        assert (null + '!') == 'null!'
    }

    // GROOVY-11196
    @Test
    void testTap() {
        def nil = null
        nil = nil.tap {
            assert it === null
            assert it !instanceof NullObject
        }
        assert nil === null
        assert nil !instanceof NullObject
    }

    // GROOVY-4526, GROOVY-4985
    @Test
    void testWith() {
        def nil = null
        def ret = nil.with { it ->
            assert it === null
            assert it !instanceof NullObject
        }
        assert ret === null
        assert ret !instanceof NullObject

        ret = nil.with {
            assert it === null
            assert it !instanceof NullObject
            return 2
        }
        assert ret == 2
    }

    //--------------------------------------------------------------------------

    static class MyCategory {
        public static String toString(NullObject obj) {
            return ''
        }

        static boolean isNull(value) {
            value == null
        }

        static boolean isNull2(value) {
            null == value
        }
    }

    @Test
    void testCategory1() {
        def nil = null

        assert "a $nil b" == 'a null b'
        assert nil.toString() == 'null'
        assert nil + ' is a null value' == 'null is a null value'
        assert 'this is a null value ' + null == 'this is a null value null'

        use (MyCategory) {
            assert "a $nil b" == 'a  b'
            assert nil.toString() == ''
            assert nil + ' is a null value' == ' is a null value'
            assert 'this is a null value ' + null == 'this is a null value '
        }
    }

    @Test
    void testCategory2() {
        def nil = null
        use (MyCategory) {
            assert nil.isNull()
            assert nil.isNull2()
        }
    }

    // GROOVY-3803
    @Test
    void testMetaClass1() {
        def oldMC = NullObject.getMetaClass()
        try {
            NullObject.metaClass.hello = { -> 'Greeting from null' }
            assert null.hello() == 'Greeting from null'
        } finally {
            NullObject.setMetaClass(oldMC)
        }
    }

    // GROOVY-8875
    @Test
    void testMetaClass2() {
        def oldMC = NullObject.getMetaClass()
        try {
            NullObject.metaClass.toString = { -> 'bar' }
            assert ('foo' + null) == 'foobar'
        } finally {
            NullObject.setMetaClass(oldMC)
        }
    }

    @Test
    void testMetaClass3() {
        assert null.metaClass == null.getMetaClass()
        assert null.metaClass.adaptee === null.getMetaClass().getAdaptee()
    }
}
