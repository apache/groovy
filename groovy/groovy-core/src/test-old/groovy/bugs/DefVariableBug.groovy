/**
 * @version $Revision$
 */
class DefVariableBug extends GroovyTestCase {
    
    void testBug() {

     /* cpoirier - "def" can be refered to as a variable name,
        but cannot be declared as one (due to ambiguities)

        def = 123
        
        assert def == 123
     */
        
        foo = new Expando(a:123, def:456)
        assert foo.def == 456
    }
}
