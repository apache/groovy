class SubscriptAndExpressionBug extends GroovyTestCase {
    
    void testBug() {
        foo = ["nice cheese grommit"]
        
        cheese = foo[0].startsWith("nice")
        
        assert cheese == true
    }

    void testSubscriptIncrement() {
        foo = [5]

        foo[0] = foo[0] + 5
        
        /** @todo bug
        foo[0] += 5
        */
        
        assert foo[0] == 10
    }
    
    void testDoubleSubscript() {
        foo = ["nice cheese grommit"]
        
        cheese = foo[0][5..10]
        
        assert cheese == "cheese"
    }
}