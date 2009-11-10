package groovy.bugs

class Groovy3873Bug extends GroovyTestCase {
    void testAddingMethodsToMetaClassOfInterface() {
        try {
            ExpandoMetaClass.enableGlobally()
            List.metaClass.methodMissing = { String name, args ->
                true
            }
            def list = []
            assert list.noSuchMethod()
        } finally {
            ExpandoMetaClass.disableGlobally()
        }
    }
}