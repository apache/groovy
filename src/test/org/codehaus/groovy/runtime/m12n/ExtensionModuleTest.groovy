package org.codehaus.groovy.runtime.m12n

import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import java.lang.reflect.Modifier

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {

    @Override
    protected void setUp() {
        super.setUp()

        // in order to test the @Grab behaviour, we need to replace the registry between each test
        GroovySystem.getDeclaredField('META_CLASS_REGISTRY').with {
            accessible = true
            modifiers = modifiers & ~Modifier.FINAL
            set(null, new MetaClassRegistryImpl())
        }
    }

    void testThatModuleHasBeenLoaded() {
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        assert registry.modules
        // look for the module
        assert registry.modules.any { it.name == 'Test module' && it.version == '1.0-test' }

        // the following methods are added by the test module
        def str = 'This is a string'
        assert str.reverseToUpperCase() == str.toUpperCase().reverse()
        assert String.answer() == 42
    }

    void testThatModuleCanBeLoadedWithGrab() {
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        // ensure that the module isn't loaded
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.1-test' } == false

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver+'''
        @Grab('module-test:module-test:1.1-test')
        import org.codehaus.groovy.runtime.m12n.*

        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        registry.modules.each { println "Found module ${it.name}" }

        // ensure that the module isn't loaded
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.1-test' }

        // the following methods are added by the Grab test module
        def str = 'This is a string'
        assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
        assert String.answer2() == 42

        '''
    }

    void testExtensionModuleUsingGrabAndMap() {
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        // ensure that the module isn't loaded
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.1-test' } == false

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver+'''
        @Grab('module-test:module-test:1.1-test')
        import org.codehaus.groovy.runtime.m12n.*

        def map = [:]
        assert 'foo'.taille() == 3
        assert map.taille() == 0

        '''
    }
}
