package org.codehaus.groovy.runtime.m12n

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {
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
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.0-test' } == false

        // find jar resource
        def jarURL = this.class.getResource("/jars")
        assert jarURL

        def resolver = "@GrabResolver(name='local',root='$jarURL')"

        assertScript resolver+'''
        @Grab('module-test:module-test:1.0-test')
        import org.codehaus.groovy.runtime.m12n.*

        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
        registry.modules.each { println "Found module ${it.name}" }

        // ensure that the module isn't loaded
        assert registry.modules.any { it.name == 'Test module for Grab' && it.version == '1.0-test' }

        // the following methods are added by the Grab test module
        def str = 'This is a string'
        assert str.reverseToUpperCase2() == str.toUpperCase().reverse()
        assert String.answer2() == 42

        '''
    }
}
