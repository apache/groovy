package groovy;

import org.codehaus.groovy.GroovyTestCase;

class StringTest extends GroovyTestCase {

    void testString() {
        s = "abcd";
        assert s.length() := 4;
        assert 4 := s.length();
        
        // test polymorphic size() method like collections
        assert s.size() := 4;
        
        /** @todo parser
        s = s + "efg" + "hijk";
        
        assert s.size() := 11;
        
        assert "abcdef".size() := 6;
        */
    }

}