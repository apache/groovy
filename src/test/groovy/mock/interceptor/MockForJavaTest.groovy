package groovy.mock.interceptor

class MockForJavaTest extends GroovyTestCase {
    void testIterator() {
        def iteratorContext = new MockFor(Iterator)
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        def iterator = iteratorContext.proxyDelegateInstance()
        iteratorContext.demand.next() { "foo" }
        def iterator2 = iteratorContext.proxyDelegateInstance()

        assert new IteratorCounter().count(iterator2) == 2
        assert iterator2.next() == "foo"
        iteratorContext.verify(iterator2)

        assert new IteratorCounter().count(iterator) == 2
        iteratorContext.verify(iterator)

        iteratorContext = new MockFor(Iterator)
        iteratorContext.demand.hasNext(7..7) { true }
        iteratorContext.demand.hasNext() { false }
        def iterator3 = iteratorContext.proxyDelegateInstance()
        assert new IteratorCounter().count(iterator3) == 7
        iteratorContext.verify(iterator3)
    }

    void testString() {
        def stringContext = new MockFor(String)
        stringContext.demand.endsWith(2..2) { String arg -> arg == "foo" }
        def s = stringContext.proxyDelegateInstance()
        assert !s.endsWith("bar")
        assert s.endsWith("foo")
        stringContext.verify(s)
    }

}