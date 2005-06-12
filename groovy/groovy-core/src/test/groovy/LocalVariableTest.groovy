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
        try {
            def y = x
            fail("x is undefined, should throw an exception")
        }
        catch (MissingPropertyException e) {
            assert e.getProperty() == "x"
            def text = e.message
            //e.printStackTrace()
            assert text == "No such property: x for class: LocalVariableTest"
        }
    }
}
