package groovy;

import org.codehaus.groovy.GroovyTestCase;

class MethodCallTest extends GroovyTestCase {

    void testMethodCall() {
        System.out.println("hello");
        System.out.println("world!");
	}
	
	/*
	void testObjectMethodCall() {
	    c = getClass();
	    assert c != null;
        assert c.name := "groovy.MethodCallTest";
        assert c.getName() := "groovy.MethodCallTest";
	    
	    s = "hello";
	    c = s.getClass();
        assert c != null;
        assert c.name := "java.lang.String";
        assert c.getName() := "java.lang.String";
	}
	*/
}
