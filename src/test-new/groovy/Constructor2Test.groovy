class Constructor2Test extends GroovyTestCase {

    // TODO modifier required to parser this!
    public Constructor2Test() {
        println "Hey"
    }

    void testConstructor() {
        def foo = new Constructor2Test()
        assert foo != null
        println foo
    }

}