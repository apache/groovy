package metaprogramming

import groovy.xml.Entity

// tag::meta_class_interception[]
class InterceptionThroughMetaClassTest extends GroovyTestCase {
    void testPOJOMetaClassInterception() {
        String invoking = 'ha'
        invoking.metaClass.invokeMethod = {String name, Object args ->
            'invoked'
        }
        assert invoking.length() == 'invoked'
        assert invoking.someMethod() == 'invoked'
    }

    void testPOGOMetaClassInterception() {
        Entity entity = new Entity('Hello')
        entity.metaClass.invokeMethod = {String name, Object args ->
            'invoked'
        }
        assert entity.build(new Object()) == 'invoked'
        assert entity.someMethod() == 'invoked'
    }
}
// end::meta_class_interception[]
