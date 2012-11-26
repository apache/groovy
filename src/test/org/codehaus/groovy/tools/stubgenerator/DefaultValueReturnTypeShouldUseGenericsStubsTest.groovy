package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5710: Stub generator should not use raw types when casting default return values
 *
 * @author Guillaume Laforge
 */
class DefaultValueReturnTypeShouldUseGenericsStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
                'GantBinding.groovy': '''
                    class GantBinding {
                        private List<BuildListener> buildListeners = []
                        List<BuildListener> getBuildListeners ( ) { buildListeners }
                    }
                ''',
                'BuildListener.java': '''
                    public class BuildListener {}
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('GantBinding')

        assert stubSource.contains('return (java.util.List<BuildListener>)null')
    }
}
