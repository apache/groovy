class StaticThisTest extends GroovyTestCase {

    void testThisFail() {
        staticMethod()
    }

    static def staticMethod() {
        foo = this

        assert foo != null
        assert foo.name.endsWith("StaticThisTest")

        println("this: " + this)

        s = super

        assert s != null
        assert s.name.endsWith("GroovyTestCase")

        println("super: " + super)
    }
}
