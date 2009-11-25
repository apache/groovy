package groovy.lang

import java.util.logging.*

/**
 * Tests for the GroovySystem class
 *
 * @author Graeme Rocher
 * @author Roshan Dawrani
 **/

class GroovySystemTest extends GroovyTestCase {

    void testGetMetaClassRegistry() {
        assert GroovySystem.metaClassRegistry
        assert GroovySystem.getMetaClassRegistry()
    }

    void testGroovyVersion() {
        assert GroovySystem.getVersion()
    }
}