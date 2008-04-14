package groovy.mock.interceptor

class StubForJavaTest extends GroovyTestCase {
    void testIterator() {
        def iteratorContext = new StubFor(Iterator)
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        def iterator = iteratorContext.proxyDelegateInstance()
        def counter = new IteratorCounter()
        assert counter.count(iterator) == 1
        assert counter.count(iterator) == 1
    }

    void testString() {
        def iteratorContext = new StubFor(String)
        iteratorContext.demand.startsWith(2..2) { String arg -> arg == "wiz" }
        iteratorContext.demand.endsWith(2..2) { String arg -> arg == "foo" }
        def s = iteratorContext.proxyDelegateInstance()
        assert !s.endsWith("bar")
        assert s.endsWith("foo")
        assert !s.startsWith("bar")
        assert s.startsWith("wiz")
        iteratorContext.verify(s)
    }

}