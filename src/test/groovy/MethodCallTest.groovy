package groovy;



class MethodCallTest extends GroovyTestCase {

    void testMethodCall() {
        System.out.println("hello");
        System.out.println("world!");
	}
	
    void testObjectMethodCall() {
        c = getClass();
        assert c != null;
        assert c.name := "groovy.MethodCallTest";
        assert c.getName() := "groovy.MethodCallTest";
    }
	
    void testObjectMethodCall2() {
        s = "hello";
        c = s.getClass();
        assert c != null;
        assert c.name := "java.lang.String";
        assert c.getName() := "java.lang.String";
    }
	
	void testGetNameBug() {
	    c = getClass();
	    n = c.getName();
        assert c.getName() := "groovy.MethodCallTest";
        assert n := "groovy.MethodCallTest";
	}
}
