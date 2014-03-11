package metaprogramming

class MethodPropertyMissingTest extends GroovyTestCase {

    void testMethodMissing() {
        assertScript '''
            //tag::method_missing_simple[]
            class Foo {
               def methodMissing(String name, def args) {
                    return "this is me"
               }
            }

            assert new Foo().someUnknownMethod(42l) == 'this is me'
            //end::method_missing_simple[]
        '''
    }

    void testPropertyMissingGetter() {
        assertScript '''
            //tag::property_missing_getter[]
            class Foo {
               def propertyMissing(String name) { name }
            }

            assert new Foo().boo == 'boo'
            //end::property_missing_getter[]
        '''
    }

    void testPropertyMissingGetterSetter() {
        assertScript '''
            //tag::property_missing_getter_setter[]
            class Foo {
               def storage = [:]
               def propertyMissing(String name, value) { storage[name] = value }
               def propertyMissing(String name) { storage[name] }
            }

            def f = new Foo()
            f.foo = "bar"

            assert f.foo == "bar"
            //end::property_missing_getter_setter[]
        '''
    }

}
