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
package groovy.lang;

class ExpandoMetaClassCreationHandleTest extends GroovyTestCase {
    def registry = GroovySystem.metaClassRegistry
    MetaClass savedStringMeta
    MetaClass savedObjectMeta

    void setUp() {
        savedStringMeta = registry.getMetaClass(String)
        savedObjectMeta = registry.getMetaClass(Object)
        ExpandoMetaClass.enableGlobally()
    }

    void tearDown() {
        ExpandoMetaClass.disableGlobally()
        registry.setMetaClass(String, savedStringMeta)
        registry.setMetaClass(Object, savedObjectMeta)
    }

    void testInheritWithExistingMetaClass() {
        registry.removeMetaClass(String)
        registry.removeMetaClass(Object)

        String foo = "hello"
        assertEquals "HELLO", foo.toUpperCase()

        Object.metaClass.doStuff = { -> delegate.toString().toUpperCase() }

        assertEquals "HELLO", foo.doStuff()
    }

    void testInheritFromInterfaceHierarchy() {
        registry.removeMetaClass(IBar)
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(Test1)

        def metaClass = registry.getMetaClass(IBar)
        assertTrue(metaClass instanceof ExpandoMetaClass)

        metaClass.helloWorld = { -> "goodbye!" }

        def t = new Test1()
        assertEquals "goodbye!", t.helloWorld()
    }

    void testExpandoInterfaceInheritanceWithOverrideDGM() {
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(Test1)

        def metaClass = registry.getMetaClass(Foo)
        assertTrue(metaClass instanceof ExpandoMetaClass)

        def map = [:]
        metaClass.getAt = { Integer i -> map[i] }
        metaClass.putAt = { Integer i, val -> map[i] = val }

        def t = new Test1()
        //assertEquals 2, t.metaClass.getExpandoMethods().size()
        //assert t.metaClass.getExpandoMethods().find { it.name == 'putAt' }

        t[0] = "foo"

        assert map.size() == 1

        assertEquals "foo", t[0]
    }

    void testOverrideSetPropertyViaInterface() {
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(Test1)

        def metaClass = registry.getMetaClass(Foo)

        def testValue = null
        metaClass.setProperty = { String name, value ->
            def mp = delegate.metaClass.getMetaProperty(name)
            if (mp) {
                mp.setProperty(delegate, value)
            } else {
                testValue = value
            }
        }

        def t = new Test1()

        t.name = "Bob"
        assertEquals "Bob", t.name

        t.foo = "bar"
        assertEquals "bar", testValue
    }

    void testOverrideGetPropertyViaInterface() {
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(Test1)

        def metaClass = registry.getMetaClass(Foo)

        metaClass.getProperty = { String name ->
            def mp = delegate.metaClass.getMetaProperty(name)
            mp ? mp.getProperty(delegate) : "foo $name"
        }

        def t = new Test1()

        assertEquals "Fred", t.getProperty("name")
        assertEquals "Fred", t.name
        assertEquals "foo bar", t.getProperty("bar")
        assertEquals "foo bar", t.bar
    }

    void testOverrideInvokeMethodViaInterface() {
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(Object)
        registry.removeMetaClass(IBar)
        registry.removeMetaClass(Test1)

        def metaClass = registry.getMetaClass(Foo)

        metaClass.invokeMethod = { String name, args ->
            def mm = delegate.metaClass.getMetaMethod(name, args)
            mm ? mm.invoke(delegate, args) : "bar!!"
        }

        def t = new Test1()

        assertEquals "bar!!", t.doStuff()
        assertEquals "foo", t.invokeMe()
    }

    void testInterfaceMethodInheritance() {
        registry.removeMetaClass(List)
        registry.removeMetaClass(ArrayList)

        def metaClass = registry.getMetaClass(List)
        assertTrue(metaClass instanceof ExpandoMetaClass)

        metaClass.sizeDoubled = { -> delegate.size() * 2 }
        metaClass.isFull = { -> false }

        def list = new ArrayList()

        list << 1
        list << 2

        assertEquals 4, list.sizeDoubled()

        list = new ArrayList()

        assert !list.isFull()
        assert !list.full
    }

    void testExpandoCreationHandle() {
        def metaClass = registry.getMetaClass(URL)
        if (!(metaClass instanceof ExpandoMetaClass)) {
            registry.removeMetaClass(URL)
        }

        def url = new URL("http://grails.org")
        metaClass = registry.getMetaClass(url.getClass())
        assertTrue(metaClass instanceof ExpandoMetaClass)

        metaClass.toUpperString = { ->
            delegate.toString().toUpperCase()
        }

        assertEquals "http://grails.org", url.toString()
        assertEquals "HTTP://GRAILS.ORG", url.toUpperString()
    }

    void testExpandoInheritance() {
        registry.removeMetaClass(String)

        def metaClass = registry.getMetaClass(Object)
        if (!(metaClass instanceof ExpandoMetaClass)) {
            registry.removeMetaClass(Object)
            metaClass = registry.getMetaClass(Object)
        }

        metaClass.toFoo = { -> "foo" }

        def uri = new URI("http://bar.com")
        def s = "bar"

        assertEquals "foo", uri.toFoo()
        assertEquals "foo", s.toFoo()

        metaClass.toBar = { -> "bar" }

        assertEquals "bar", uri.toBar()
        assertEquals "bar", s.toBar()
    }

    void testAddMethodToChildThenParent() {
        registry.removeMetaClass(Test1)
        registry.removeMetaClass(EMCInheritTest)

        EMCInheritTest.metaClass.foo = { -> "hello!" }

        def emc = new EMCInheritTest()

        assertEquals "hello!", emc.foo()

        Test1.metaClass.foo = { -> "uck" }
        emc = new EMCInheritTest()
        // make sure original foo wasn't overridden
        assertEquals "hello!", emc.foo()
    }

    void testAddMethodMissingToChildThenParent() {
        registry.removeMetaClass(Test1)
        registry.removeMetaClass(EMCInheritTest)
        registry.removeMetaClass(Foo)
        registry.removeMetaClass(IBar)
        registry.removeMetaClass(Object)

        EMCInheritTest.metaClass.methodMissing = { String name, args -> "hello!" }

        def emc = new EMCInheritTest()

        assertEquals "hello!", emc.foo()

        Test1.metaClass.methodMissing = { String name, args -> "uck" }
        emc = new EMCInheritTest()
        // make sure original foo wasn't overridden
        assertEquals "hello!", emc.bar()
    }
}

interface IBar {}

interface Foo extends IBar {

}

class Test1 implements Foo {
    String name = "Fred"

    def invokeMe() { "foo" }
}

class EMCInheritTest extends Test1 {

}
