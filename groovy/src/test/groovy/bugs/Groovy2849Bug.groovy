package groovy.bugs

class Groovy2849Bug extends GroovyTestCase {
    def void testPropertySelectionConflictInANestedClosure(){
        assert c1() == 11
        assert p == 11
    }
    def p = 1
    def c1 = {
        def p = 2
        def c2 = {
            /*
             *  If both 'test' and 'this.test' are used as below,
             *  'this.test' should not resolve to c1 closure's 'test' property.
             *  It should resolve to Groovy2849Bug's 'test' property.
             */
            this.p += 10
            p = 3
            assert p == 3
            return this.p
        }
        return c2()
    }
}
