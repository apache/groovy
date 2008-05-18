package groovy.bugs

import org.codehaus.groovy.runtime.MethodClosure

class MethodClosureTest extends GroovyTestCase {

    def aa(x) {
        x
    }
    
    static bb(it) { it}

    void testMethodClosure() {
        Class[] c1 = [ Exception.class, Throwable.class ]
        Class[] c2 = [ IllegalStateException.class ]

        def cl = this.&aa

        assert cl instanceof Closure
        assert cl instanceof MethodClosure

        assert [c1, c2].collect(cl) == [c1,c2]
    }
    
    void testStaticMethodAccess() {
       def list = [1].collect (this.&bb)
       assert list == [1]
       list = [1].collect (MethodClosureTest.&bb)
       assert list == [1]
       def mct = new MethodClosureTest()
       list = [1].collect (mct.&bb)
       assert list == [1]
    }
}


