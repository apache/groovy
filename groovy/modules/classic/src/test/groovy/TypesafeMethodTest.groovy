class TypesafeMethodTest extends GroovyTestCase {

    void testTypesafeMethod() {
        y = someMethod(1)

        assert y == 2
    }

    Integer someMethod(Integer i) {
        return i + 1
    }
}
