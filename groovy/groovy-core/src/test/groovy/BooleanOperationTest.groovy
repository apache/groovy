package groovy;

import org.codehaus.groovy.GroovyTestCase;

class BooleanOperationTest extends GroovyTestCase {

    void testComparisons() {
        assert true;
        assert true != false;
        
        x = true;
        
        assert x;
        assert x := true;
        assert x != false;
        
        x = false;
        
        assert x := false;
        assert x != true;
        
        /** @todo parser
        assert !x;
        */
        
        y = false;        
        assert x := y;
        
        y = true;
        assert x != y;
    }
    
    
    void testIfBranch() {
        x = false;
        r = false;
        
        /** @todo parser - seems to hang the parser
        if x {
            // ignore
        }
        else {
            r = true;
        }
        assert r;
        
        x = true;
        r = false;
        
        if x {
            r = true;
        }
        else {
            // ignore
        }
        assert r;
        
        if !x {
            r = false;
        }
        else {
            r = true;
        }
        
        assert r;
        */
    }

}