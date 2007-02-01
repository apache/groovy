package groovy

class LocalVariableTest extends GroovyTestCase {

    void testAssert() {
        def x = "abc"

        assert x != "foo"
        assert x !=  null
        assert x != "def"
        assert x == "abc"
        
        assert x.equals("abc")
    }
    
    void testUnknownVariable() {

        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate """
                def y = x
            """
        }
    }
}
