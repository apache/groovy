class SubscriptTest extends GroovyTestCase {

    void testListRange() {
        list = ['a', 'b', 'c', 'd', 'e']
		
		sub = list[2..4]
		assert sub == ['c', 'd']
    }
    
    void testStringSubscript() {
        text = "nice cheese gromit!"
        
        x = text[2]
        
        assert x == "c"
        assert x.class == String
        
        sub = text[5..11]
        
        assert sub == 'cheese'
    }
    
    
    void testListSubscriptWithList() {
        list = ['a', 'b', 'c', 'd', 'e']
        
        indices = [0, 2, 4]
        sub = list[indices]
        assert sub == ['a', 'c', 'e']
        
        // verbose but valid
        sub = list[[1, 3]]
        assert sub == ['b', 'd']
     
        // syntax sugar
        sub = list[2, 4]
        assert sub == ['c', 'e']
    }
    
    
	void testListSubscriptWithListAndRange() {
	    list = 100..200
	    
	    sub = list[1, 3, 20..25, 33]
	    assert sub == [101, 103, 120, 121, 122, 123, 124, 133]
	    
	    // now lets try it on an array
	    array = list.toArray()
	    
	    sub = array[1, 3, 20..25, 33]
	    assert sub == [101, 103, 120, 121, 122, 123, 124, 133]
	}
	
    void testStringWithSubscriptList() {
        text = "nice cheese gromit!"
        
        sub = text[1, 2, 3, 5..11]
        
        assert sub == "icecheese"
    }
    
    void testSubMap() {
        map = ['a':123, 'b':456, 'c':789]
        
        keys = ['b', 'a']
        sub = map.subMap(keys)
        
        assert sub.size() == 2
        assert sub['a'] == 123
        assert sub['b'] == 456
        assert ! sub.containsKey('c')
    }
    
    void testListWithinAListSyntax() {
        list = [1, 2, 3, 4..10, 5, 6]
        
        assert list.size() == 6
        sublist = list[3]
        assert sublist == 4..10
        assert sublist == [4, 5, 6, 7, 8, 9]
    }
}
