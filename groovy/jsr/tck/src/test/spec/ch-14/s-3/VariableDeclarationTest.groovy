class VariableDeclarationTest extends GroovyTestCase {

    void testUninitialisedVariables() {
    	Integer a
    	String b
    	any c

        a = 1
        b = "hello"
        c = "whatever"
    }


    void testInitialisedVariables() {
    	Integer a = 1
    	String b = "Hello"

        any c = 2
        Integer d = 3

        assert a == 1
        assert b == "Hello"
        assert c == 2
        assert d - c == a
    }
}