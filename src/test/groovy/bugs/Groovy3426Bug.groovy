package groovy.bugs

class Groovy3426Bug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(Groovy3426Foo)
    }

    void tearDown() {
        registry.setMetaClass(Groovy3426Foo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def getFoo = { -> Groovy3426Foo.get() }

        MetaClass mc = new Groovy3426ClassProxyMetaClass(registry, Groovy3426Foo, originalMetaClass)
        registry.setMetaClass(Groovy3426Foo, mc)

        assert 'static' == Groovy3426Foo.get()
        assert 'static' == getFoo()

        registry.setMetaClass(Groovy3426Foo, originalMetaClass)

        assert 'foo' == Groovy3426Foo.get()
        assert 'foo' == getFoo()
    }

}

class Groovy3426Foo {
    static get() { 'foo' }
}

class Groovy3426ClassProxyMetaClass extends ProxyMetaClass {
    Groovy3426ClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }
    public Object invokeStaticMethod(final Object aClass, final String method, final Object[] arguments) {
        'static'
    }
}
