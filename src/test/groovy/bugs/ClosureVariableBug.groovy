/**
 * @version $Revision$
 */
class ClosureVariableBug extends GroovyTestCase {
    
    void testBug() {
        count = 0
        closure = { assert count == it }
        closure(0)
        
        count = 1
        closure(1)
    }
    
    void testPassingClosureAsNamedParameter() {
        x = 123
        
        foo = new Expando(a:{x}, b:456)
    
    	assert foo.a != null
        
        println "Foo has a = ${foo.a}"
        
    	value = foo.a()
    	assert value == 123
    }
}