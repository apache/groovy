package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5630:
 * Java stub generator generates wrong cast for return value of generic method
 *
 * (also covers GROOVY-5439)
 *
 * @author Guillaume Laforge
 */
class WrongCastForGenericReturnValueOfMethodStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
                'HelperUtil.groovy': '''
                    class HelperUtil {
                        final Map<String, String> test = new HashMap<String, String>()
                        static <T extends Task> T createTask(Class<T> type) { }
                        public <T extends List> T foo() { null }
                    }
                ''',
                'Task.java': '''
                    public class Task {}
                ''',
                'Schedule.groovy': '''
                    class Schedule<T extends ScheduleItem> extends HashSet<T> {
                        T getCurrentItem() { }
                    }
                ''',
                'ScheduleItem.java': '''
                    public class ScheduleItem {}
                '''
        ]
    }

    void verifyStubs() {
        def stubSourceForHelper = stubJavaSourceFor('HelperUtil')
        def stubSourceForSchedule = stubJavaSourceFor('Schedule')

        assert stubSourceForHelper.contains("public static <T extends Task> T createTask(java.lang.Class<T> type) { return (T)null;}")
        assert stubSourceForHelper.contains("public final  java.util.Map<java.lang.String, java.lang.String> getTest() { return (java.util.Map<java.lang.String, java.lang.String>)null;}")
        assert stubSourceForHelper.contains("public <T extends java.util.List> T foo() { return (T)null;}")

        assert stubSourceForSchedule.contains("public  T getCurrentItem() { return (T)null;}")
    }
}
