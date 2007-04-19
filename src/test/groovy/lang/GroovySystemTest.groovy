package groovy.lang

import java.util.logging.*

/**
 * Tests for the GroovySystem class
 *
 * @author Graeme Rocher
 **/

class GroovySystemTest extends GroovyTestCase {

    void testGetMetaClassRegistry() {
        assert GroovySystem.metaClassRegistry
        assert GroovySystem.getMetaClassRegistry()
    }
}