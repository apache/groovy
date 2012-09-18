package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5630:
 * Java stub generator generates wrong cast for return value of generic method
 *
 * @author Guillaume Laforge
 */
class WrongCastForGenericReturnValueOfMethodStubsTest extends StringSourcesStubTestCase  {

    @Override
    protected void setUp() {
        super.setUp()
        debug = true
    }

    Map<String, String> provideSources() {
        [
                'HelperUtil.groovy': '''
                    class HelperUtil {
                        final Map<String, String> test = new HashMap<String, String>()
                        static <T extends Task> T createTask(Class<T> type) { }
                    }
                ''',
                'Task.java': '''
                    public class Task {}
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('HelperUtil')

        assert stubSource.contains("public static <T extends Task> T createTask(java.lang.Class<T> type) { return (T)null;}")
        assert stubSource.contains("public final  java.util.Map<java.lang.String, java.lang.String> getTest() { return (java.util.Map<java.lang.String, java.lang.String>)null;}")
    }
}
