package groovy

class MethodCallTest extends GroovyTestCase {

    void testMethodCall() {
        System.out.print("hello")
        println("world!")
    }

    void testObjectMethodCall() {
        def c = getClass()
        assert c != null
        assert c.name.endsWith("MethodCallTest")
        assert c.getName().endsWith("MethodCallTest")
    }

    void testObjectMethodCall2() {
        def s = "hello"
        def c = s.getClass()
        assert c != null
        assert c.name == "java.lang.String"
        assert c.getName() == "java.lang.String"
    }

    void testGetNameBug() {
        def c = getClass()
        def n = c.getName()
        assert c.getName().endsWith("MethodCallTest")
        assert n.endsWith("MethodCallTest")
    }
}
