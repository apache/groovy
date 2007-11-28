package groovy

/**
 * @author Michael Baehr
 */
class UniqueOnCollectionWithClosureTest extends GroovyTestCase {

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