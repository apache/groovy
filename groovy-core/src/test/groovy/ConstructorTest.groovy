package groovy

class ConstructorTest extends GroovyTestCase {

    public ConstructorTest() {
        println "Hey"
    }

    public void testConstructor() {
        def foo = new ConstructorTest()
        assert foo != null
        println foo
    }

}