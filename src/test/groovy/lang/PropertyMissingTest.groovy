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

/**
 * Tests the behaviour of the propertyMissing functionality of Groovy
 *
 * @since 1.5
 */
class PropertyMissingTest extends GroovyTestCase {


    void testPropertyMissingWithMethods() {
        def t = new PMTest1()

        assertEquals "bar", t.foo
        t.foo = "changed"
        assertEquals "changed", t.foo

        assertNull t.bar
        t.bar = "keepme"

        assertEquals "keepme", t.bar
    }

    void testPropertyMissingViaMetaClass() {
        def store = [:]
        PMTest2.metaClass.propertyMissing = { String name ->
            store.name
        }
        PMTest2.metaClass.propertyMissing = { String name, value ->
            store.name = value
        }

        def t = new PMTest2()

        assertEquals "bar", t.foo
        t.foo = "changed"
        assertEquals "changed", t.foo
        assertNull t.bar
        t.bar = "keepme"

        assertEquals "keepme", t.bar

    }

    void testStaticPropertyMissingViaMetaClass() {

        shouldFail(MissingPropertyException) {
            PMTest1.SOME_PROP
        }

        PMTest1.metaClass.'static'.propertyMissing = { String name ->
            name
        }

        assertEquals "SOME_PROP", PMTest1.SOME_PROP
        assertEquals "FOO", PMTest1.FOO
    }

    // GROOVY-7723
    void testPropertyMissingSetterWithNoGetter() {
        def t = new PMTest3()

        assert t.foo == 'bar'

        shouldFail(MissingPropertyException) {
            t.notfound
        }

        assert t.foo == 'bar'

        t.notfound = 'baz'
        assert t.foo == 'notfound-baz'

        t.metaClass.propertyMissing = { String name ->
            "get-${foo}"
        }
        assert t.notfound == 'get-notfound-baz'

    }

}

class PMTest1 {
    def store = [:]
    String foo = "bar"

    String propertyMissing(String name) {
        store.name
    }

    void propertyMissing(String name, value) {
        store.name = value
    }
}

class PMTest2 {
    String foo = "bar"
}

class PMTest3 {
    String foo = 'bar'

    void propertyMissing(String name, value) {
        foo = "${name}-${value}"
    }
}