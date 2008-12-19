package groovy

/** 
 * @author Michael Baehr
 * @author Paul King
 */
class UniqueOnCollectionWithComparatorTest extends GroovyTestCase {
    
    void testUniqueOnIterator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator() {a,b -> Math.abs(a) <=> Math.abs(b)}
    	def it = list.iterator().unique(comparator)
    	assert it instanceof Iterator
        def result = it.toList()
        assert result == [-1, 0]
    }

    void testUniqueWithComparatorList() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator() {a,b -> Math.abs(a) <=> Math.abs(b)}
        assert list.unique(comparator) == [-1, 0]
    }

    void testUniqueWithComparatorSet() {
        def set = [-1, 0, 1] as Set
        def comparator = new ClosureComparator() {a,b -> Math.abs(a) <=> Math.abs(b)}
        assert set.unique(comparator).size() == 2
    }
}