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
package groovy.util

import org.apache.groovy.util.HiddenClassDefiner
import org.codehaus.groovy.runtime.ProxyGeneratorAdapter
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.*

class ProxyGeneratorAdapterTest {
    @Test
    void testShouldCreateProxy() {
        def map = ['toString': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj.toString() == 'HELLO'
    }

    @Test
    void testShouldCreateProxyWithArrayDelegate() {
        def adapter = new ProxyGeneratorAdapter([:], Map.Entry, [Map.Entry] as Class[], null, false, String[])
        assert adapter.proxyName() =~ /String_array\d+_groovyProxy/
    }

    @Test
    void testImplementSingleAbstractMethod() {
        def map = ['m': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == 'HELLO'
    }

    @Test
    void testImplementSingleAbstractMethodReturningVoid() {
        def map = ['bar': { println 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        obj.bar()
    }

    @Test
    void testImplementSingleAbstractMethodReturningVoidAndSharedVariable() {
        def x = null
        def map = ['bar': { x = 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        assert x == null
        obj.bar()
        assert x == 'HELLO'
    }

    @Test
    void testImplementMethodFromInterface() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, [FooInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

    @Test
    void testImplementMethodFromInterfaceUsingInterfaceAsSuperClass() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, FooInterface, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

    @Test
    void testImplementMethodFromInterfaceAndSuperClass() {
        def x = null
        def map = ['foo': { 'HELLO' }, 'bar': { x='WORLD'} ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, [FooInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        assert obj instanceof FooInterface
        assert x == null
        assert obj.foo() == 'HELLO'
        obj.bar()
        assert x == 'WORLD'
    }

    @Test
    void testImplementMethodFromInterfaceWithPrimitiveTypes() {
        def map = ['calc': { x -> x*2 } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, [OtherInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof OtherInterface
        assert obj.calc(3) == 6
    }

    @Test
    void testWildcardProxy() {
        def map = ['*': { '1' } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == '1'
    }

    @Test
    void testDelegatingProxy() {
        assertScript '''
        public abstract class A { abstract protected String doIt() }

        class B extends A {
           String doIt() { 'foo' }
        }
        def map = [ x : { int a, int b -> } ]
        def adapter = new org.codehaus.groovy.runtime.ProxyGeneratorAdapter(map, B, null, B.classLoader, false, B)
        def pxy = adapter.delegatingProxy(new B(), map)
        assert pxy.doIt() ==  'foo'
        '''
    }

    // GROOVY-5925
    @Test
    void testProxyForLongConstructor() {

        def map =  [nextInt: { x -> return 0 }]
        def gen = new ProxyGenerator()

        // Random(long) is special as the long param has a register length == 2
        def proxy = gen.instantiateAggregateFromBaseClass(map, Random)

        assert proxy.nextInt() == 0
    }

    @Test
    void testProxyForDoubleConstructor() {
        assertScript '''
        public class A {
            A() {}
            A(double d) {}

            def test() {}
        }

        def map = [ test : { 42 } ]
        def gen = new ProxyGenerator()

        // A(double) is special as the double param has a register length == 2
        def proxy = gen.instantiateAggregateFromBaseClass(map, A)

        assert proxy.test() ==  42
        '''
    }

    // GROOVY-7146
    @Test
    void testShouldNotThrowVerifyErrorBecauseOfStackSize() {
        assertScript '''
            interface DoStuff {
            }
            class Foo {
               void foo(double a, int b) {} // first a parameter that requires 2 slots, then one that requires only 1
            }

            def gp=new Foo() as DoStuff
            '''
    }

    static class ClassA {}
    static trait Trait1 { def method1() { 'Trait1 method' } }

    // GROOVY-7443
    @Test
    void testTraitFromDifferentClassloader() {
        def aWith1 = new ClassA().withTraits(Trait1)
        assert aWith1.method1() == 'Trait1 method'
        GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader)
        Class classB = gcl.parseClass('class ClassB {}')
        Class trait2 = gcl.parseClass('trait Trait2 { def method2() { "Trait2 method" } }')
        def bWith1 = classB.newInstance().withTraits(Trait1)
        assert bWith1.method1() == 'Trait1 method'
        def bWith2 = classB.newInstance().withTraits(trait2)
        assert bWith2.method2() == 'Trait2 method'
        def aWith2 = new ClassA().withTraits(trait2)
        assert aWith2.method2() == 'Trait2 method'
    }

    @Test
    void testGetTypeArgsRegisterLength() {
        def types = { list -> list as org.objectweb.asm.Type[] }
        def proxyGeneratorAdapter = new ProxyGeneratorAdapter([:], Object, [] as Class[], null, false, Object)

        assert 2 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.LONG_TYPE]))
        assert 2 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.DOUBLE_TYPE]))

        assert 1 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.BYTE_TYPE]))
        assert 1 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.CHAR_TYPE]))
        assert 1 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.INT_TYPE]))
        assert 1 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.FLOAT_TYPE]))

        assert 1 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([org.objectweb.asm.Type.BOOLEAN_TYPE]))

        assert 5 == proxyGeneratorAdapter.getTypeArgsRegisterLength(types([
                org.objectweb.asm.Type.LONG_TYPE,
                org.objectweb.asm.Type.LONG_TYPE,
                org.objectweb.asm.Type.INT_TYPE ] as org.objectweb.asm.Type[]))
    }

    abstract static class Foo {
        abstract String m()
    }

    abstract static class Bar {
        abstract void bar()
    }

    static interface FooInterface {
        String foo()
    }

    static interface OtherInterface {
        int calc(int x)
    }

    static interface UserMarker {} // user-defined marker; classloader is the test classloader

    // GROOVY-11999: building a proxy whose interface list mixes a bootstrap-loaded
    // interface (Runnable/Serializable) with a user-defined one used to NPE in
    // InnerLoader because the bootstrap classloader (null) was added to the
    // internalClassLoaders list and dereferenced during class definition.
    @Test
    void testProxyMixingBootstrapAndUserInterfaces() {
        def closure = { -> /* doCall */ }
        def closureMap = ['*': closure]
        def adapter = new ProxyGeneratorAdapter(
                closureMap,
                Object,
                [Runnable, UserMarker] as Class[],
                this.class.classLoader,
                false,
                null)
        def obj = adapter.proxy(closureMap, null)
        assert obj instanceof Runnable
        assert obj instanceof UserMarker
        obj.run() // does not throw
    }

    // GROOVY-11999: same scenario via the public ProxyGenerator entry point used
    // by the runtime intersection-cast path (IntersectionCastSupport.asType).
    @Test
    void testInstantiateAggregateMixingBootstrapAndUserInterfaces() {
        def calls = 0
        def proxy = ProxyGenerator.INSTANCE.instantiateAggregate(
                ['run': { -> calls++ }],
                [Runnable, java.io.Serializable, UserMarker] as List<Class>)
        assert proxy instanceof Runnable
        assert proxy instanceof java.io.Serializable
        assert proxy instanceof UserMarker
        proxy.run()
        proxy.run()
        assert calls == 2
    }

    // -------------------------------------------------------------------------
    // Hidden-class-specific tests (since Groovy 6.0 / JEP 371)
    // -------------------------------------------------------------------------

    /**
     * Concrete abstract superclasses only reference types visible from the
     * host loader, so the hidden nestmate path must succeed when enabled.
     */
    @Test
    void testProxyIsDefinedAsHiddenClass() {
        if (!HiddenClassDefiner.isEnabled()) return

        def map = ['bar': { }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        assertTrue(adapter.isProxyHidden(),
            'Concrete-super proxy must be a hidden class when hidden classes are enabled')
        assert adapter.proxy(map) instanceof Bar
    }

    /**
     * Interface aggregates (Object super + user interfaces, no typed delegate)
     * must stay <em>visible</em>: MockFor/StubFor re-wrap them and need a
     * nameable binary type for the {@code $delegate} field.
     */
    @Test
    void testInterfaceAggregateIsNotHidden() {
        if (!HiddenClassDefiner.isEnabled()) return

        def map = [:]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(
                map, Object, [Iterator] as Class[], this.class.classLoader, false, null)
        assertFalse(adapter.isProxyHidden(),
            'Interface aggregates must remain nameable for MockFor re-wrapping')
        def obj = adapter.proxy(map)
        assert obj instanceof Iterator
        assertFalse(obj.getClass().isHidden())
    }

    /**
     * A proxy defined as a hidden class must report {@link Class#isHidden()} as
     * {@code true} and must not be discoverable via {@code Class.forName()}.
     */
    @Test
    void testHiddenProxyIsNotDiscoverableByName() {
        if (!HiddenClassDefiner.isEnabled()) return

        def map = ['bar': { }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        if (!adapter.isProxyHidden()) return

        Class<?> proxyCls = adapter.proxy(map).getClass()
        assertTrue(proxyCls.isHidden(), 'Proxy class must report isHidden() == true')
        // Non-discoverability: the binary-name prefix (before '/') must not load
        String binaryPrefix = proxyCls.name.substring(0, proxyCls.name.indexOf('/'))
        assertThrows(ClassNotFoundException) {
            Class.forName(binaryPrefix)
        }
        assertThrows(ClassNotFoundException) {
            proxyCls.classLoader.loadClass(binaryPrefix)
        }
    }

    /**
     * MockFor-style re-wrap: interface aggregate (visible) then a delegating
     * proxy whose {@code $delegate} field names that class. Must not throw
     * during class definition (the original regression for hidden proxies).
     */
    @Test
    void testDelegatingProxyOverInterfaceAggregate() {
        def closures = [hasNext: { false }, next: { null }]
        def aggregateAdapter = new ProxyGeneratorAdapter(
                closures, Object, [Iterator] as Class[], this.class.classLoader, false, null)
        def aggregate = aggregateAdapter.proxy(closures)
        assert aggregate instanceof Iterator
        assertFalse(aggregate.getClass().isHidden())

        def wrapAdapter = new ProxyGeneratorAdapter(
                closures, Object, [Iterator] as Class[],
                aggregate.getClass().classLoader, false, aggregate.getClass())
        def wrapped = wrapAdapter.delegatingProxy(aggregate, closures)
        assert wrapped instanceof Iterator
        assert !wrapped.hasNext()
    }

    /**
     * A hidden proxy must implement the same interfaces and expose the same
     * method behaviour as a visible (fallback) proxy.
     */
    @Test
    void testHiddenProxyBehaviourIsIdenticalToVisibleProxy() {
        def x = null
        def map = ['bar': { x = 'HELLO_HIDDEN' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)

        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        assert x == null
        obj.bar()
        assert x == 'HELLO_HIDDEN'
        // Bar is loadable from this test's class loader → hidden path preferred
        if (HiddenClassDefiner.isEnabled()) {
            assertTrue(adapter.isProxyHidden())
            assertTrue(obj.getClass().isHidden())
        }
    }

    /**
     * Proxies that extend a user type must still work: the nest host must be
     * the user type (or another type sharing its ClassLoader), not a Groovy-core
     * class whose loader cannot see the user type. Covered end-to-end by
     * {@code testShouldNotThrowVerifyErrorBecauseOfStackSize} and
     * {@code testTraitFromDifferentClassloader}; this focuses on a local type.
     */
    @Test
    void testProxyOverUserSuperclassRemainsFunctional() {
        def called = false
        def map = ['bar': { called = true }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof Bar
        obj.bar()
        assert called
        if (HiddenClassDefiner.isEnabled()) {
            assertTrue(adapter.isProxyHidden())
            assertTrue(obj.getClass().isHidden())
            // Nest host is Bar (or its nest host), not a Groovy-core class alone
            assertEquals(Bar.nestHost, obj.getClass().nestHost)
        }
    }
}
