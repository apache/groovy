package groovy

/** 
 * @author Michael Baehr
 */
class UniqueOnCollectionWithComparatorTest extends GroovyTestCase {
    
    void testUniqueWithComparator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator() {a,b -> Math.abs(a) <=> Math.abs(b)}
        assert list.unique(comparator) == [-1, 0]
    }
}