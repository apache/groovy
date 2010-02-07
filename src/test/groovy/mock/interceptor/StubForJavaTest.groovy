package groovy.mock.interceptor

class StubForJavaTest extends GroovyTestCase {
    void testIterator() {
//        ProxyGenerator.INSTANCE.debug = true
        def iteratorContext = new StubFor(Iterator)
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        iteratorContext.ignore('dump')
        iteratorContext.ignore('getMetaClass')
        def iterator = iteratorContext.proxyDelegateInstance()
        def counter = new IteratorCounter()
        assert counter.count(iterator) == 1
        assert counter.count(iterator) == 1
        iteratorContext.verify(iterator)
    }

    void testString() {
        ProxyGenerator.INSTANCE.debug = false
        def stringContext = new StubFor(String)
        stringContext.demand.startsWith(2) { String arg -> arg == "wiz" }
        stringContext.demand.endsWith(2..2) { String arg -> arg == "foo" }
        def s = stringContext.proxyDelegateInstance()
        assert !s.endsWith("bar")
        assert s.endsWith("foo")
        assert !s.startsWith("bar")
        assert s.startsWith("wiz")
        stringContext.verify(s)
    }

}