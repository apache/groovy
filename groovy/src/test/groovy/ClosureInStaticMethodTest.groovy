package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureInStaticMethodTest extends GroovyTestCase {

    void testClosureInStaticMethod() {
        def closure = closureInStaticMethod()
        assertClosure(closure)    
    }

    void testMethodClosureInStaticMethod() {
        def closure = methodClosureInStaticMethod()
        assertClosure(closure)    
    }
    
    static def closureInStaticMethod() {
        return { println(it) }
    }

    static def methodClosureInStaticMethod() {
        System.out.&println
    }
    
    static def assertClosure(Closure block) {
        assert block != null
        block.call("hello!")
    }
    
    void testClosureInStaticMethodCallingStaticMethod() {
       assert doThing(1) == 10
       assert this.doThing(1) == 10
       assert ClosureInStaticMethodTest.doThing(1) == 10
    }
    
    
    static doThing(count) {
      def ret = count
      if (count > 2) return ret
      count.times {
        ret += doThing(count+it+1)
      }
      return ret
    }
}
