package groovy.bugs

class SubscriptAndExpressionBug extends GroovyTestCase {
    
    void testBug() {
        def foo = ["nice cheese grommit"]
        
        def cheese = foo[0].startsWith("nice")
        
        assert cheese == true
    }

    void testSubscriptIncrement() {
        def foo = [5, 6, 7]
        foo[0] += 5
        
        assert foo[0] == 10
        
        def i = 0
        foo[i++] = 1
        assert foo[0] == 1
        assert i == 1
        
        foo[i++] += 5
        assert i == 2
        assert foo[1] == 11
    }

    void testLargeSubscript() {
        def foo = [1]
        
        foo[10] = 123
        
        assert foo[10] == 123
        
        foo.putAt(12, 55)
        assert foo[12] == 55
        
        def i = 20
        foo[i] = 1
        foo[i++] += 5
        
        assert i == 21
        assert foo[20] == 6
    }
    
    void testDoubleSubscript() {
        def foo = ["nice cheese grommit"]
        
        def cheese = foo[0][5..10]
        
        assert cheese == "cheese"
    }
    
    void testSubscriptAndProperty() {
        def foo = [['gromit':'cheese']]
        
        def cheese = foo[0].gromit
        
        assert cheese == "cheese"
    }
    
    void testBooleanExpression() {
       int[] a = new int[1]
       assert (a[0] = 42) == 42 
       assert a[0] == 42
    }
}