package groovy.util

import org.codehaus.groovy.runtime.ProxyGeneratorAdapter

class ProxyGeneratorAdapterTest extends GroovyTestCase {
    void testShouldCreateProxy() {
        def map = ['toString': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj.toString() == 'HELLO'
    }

    void testImplementSingleAbstractMethod() {
        def map = ['m': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == 'HELLO'

    }
    
    void testImplementSingleAbstractMethodReturningVoid() {
        def map = ['bar': { println 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        obj.bar()

    }

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

    void testImplementMethodFromInterface() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, [FooInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

    void testImplementMethodFromInterfaceUsingInterfaceAsSuperClass() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, FooInterface, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

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
    
    void testImplementMethodFromInterfaceWithPrimitiveTypes() {
        def map = ['calc': { x -> x*2 } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, [OtherInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof OtherInterface
        assert obj.calc(3) == 6
    }
    
    void testWildcardProxy() {
        def map = ['*': { '1' } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == '1'
    }

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
    void testProxyForLongConstructor() {

        def map =  [nextInt: { x -> return 0 }]
        def gen = new ProxyGenerator()

        // Random(long) is special as the long param has a register length == 2
        def proxy = gen.instantiateAggregateFromBaseClass(map, Random)

        assert proxy.nextInt() == 0
    }

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
}
