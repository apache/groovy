/** 
 * @author Michael Baehr
 */
class UniqueTest extends GroovyTestCase {
    
	void testUnique() {
		def list = [-1, 0, 1, 1, 0, -1]
    assert list.unique() == [-1, 0, 1]
	}
	
	void testUniqueWithComparator() {
		def list = [-1, 0, 1, 1, 0, -1]
		def comparator = new ClosureComparator() {a,b -> Math.abs(a) <=> Math.abs(b)} 
    assert list.unique(comparator) == [-1, 0]
	}    
	
	// new functionality - see GROOVY-1236
	void testUniqueWithTwoParameterClosure() {
		def list = [-1, 0, 1, 1, 0, -1]
		def closure = {a,b -> Math.abs(a) <=> Math.abs(b)} 
    assert list.unique(closure) == [-1, 0]
	}   

	// new functionality - see GROOVY-1236	
	void testUniqueWithOneParameterClosure() {
		def list = [-1, 0, 1, 1, 0, -1]
		def closure = {a -> Math.abs(a)} 
    assert list.unique(closure) == [-1, 0]
	}   
		
}