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
package groovy.lang

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the behaviour of the propertyMissing functionality of Groovy
 */
final class PropertyMissingTest {

    @Test
    void testPropertyMissingWithMethods() {
        assertScript '''
            class PMTest1 {
                String foo = 'bar'
                private Map<String,?> store = [:]
                String propertyMissing(String name) {
                    store.name
                }
                void propertyMissing(String name, value) {
                    store.name = value
                }
            }

            def t = new PMTest1()
            assert t.foo == 'bar'
            t.foo = 'changed'
            assert t.foo == 'changed'
            assert t.bar == null
            t.bar = 'keepme'
            assert t.bar == 'keepme'
        '''
    }

    @Test
    void testPropertyMissingViaMetaClass() {
        assertScript '''
            class PMTest2 {
                String foo = 'bar'
            }

            def store = [:]
            PMTest2.metaClass.propertyMissing = { String name ->
                store.name
            }
            PMTest2.metaClass.propertyMissing = { String name, value ->
                store.name = value
            }

            def t = new PMTest2()
            assert t.foo == 'bar'
            t.foo = 'changed'
            assert t.foo == 'changed'
            assert t.bar == null
            t.bar = 'keepme'
            assert t.bar == 'keepme'
        '''
    }

    @Test
    void testStaticPropertyMissingViaMetaClass() {
        assertScript '''
            class PMTest3 {
            }

            groovy.test.GroovyAssert.shouldFail(MissingPropertyException) {
                PMTest3.SOME_PROP
            }
            PMTest3.metaClass.'static'.propertyMissing = { String name ->
                name
            }
            assert PMTest3.SOME_PROP == 'SOME_PROP'
            assert PMTest3.FOO == 'FOO'
        '''
    }

    // GROOVY-7723
    @Test
    void testPropertyMissingSetterWithNoGetter() {
        assertScript '''
            class PMTest4 {
                String foo = 'bar'

                void propertyMissing(String name, value) {
                    foo = "${name}-${value}"
                }
            }

            def t = new PMTest4()
            assert t.foo == 'bar'
            groovy.test.GroovyAssert.shouldFail(MissingPropertyException) {
                t.notfound
            }
            assert t.foo == 'bar'
            t.notfound = 'baz'
            assert t.foo == 'notfound-baz'
            t.metaClass.propertyMissing = { String name ->
                "get-${foo}"
            }
            assert t.notfound == 'get-notfound-baz'
        '''
    }

    @Test
    void testClassDotProperty() {
        def mpe = shouldFail MissingPropertyException, '''
            Integer.xxx
        '''
        assert mpe.message == 'No such property: xxx for class: java.lang.Integer'
    }

    // GROOVY-10188
    @Test
    void testNullDotProperty1() {
        // strange looking parameter in Closure to force a particular edge case
        def npe = shouldFail NullPointerException, '''
            { Integer -> Integer.xxx }()
        '''
        assert npe =~ /Cannot get property 'xxx' on null object/
    }

    @Test
    void testNullDotProperty2() {
        def npe = shouldFail NullPointerException, '''
            foo = null
            foo.bar
        '''
        assert npe =~ /Cannot get property 'bar' on null object/
    }

    // GROOVY-12704
    @Test
    void testNullDotProperty3() {
        def npe = shouldFail NullPointerException, '''
            foo = null
            bar = 'baz'
            foo."$bar"
        '''
        assert npe =~ /Cannot get property 'baz' on null object/
    }

    @Test
    void testNullDotProperty4() {
        def npe = shouldFail NullPointerException, '''
            foo = null
            foo.bar = null
        '''
        assert npe =~ /Cannot set property 'bar' on null object/
    }

    // GROOVY-12704
    @Test
    void testNullDotProperty5() {
        def npe = shouldFail NullPointerException, '''
            foo = null
            bar = 'baz'
            foo."$bar" = null
        '''
        assert npe =~ /Cannot set property 'baz' on null object/
    }
}
