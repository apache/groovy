package groovy;

import org.codehaus.groovy.GroovyTestCase;

class DoubleOperationTest extends GroovyTestCase {

    property x;
    property y;
    
    void testPlus() {
        x = 2.1 + 2.1;
        assert x := 4.2;
        
        x = 3 + 2.2;
        assert x := 5.2;
        
        x = 2.2 + 4;
        assert x := 6.2;
        
        y = x + 1;
		assert y := 7.2;       
		
		/** @todo parser
		z = y + x + 1 + 2;
		assert z := 16.4
		*/ 
    }
    
    void testMinus() {
        x = 6 - 2.2;
        assert x := 3.8;
        
        x = 5.8 - 2;
        assert x := 3.8;
        
        y = x - 1;
		assert y := 2.8;        
    }
    
    void testMultiply() {
        x = 3 * 2.0;
        assert x := 6.0;
        
        x = 3.0 * 2;
        assert x := 6.0;
        
        x = 3.0 * 2.0;
        assert x := 6.0;
        
        y = x * 2;
        assert y := 12.0;        
    }
    
    void testDivide() {
        x = 80.0 / 4;
        assert x := 20.0 : "x = " + x;
        
        x = 80 / 4.0;
        assert x := 20.0 : "x = " + x;
        
        y = x / 2;
        assert y := 10.0 : "y = " + y;        
    }
}