package groovy.bugs

class Groovy3156And2621Bug extends GroovyTestCase {
    
	void testMethodNameResolutionInANestedClosure() {
        assert m() == 'method'
        assert c1() == 'method'
    }

    void testSimilarNamesForMethodAndLocalWithLocalAsMethodArgument() {
        failingExecute()
    }

    def m = { return 'method' }
    def c1 = {
        def m = { return 'c1' }
        def c2 = {
            /*
            *  If both 'm()' and 'this.m()' are used as follows,
            *  'this.m()' should not resolve to c1 closure's 'm' local variable.
            *  It should resolve to outermost class' m().
            */
            assert m() == 'c1'
            return this.m()
        }
        return c2()
    }

    void convention(String arg) {
        println 'called'
    }
    
    void failingExecute() {
        def convention= 'value'
        1.times {
            this.convention(convention)
        }
    }
}
