package groovy.operator

class TernaryOperatorsTest extends GroovyTestCase {

    void testSimpleUse() {
        def y = 5

        def x = (y > 1) ? "worked" : "failed"
        assert x == "worked"


        x = (y < 4) ? "failed" : "worked"
        assert x == "worked"
    }

    void testUseInParameterCalling() {
        def z = 123
        assertCalledWithFoo(z > 100 ? "foo" : "bar")
        assertCalledWithFoo(z < 100 ? "bar" : "foo")
       }

    def assertCalledWithFoo(param) {
        println "called with param ${param}"
        assert param == "foo"
    }
    
    void testWithBoolean(){
        def a = 1
        def x = a!=null ? a!=2 : a!=1
        assert x == true
        def y = a!=1 ? a!=2 : a!=1
        assert y == false
    }
    
    void testElvisOperator() {
        def a = 1
        def x = a?:2
        assert x==a
        
        a = null
        x = a?:2
        assert x==2
        
        def list = ['a','b','c']
        def index = 0
        def ret = list[index++]?:"something else"
        assert index==1
        assert ret=='a'
    }
    
    void testForType() {
        boolean b = false
        int anInt = b ? 100 : 100 / 3
        assert anInt.class == Integer
    }
    
    void testBytecodeRegisters() {
        // this code will blow up if the true and false parts
        // are not handled correctly in regards to the registers.
        def i = 1
        def c= { false? { i } : it == i }
        assert true
    }

    void testLineBreaks() {
        def bar = 0 ? "moo" : "cow"
        assert bar == 'cow'

        bar = 0 ?
            "moo" : "cow"
        assert bar == 'cow'

        bar = 0 ? "moo" :
            "cow"
        assert bar == 'cow'

        bar = 0 ?
            "moo" :
            "cow"
        assert bar == 'cow'

        bar = 0 ? "moo"         \
              : "cow"
        assert bar == 'cow'

        // This used to fail
        bar = 0 ? "moo"
                : "cow"
        assert bar == 'cow'
    }
}   
