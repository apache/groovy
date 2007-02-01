package groovy.bugs

/**
 * @version $Revision: 1.3 $
 */
class DefVariableBug extends GroovyTestCase {
    
    void testBug() {

     /* cpoirier - "def" can be refered to as a variable name,
        but cannot be declared as one (due to ambiguities)

        def = 123
        
        assert def == 123
     */
        
        def foo = new Expando(a:123, def:456)
        assert foo.def == 456
    }
}
