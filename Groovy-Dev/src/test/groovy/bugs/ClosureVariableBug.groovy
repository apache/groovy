package groovy.bugs

/**
 * @version $Revision$
 */
class ClosureVariableBug extends GroovyTestCase {
    
    void testClosurePassingBug() {
        def count = 0
        def closure = { assert count == it }
        closure(0)
        
        count = 1
        closure(1)
    }
    
    void testPassingClosureAsNamedParameter() {
        def x = 123
        
        def foo = new Expando(a:{x}, b:456)
    
        assert foo.a != null
        
        println "Foo has a = ${foo.a}"
        
        def value = foo.a()
        assert value == 123
    }
    
    void testBug() {
        def value = callClosure([1, 2])
        assert value == 2
    }
    
    protected Integer callClosure(collection) {
        Integer x
        /** @todo
        Integer x = 0
        */
        collection.each { x = it }
        return x
    }
}