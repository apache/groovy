package groovy.bugs

/**
 *  Verifies that the Groovy parser can accept quoted methods.
 */

class Groovy1462_Bug extends GroovyTestCase {
 
    void testShort() {
        def smn = new StringMethodName()
        assert smn.foo0() == 'foo0'
        assert smn.'foo0'() == 'foo0'
        assert smn.foo1() == 'foo1'
        assert smn.'foo1'() == 'foo1'
        assert smn.foo2() == 2
        assert smn.foo3() == 3
        assert smn.foo4(3) == 12
        assert smn.foo5 == 'foo5'
        assert !smn.fooFalse()
        assert smn.fooDef() == null
    }
    
}

class StringMethodName {
    def foo0() {'foo0'} // control
    def 'foo1'() {'foo1'}
    public Integer 'foo2'() {2}
    public int 'foo3'() {3}
    Integer 'foo4'(x) { x * 4}
    public def 'getFoo5'() {'foo5'}
    private boolean 'fooFalse'() {false}
    public def 'fooDef'() {}
}
