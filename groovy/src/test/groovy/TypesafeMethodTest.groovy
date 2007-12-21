package groovy

class TypesafeMethodTest extends GroovyTestCase {

    void testTypesafeMethod() {
        def y = someMethod(1)

        assert y == 2
    }

    Integer someMethod(Integer i) {
        return i + 1
    }
}
