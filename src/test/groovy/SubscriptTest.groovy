class SubscriptTest extends GroovyTestCase {

    void testListRange() {
        /** @todo fixme
		list = ['a', 'b', 'c', 'd', 'e']
		
		sub = list[2..4]

		assert sub == ['c', 'd']
		*/
    }
    
    void testStringSubscript() {
        text = "nice cheese gromit!"
        
        x = text[2]
        
        assert x == "c"
        assert x.class == String
        
        sub = text[5..11]
        
        assert sub == 'cheese'
    }
}
