package org.codehaus.groovy.runtime.m12n

/**
 * Unit tests for extension methods loading.
 */
class ExtensionModuleTest extends GroovyTestCase {

    void testThatModuleHasBeenLoaded() {
        ExtensionModuleHelperForTests.doInFork '''
            ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry
            assert registry.modules
            // look for the module
            assert registry.modules.any { it.name == 'Test module' && it.version == '1.0-test' }

            // the following methods are added by the test module
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
            assert String.answer() == 42
        '''
    }

    void testThatModuleCanBeLoadedWithGrab() {
        ExtensionModuleHelperForTests.doInFork 'ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry\n' +
                '        // ensure that the module isn\'t loaded\n' +
                '        assert !registry.modules.any { it.name == \'Test module for Grab\' && it.version == \'1.2-test\' }\n' +
                '\n' +
                '        // find jar resource\n' +
                '        def jarURL = this.class.getResource("/jars")\n' +
                '        assert jarURL\n' +
                '\n' +
                '        def resolver = "@GrabResolver(name=\'local\',root=\'$jarURL\')"\n' +
                '\n' +
                '        assertScript resolver + \'\'\'\n' +
                '        @Grab(value=\'module-test:module-test:1.2-test\', changing=true)\n' +
                '        import org.codehaus.groovy.runtime.m12n.*\n' +
                '\n' +
                '        // ensure that the module is now loaded\n' +
                '        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry\n' +
                '        assert registry.modules.any { it.name == \'Test module for Grab\' && it.version == \'1.2-test\' }\n' +
                '\n' +
                '        // the following methods are added by the Grab test module\n' +
                '        def str = \'This is a string\'\n' +
                '        assert str.reverseToUpperCase2() == str.toUpperCase().reverse()\n' +
                '        assert String.answer2() == 42\n' +
                '        \'\'\'\n' +
                '\n' +
                '        // it should still be available\n' +
                '        assert registry.modules.any { it.name == \'Test module for Grab\' && it.version == \'1.2-test\' }'

    }

    void testExtensionModuleUsingGrabAndMap() {
        ExtensionModuleHelperForTests.doInFork 'println "ExtensionModuleTest.testExtensionModuleUsingGrabAndMap"\n' +
                '        ExtensionModuleRegistry registry = GroovySystem.metaClassRegistry.moduleRegistry\n' +
                '        // ensure that the module isn\'t loaded\n' +
                '        assert !registry.modules.any { it.name == \'Test module for Grab\' && it.version == \'1.2-test\' }\n' +
                '\n' +
                '        // find jar resource\n' +
                '        def jarURL = this.class.getResource("/jars")\n' +
                '        assert jarURL\n' +
                '\n' +
                '        def resolver = "@GrabResolver(name=\'local\',root=\'$jarURL\')"\n' +
                '\n' +
                '        assertScript resolver + \'\'\'\n' +
                '        @Grab(value=\'module-test:module-test:1.2-test\', changing=true)\n' +
                '        import org.codehaus.groovy.runtime.m12n.*\n' +
                '\n' +
                '        def map = [:]\n' +
                '        assert \'foo\'.taille() == 3\n' +
                '        assert map.taille() == 0\n' +
                '        \'\'\''
    }
}
