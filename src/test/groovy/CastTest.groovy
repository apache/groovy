class CastTest extends GroovyTestCase {

    void testCast() {
        x = (Short) 5

        println("Cast Integer to ${x}")
        
        x.class == Short
        
        methodWithShort(x)
    }

    void methodWithShort(Short s) {
        println("Called with ${s} with type ${s.class}")
        assert s.class == Short
    }
}
