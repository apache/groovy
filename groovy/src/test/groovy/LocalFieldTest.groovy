package groovy

class LocalFieldTest extends GroovyTestCase {

    private def x
	
    void testAssert() {
        this.x = "abc"

        assert this.x == "abc"
        assert this.x != "def"
    }
}
