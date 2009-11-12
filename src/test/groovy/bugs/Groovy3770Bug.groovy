package groovy.bugs

class Groovy3770Bug extends GroovyTestCase {
    void testSetDelegateAndResolveStrategyOnACurriedClosure() {
        assertScript """
            void hello(who) {
                println ("Hello " + who)
            }
            
            def c = { x ->
                hello(x)
            }
            
            def d = c.curry("Ian")
            d.call()
            
            d.delegate = null

            assert d.delegate == null

            d.resolveStrategy = Closure.DELEGATE_ONLY

            try {
                d.call()
                throw new RuntimeException("The curried closure call should have failed here with MME")
            } catch(MissingMethodException ex) {
                // ok if closure call returned in an exception (MME)
            }
        """
    }
    
    void testCurriedClosuresShouldNotAffectParent() {
        // GROOVY-3875
        def orig = { tmp -> println tmp }
        def curriedOrig = orig.curry(1)
        assert orig != curriedOrig.getOwner()
    }
}
