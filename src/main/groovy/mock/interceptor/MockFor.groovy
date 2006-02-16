package groovy.mock.interceptor

/**
    Facade over the Mocking details.
    A Mock's expectation is always sequence dependent and it's use always ends with a verify().
    See also StubFor.
*/

class MockFor {

    @Property MockProxyMetaClass proxy
    @Property Demand demand
    @Property expect

    MockFor(Class clazz) {
        proxy = MockProxyMetaClass.make(clazz)
        demand = new Demand()
        expect = new StrictExpectation(demand)
        proxy.interceptor = new MockInterceptor(expectation: expect)
    }

    void use(Closure closure) {
        proxy.use closure
        expect.verify()
    }

    void use(GroovyObject obj, Closure closure) {
        proxy.use obj, closure
        expect.verify()
    }
}