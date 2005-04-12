/**
 * A little performance test
 * @version $Revision$
 */
class BenchmarkBug extends GroovyTestCase {
    
    void testPerformance() {
		start = System.currentTimeMillis()
		
		total = 0
		size = 10000
		for (i in 0..size) {
		    total = total + callSomeMethod("hello", total)
		} 
		
		end = System.currentTimeMillis()
		
		time = end - start
		
		println "Performed ${size} iterations in ${time / 1000} seconds which is ${time / size} ms per iteration"
		
		assert total == size * 10 + 10
    }
    
    callSomeMethod(text, total) {
        return 10
    }
}