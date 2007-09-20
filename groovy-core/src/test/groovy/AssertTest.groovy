package groovy

class AssertTest extends GroovyTestCase {

    void testAssert() {
        def x = null
        
        assert x == null
        assert x != "abc"
        assert x != "foo"
	    
        x = "abc"

        assert x != "foo"
        assert x !=  null
        assert x != "def"
        assert x == "abc"
        
        assert x.equals("abc")
        
        assert !x.equals("def")
        assert !false
        assert !(1==2)
        assert !(1>3)
        assert !(1!=1)
    }
	
    void testAssertFail() {
        def x = 1234

        def runCode = false
        try {
            runCode = true
            assert x == 5

            fail("Should have thrown an exception")
        }
        catch (AssertionError e) {
            //msg = "Expression: (x == 5). Values: x = 1234"
            //assert e.getMessage() == msg
            //assert e.message == msg
        }
        assert runCode, "has not ran the try / catch block code"
    }
}
