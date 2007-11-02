package groovy

class StaticThisTest extends GroovyTestCase {

    void testThisFail() {
        staticMethod()
    }

    static def staticMethod() {
        def foo = this

        assert foo != null
        assert foo.name.endsWith("StaticThisTest")

        println("this: " + this)

        def s = super

        assert s != null
        assert s.name.endsWith("GroovyTestCase")

        println("super: " + super)
    }
}
