package groovy.bugs

class Groovy3424Bug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(Groovy3424Foo)
    }

    void tearDown() {
        registry.setMetaClass(Groovy3424Foo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        def newFoo = { -> new Groovy3424Foo() }

        assert new Groovy3424Foo() instanceof Groovy3424Foo
        assert newFoo() instanceof Groovy3424Foo

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo()
        assert 'constructor' == newFoo()
    }

    void testExpandoCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        Groovy3424Foo.metaClass.constructor << { String test -> 'foo' }

        def newFoo = { -> new Groovy3424Foo('test') }

        assert 'foo' == new Groovy3424Foo('test')
        assert 'foo' == newFoo()

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo('test')
        assert 'constructor' == newFoo()
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def newFoo = { -> new Groovy3424Foo() }

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo()
        assert 'constructor' == newFoo()

        registry.setMetaClass(Groovy3424Foo, originalMetaClass)

        assert new Groovy3424Foo() instanceof Groovy3424Foo
        assert newFoo() instanceof Groovy3424Foo
    }

}

class Groovy3424Foo {}

class Groovy3424ClassProxyMetaClass extends ProxyMetaClass {
    Groovy3424ClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }
    public Object invokeConstructor(final Object[] arguments) {
        'constructor'
    }
}
