package groovy.bugs

import org.codehaus.groovy.runtime.MethodClosure

class MethodClosureWithArrayBug extends GroovyTestCase {

    def aa(x) {
        println x
    }

    void testMetodClosure() {   
        Class[] c1 =  [ Exception.class, Throwable.class ]
        Class[] c2 = [ IllegalStateException.class ]

        println (c1)
        println (c2)

        def cl = this.&aa
        println(cl.class)

        assertTrue(cl instanceof Closure)
        assertTrue(cl instanceof MethodClosure)

        [c1, c2].each(cl)
    }
}


