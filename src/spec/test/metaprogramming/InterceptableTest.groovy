package metaprogramming

// tag::groovy_interceptable_test[]
class InterceptableTest extends GroovyTestCase {

    void testCheckInterception() {
        def interception = new Interception()

        assert interception.definedMethod() == 'invokedMethod'
        assert interception.someMethod() == 'invokedMethod'
    }
}
// end::groovy_interceptable_test[]

// tag::groovy_interceptable_object_example[]
class Interception implements GroovyInterceptable {

    def definedMethod() { }

    def invokeMethod(String name, Object args) {
        'invokedMethod'
    }
}
// end::groovy_interceptable_object_example[]