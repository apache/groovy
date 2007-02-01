package groovy

class Constructor2Test extends GroovyTestCase {

    Constructor2Test() {
        println "Hey"
    }

    void testConstructor() {
        def foo = new Constructor2Test()
        assert foo != null
        println foo
    }

}