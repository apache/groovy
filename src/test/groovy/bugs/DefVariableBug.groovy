/**
 * @version $Revision: 1.2 $
 */
class DefVariableBug extends GroovyTestCase {
    
    void testBug() {
        def = 123
        
        assert def == 123
        
        foo = new Expando(a:123, def:456)
        assert foo.def == 456
    }
}