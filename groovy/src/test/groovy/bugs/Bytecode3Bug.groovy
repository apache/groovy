package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode3Bug extends GroovyTestCase {
    
    def count
         
    void testIncrementPropertyInclosure() {
        def args = [1, 2, 3]
        def m = [:]
        count = 0
        doLoop(args, m)
        assert count == 3
    }
    
    void doLoop(args, m) {
        args.each { 
            m.put(it, count++)
        }
    }
}