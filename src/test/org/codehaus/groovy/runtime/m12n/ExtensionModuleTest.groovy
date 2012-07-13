package org.codehaus.groovy.runtime.m12n

import java.lang.reflect.Modifier

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {

    private List<ExtensionModule> savedModules

    @Override
    protected void setUp() {
        super.setUp()
        // save modules here to clean up after ourselves
        savedModules = GroovySystem.metaClassRegistry.moduleRegistry.modules
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        GroovySystem.metaClassRegistry.moduleRegistry.class.getDeclaredField('modules').with {
            accessible = true
            modifiers = modifiers & ~Modifier.FINAL
            set(GroovySystem.metaClassRegistry.moduleRegistry, savedModules)
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
        assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver + '''
        @Grab('module-test:module-test:1.2-test')
        import org.codehaus.groovy.runtime.m12n.*

        // ensure that the module is now loaded
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

        // the following methods are added by the Grab test module
        def str = 'This is a string'
        assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
        assert String.answer2() == 42
        '''

        // it should still be available
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }
    }

    void testExtensionModuleUsingGrabAndMap() {
        println "ExtensionModuleTest.testExtensionModuleUsingGrabAndMap"
        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        // ensure that the module isn't loaded
        assert !registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.2-test' }

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver + '''
        @Grab('module-test:module-test:1.2-test')
        import org.codehaus.groovy.runtime.m12n.*

        def map = [:]
        assert 'foo'.taille() == 3
        assert map.taille() == 0
        '''
    }
}
