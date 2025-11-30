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

import org.codehaus.groovy.reflection.ClassInfo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

final class ExpandoMetaClassCreationHandleTest {

    @BeforeAll
    static void setUp() {
        ExpandoMetaClass.enableGlobally()
    }

    @AfterAll
    static void tearDown() {
        ExpandoMetaClass.disableGlobally()

        ClassInfo.onAllClassInfo { info ->
            if (info.getMetaClassForClass()) {
                info.lock()
                try {
                    info.setStrongMetaClass(null)
                } finally {
                    info.unlock()
                }
            }
        }
    }

    private MetaClass expand(Class type) {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry()
        MetaClass metaClass = registry.getMetaClass(type)
        assert metaClass instanceof ExpandoMetaClass
        return metaClass
    }

    private void reset(Class<?>... types) {
        types.each(GroovySystem.getMetaClassRegistry().&removeMetaClass)
    }

    //--------------------------------------------------------------------------

    @Test
    void testExpandoCreationHandle() {
        reset(URL)

        expand(URL).toStringUC = { -> delegate.toString().toUpperCase() }

        def url = new URL('http://grails.org')
        assert url.toString() == 'http://grails.org'
        assert url.toStringUC() == 'HTTP://GRAILS.ORG'
    }

    @Test
    void testInheritFromSuperClass() {
        reset(Object, String)

        String string = 'hello'
        assert string.toUpperCase() == 'HELLO'

        expand(Object).doStuff = { -> delegate.toString().toUpperCase() }

        assert string.doStuff() == 'HELLO'
    }

    @Test
    void testInheritFromSuperClass2() {
        reset(Object, String, URI)

        expand(Object).toFoo = { -> 'foo' }

        def uri = new URI('http://bar.com')
        def s = 'bar'

        assert uri.toFoo() == 'foo'
        assert s.toFoo() == 'foo'

        expand(Object).toBar = { -> 'bar' }

        assert uri.toBar() == 'bar'
        assert s.toBar() == 'bar'
    }

    @Test
    void testInheritFromSuperInterface() {
        reset(Bar, Foo, Tester)

        expand(Bar).helloWorld = { -> 'goodbye!' }

        assert new Tester().helloWorld() == 'goodbye!'
    }

    @Test
    void testOverrideGetAndPutAtViaInterface() {
        reset(Bar, Foo, Tester)

        assert metaClass instanceof ExpandoMetaClass

        def map = [:]
        MetaClass metaClass = expand(Foo)
        metaClass.getAt = { Integer idx -> map[idx] }
        metaClass.putAt = { Integer idx, val -> map[idx] = val }

        def t = new Tester()
        //assert t.getMetaClass().getExpandoMethods().size() == 2
        //assert t.getMetaClass().getExpandoMethods().find { it.name == 'putAt' }

        t[0] = 'foo'

        assert map.size() == 1

        assert t[0] == 'foo'
    }

    @Test
    void testOverrideSetPropertyViaInterface() {
        reset(Bar, Foo, Tester)

        def testValue = null
        expand(Foo).setProperty = { String name, value ->
            def mp = delegate.getMetaClass().getMetaProperty(name)
            if (mp) {
                mp.setProperty(delegate, value)
            } else {
                testValue = value
            }
        }

        def t = new Tester()

        t.name = 'Bob'
        assert t.name == 'Bob'

        t.xxxx = 'foo bar'
        assert testValue == 'foo bar'
    }

    @Test
    void testOverrideGetPropertyViaInterface() {
        reset(Bar, Foo, Tester)

        expand(Foo).getProperty = { String name ->
            def mp = delegate.getMetaClass().getMetaProperty(name)
            mp ? mp.getProperty(delegate) : "fizz $name"
        }

        def t = new Tester()

        assert t.getProperty('name') == 'Fred'
        assert t.name == 'Fred'
        assert t.getProperty('buzz') == 'fizz buzz'
        assert t.buzz == 'fizz buzz'
    }

    @Test
    void testOverrideInvokeMethodViaInterface() {
        reset(Bar, Foo, Tester)

        expand(Foo).invokeMethod = { String name, args ->
            def mm = delegate.metaClass.getMetaMethod(name, args)
            mm ? mm.invoke(delegate, args) : 'bar!'
        }

        def t = new Tester()

        assert t.invokeMe() == 'foo'
        assert t.whatever() == 'bar!'
    }

    // GROOVY-3873
    @Test
    void testOverrideMethodMissingViaInterface() {
        reset(List, ArrayList)

        expand(List).methodMissing = { String name, args ->
            true
        }

        def list = new ArrayList()

        assert list.noSuchMethod()
    }

    @Test
    void testInterfaceMethodInheritance() {
        reset(List, ArrayList)

        expand(List).with {
            sizeDoubled = { -> delegate.size() * 2 }
            isFull = { -> false }
        }

        def list = new ArrayList()
        list << 1
        list << 2

        assert list.sizeDoubled() == 4
        assert !list.isFull()
      //assert !list.full -- tries [1.full,2.full]
    }

    @Test
    void testAddMethodToChildThenParent() {
        reset(Bar, Foo, Tester, EMCInheritTest)

        expand(EMCInheritTest).foo = { -> 'hello!' }

        def emc = new EMCInheritTest()

        assert emc.foo() == 'hello!'

        expand(Tester).foo = { -> 'goodbye!' }

        emc = new EMCInheritTest()

        assert emc.foo() == 'hello!' : 'original foo was replaced'
    }

    @Test
    void testAddMethodMissingToChildThenParent() {
        reset(Bar, Foo, Tester, EMCInheritTest)

        expand(EMCInheritTest).methodMissing = { String name, args -> 'hello!' }

        def emc = new EMCInheritTest()

        assert emc.foo() == 'hello!'

        expand(Tester).methodMissing = { String name, args -> 'goodbye!' }

        emc = new EMCInheritTest()

        assert emc.bar() == 'hello!' : 'original methodMissing was replaced'
    }

    //--------------------------------------------------------------------------

    interface Bar {
    }

    interface Foo extends Bar {
    }

    static class Tester implements Foo {
        String name = 'Fred'
        def invokeMe() { 'foo' }
    }

    static class EMCInheritTest extends Tester {
    }
}
