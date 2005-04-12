/**
 * @version $Revision$
 */
class Bytecode3Bug extends GroovyTestCase {
    
    count
         
    void testIncrementPropertyInclosure() {
        args = [1, 2, 3]
        m = [:]
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