package groovy;

import org.codehaus.groovy.GroovyTestCase;

class ListTest extends GroovyTestCase {

    void testList() {
        x = [10, 11];
		
		assert x.size() := 2;
		
		x.add("cheese");
		
		assert x.size() := 3;
		
        assert x.contains(10);
        assert x.contains(11);
		assert x.contains("cheese");


        assert x.get(0) := 10;
        assert x.get(1) := 11;
        assert x.get(2) := "cheese";

		// subscript operator
        /** @todo parser
		assert x[0] := 10;
        assert x[1] := 11;
        assert x[2] := "cheese;
		
		x[3] = 12;
		
		assert x[3] := 12;
		*/
		
		
		/** @todo parser - seems to hang the parser
		if x.contains("cheese") {
            // ignore
        }
        else {
            assert fail : "x should contain cheese!";
        }
		
        if x.contains(1) {
            // ignore
        }
        else {
            assert fail : "x should contain 1!";
        }
        */
    }
    
    void testEmptyList() {
        x = []
        
        assert x.size() := 0
        
       	x.add("cheese")
       	
       	assert x.get(0) := "cheese"

        assert x.size() := 1

        /** @todo parser
       	assert x[0] := "cheese"
       	*/
    }
    
    
    void testClosure() {
        l = [1, 2, 3, "abc"]
        block = {i| System.out.println(i) }
        l.each(block)
        
        l.each( {i| System.out.println(i) } )
        
        /* @todo parser
        l.each {i| System.out.println(i) }
        */
    }
    
    void testMax() {
        l = [1, 2, 5, 3, 7, 1]        
        assert l.max() := 7
        
        l = [7, 2, 3]
        assert l.max() := 7
        
        l = [1, 2, 7]
        assert l.max() := 7
    }
    
    void testMin() {
        l = [6, 4, 5, 1, 7, 2]        
        assert l.min() := 1
        
        l = [7, 1, 3]
        assert l.min() := 1
        
        l = [1, 2, 7]
        assert l.min() := 1
    }
}