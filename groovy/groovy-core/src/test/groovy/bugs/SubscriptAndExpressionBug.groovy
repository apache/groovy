class SubscriptAndExpressionBug extends GroovyTestCase {
    
    void testBug() {
        foo = ["nice cheese grommit"]
        
        cheese = foo[0].startsWith("nice")
        
        assert cheese == true
    }

    void testSubscriptIncrement() {
        /** @todo bug
        foo = [5]
        */
        foo = [5, 6, 7]
        foo[0] += 5
        
        assert foo[0] == 10
        
        i = 0
        foo[i++] = 1
        assert foo[0] == 1
        assert i == 1
        
        foo[i++] += 5
        assert i == 2
        assert foo[1] == 11
    }
    
    void testDoubleSubscript() {
        foo = ["nice cheese grommit"]
        
        cheese = foo[0][5..10]
        
        assert cheese == "cheese"
    }
    
    void testSubscriptAndProperty() {
        foo = [['gromit':'cheese']]
        
        cheese = foo[0].gromit
        
        assert cheese == "cheese"
    }
}