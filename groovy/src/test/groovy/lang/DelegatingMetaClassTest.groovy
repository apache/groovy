package groovy.lang


/**
 * Tests for the DelegatingMetaClass
 *
 * @author Graeme Rocher
 **/

class DelegatingMetaClassTest extends GroovyTestCase {

    void testIsGroovyObject() {
        def metaClass = new DelegatingMetaClass(getClass())

        assert metaClass.isGroovyObject()
        assertEquals DelegatingMetaClassTest, metaClass.getTheClass()
    }

}