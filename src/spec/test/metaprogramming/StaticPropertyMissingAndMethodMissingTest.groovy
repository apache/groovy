package metaprogramming

class StaticPropertyMissingAndMethodMissingTest extends GroovyTestCase {

    void testStaticMethodMissing() {
        assertScript '''
            // tag::static_method_missing[]
            class Foo {
                static def $static_methodMissing(String name, Object args) {
                    return "Missing static method name is $name"
                }
            }

            assert Foo.bar() == 'Missing static method name is bar'
            // end::static_method_missing[]
'''
    }

    void testStaticPropertyMissing() {

        assertScript '''
            // tag::static_property_missing[]
            class Foo {
                static def $static_propertyMissing(String name) {
                    return "Missing static property name is $name"
                }
            }

            assert Foo.foobar == 'Missing static property name is foobar'
            // end::static_property_missing[]
'''
    }
}
