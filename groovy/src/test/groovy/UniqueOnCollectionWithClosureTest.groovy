package groovy

/**
 * @author Michael Baehr
 * @author Paul King
 */
class UniqueOnCollectionWithClosureTest extends GroovyTestCase {

    void testUniqueOnIterator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a,b -> Math.abs(a) <=> Math.abs(b)}
    	def it = list.iterator().unique(closure)
    	assert it instanceof Iterator
        def result = it.toList()
        assert result == [-1, 0]
    }

    // GROOVY-3213
    void testUniqueWithTwoParameterClosureOnSet() {
        def set = [-1, 0, 1] as Set
        def closure = {a,b -> Math.abs(a) <=> Math.abs(b)}
        assert set.unique(closure).size() == 2
    }

    // GROOVY-1236
    void testUniqueWithTwoParameterClosure() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a,b -> Math.abs(a) <=> Math.abs(b)}
        assert list.unique(closure) == [-1, 0]
    }

    // GROOVY-1236
    void testUniqueWithOneParameterClosure() {
        def list = [-1, 0, 1, 1, 0, -1]
        def closure = {a -> Math.abs(a)}
        assert list.unique(closure) == [-1, 0]
    }

}