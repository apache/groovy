package groovy.bugs

/**
 *  Verifies that comparisons to Integer.MIN_VALUE work
 */

class Groovy513_Bug extends GroovyTestCase {
 
    void testMinMaxValueComparison() {
    	assertTrue(8 < Integer.MAX_VALUE);
    	assertTrue(8 > Integer.MIN_VALUE);
    	assertTrue(8L < Long.MAX_VALUE);
    	assertTrue(8L > Long.MIN_VALUE);
    	assertTrue(8.0 < Double.MAX_VALUE);
    	assertTrue(8.0 > Double.MIN_VALUE);
    }
    
}
