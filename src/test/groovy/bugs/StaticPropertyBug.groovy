package groovy.bugs

class StaticPropertyBug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(StaticPropertyFoo)
    }

    void tearDown() {
        registry.setMetaClass(StaticPropertyFoo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        def getFoo = {-> StaticPropertyFoo.bar }

        assert 'foo' == StaticPropertyFoo.bar
        assert 'foo' == getFoo()

        MetaClass mc = new StaticPropertyClassProxyMetaClass(registry, StaticPropertyFoo, originalMetaClass)
        registry.setMetaClass(StaticPropertyFoo, mc)

        assert 'static' == StaticPropertyFoo.bar
        assert 'static' == getFoo()
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def getFoo = {-> StaticPropertyFoo.bar }

        MetaClass mc = new StaticPropertyClassProxyMetaClass(registry, StaticPropertyFoo, originalMetaClass)
        registry.setMetaClass(StaticPropertyFoo, mc)

        assert 'static' == StaticPropertyFoo.bar
        assert 'static' == getFoo()

        registry.setMetaClass(StaticPropertyFoo, originalMetaClass)

        assert 'foo' == StaticPropertyFoo.bar
        assert 'foo' == getFoo()
    }

}

class StaticPropertyFoo {
    static bar = 'foo'
}

class StaticPropertyClassProxyMetaClass extends ProxyMetaClass {
    StaticPropertyClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }

    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        'static'
    }
}
