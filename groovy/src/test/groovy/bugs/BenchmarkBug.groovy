package groovy.bugs

/**
 * A little performance test
 * @version $Revision$
 */
class BenchmarkBug extends GroovyTestCase {
    
    void testPerformance() {
        def start = System.currentTimeMillis()

        def total = 0
        def size = 10000
        for (i in 0..size) {
            total = total + callSomeMethod("hello", total)
        }

        def end = System.currentTimeMillis()

        def time = end - start

        println "Performed ${size} iterations in ${time / 1000} seconds which is ${time / size} ms per iteration"

        // TODO: parser bug
        // assert total == size * 10 + 10
        assert total == 100010
    }
    
    def callSomeMethod(text, total) {
        return 10
    }
}