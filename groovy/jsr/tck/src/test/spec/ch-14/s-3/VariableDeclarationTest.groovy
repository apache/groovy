class VariableDeclarationTest extends GroovyTestCase {

    void testUninitialisedVariables() {
    	Integer a
    	String b
    	def c

        a = 1
        b = "hello"
        c = "whatever"
    }


    void testInitialisedVariables() {
    	Integer a = 1
    	String b = "Hello"

        def c = 2
        def Integer d = 3

        assert a == 1
        assert b == "Hello"
        assert c == 2
        assert d - c == a
    }
}