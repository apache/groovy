class MethodCallTest extends GroovyTestCase {

    void testMethodCall() {
        System.out.println("hello")
        "world!".println()
	}
	
    void testObjectMethodCall() {
        c = getClass()
        assert c != null
        assert c.name.endsWith("MethodCallTest")
        assert c.getName().endsWith("MethodCallTest")
    }
	
    void testObjectMethodCall2() {
        s = "hello"
        c = s.getClass()
        assert c != null
        assert c.name == "java.lang.String"
        assert c.getName() == "java.lang.String"
    }
	
	void testGetNameBug() {
	    c = getClass()
	    n = c.getName()
        assert c.getName().endsWith("MethodCallTest")
        assert n.endsWith("MethodCallTest")
	}
}
