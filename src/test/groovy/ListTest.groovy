class ListTest extends GroovyTestCase {

    void testList() {
        x = [10, 11]
		
		assert x.size() == 2
		
		x.add("cheese")
		
		assert x.size() == 3
		
        assert x.contains(10)
        assert x.contains(11)
		assert x.contains("cheese")


        assert x.get(0) == 10
        assert x.get(1) == 11
        assert x.get(2) == "cheese"

		// subscript operator
		assert x[0] == 10
        assert x[1] == 11
        assert x[2] == "cheese"
		
        x[3] = 12
		
		assert x[3] == 12
		
		
		if ( x.contains("cheese") ) {
            // ignore
        }
        else {
            assert false : "x should contain cheese!"
        }
		
        if ( x.contains(10) ) {
            // ignore
        }
        else {
            assert false : "x should contain 1!"
        }
    }
    
    void testEmptyList() {
        x = []
        
        assert x.size() == 0
        
       	x.add("cheese")
       	
       	assert x.get(0) == "cheese"

        assert x.size() == 1

       	assert x[0] == "cheese"
    }
    
    void testSubscript() {
        x = []
        x[1] = 'cheese'
        
        assert x[0] == null
        assert x[1] == 'cheese'
        
        x[2] = 'gromit'
        x[3] = 'wallace'
        
        assert x.size() == 4
        
        /** @todo parser bug

        index = -1
        
        x[-1] = 'nice'
        
        assert x[3] == 'nice'
        
        x[-2] = 'cheese'
        
        assert x[2] == 'cheese'
        */
    }
    
    void testClosure() {
        l = [1, 2, 3, "abc"]
        block = {i| println(i) }
        l.each(block)
        
        l.each {i| println(i) }
    }
    
    void testMax() {
        l = [1, 2, 5, 3, 7, 1]        
        assert l.max() == 7
        
        l = [7, 2, 3]
        assert l.max() == 7
        
        l = [1, 2, 7]
        assert l.max() == 7
    }
    
    void testMin() {
        l = [6, 4, 5, 1, 7, 2]        
        assert l.min() == 1
        
        l = [7, 1, 3]
        assert l.min() == 1
        
        l = [1, 2, 7]
        assert l.min() == 1
    }
    
    void testPlus() {
        l1 = [6, 4, 5, 1, 7, 2]        
        l2 = [6, 4, 5, 1, 7, [4,5]]
        assert l1 + l2 == [6, 4, 5, 1, 7, 2, 6, 4, 5, 1, 7, [4,5]]            
    }
    
    void testPlusOneElement() {
        l1 = [6, 4, 5, 1, 7, 2]        
        l2 = "erererer"
        assert l1 + l2 == [6, 4, 5, 1, 7, 2, "erererer"]            
    }
    
    void testTimes() {
        l = [4,7,8]
        assert l * 3 == [4, 7, 8, 4, 7, 8, 4, 7, 8]
    }
    
    void testMinus() {
        l1 = [1, 1, 2, 2, 3, 3, 3, 4, 5] 
        l2 = [1, 2, 4] 
        assert l1 - l2 == [3, 5] 
    }

    void testMinusDifferentTypes() {
        l1 = [1, 1, "wrer", 2, 3, 3, "wrewer", 4, 5, "w", "w"] 
        l2 = [1, 2, "w"] 
        assert l1 - l2 == ["wrer", 3, "wrewer", 4, 5] 
    }  
     
    void testIntersect() {
        l1 = [1, 1, "wrer", 2, 3, 3, "wrewer", 4, 5, "w", "w"] 
        l2 = [1, 2, "f", "w"] 
        assert l1.intersect(l2) == [1, 2, "w"] 
    }
      
    void testFlatten() {
        l= [[[4, 5, 6, [46, 7, "erer"]], 4, [3, 6, 78]], 4]
        assert l.flatten() == [4, 5, 6, 46, 7, "erer", 4, 3, 6, 78, 4]
    }
    
    void testFlattenWithRanges() {
        flat = [1, 3, 20..24, 33].flatten()
        assert flat == [1, 3, 20, 21, 22, 23, 33]
    }
    
    void testRemove() {
        l = ['a', 'b', 'c']
        
        l.remove(1)
        
        assert l == ['a', 'c']
        
        l.remove(0)
        
        assert l == ['c']
        assert l.size() == 1
    }
}
