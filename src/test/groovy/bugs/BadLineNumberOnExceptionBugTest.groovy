package groovy.bugs

/**
 * Ensure that the correct line information is reported when an exception is thrown.
 * <p>
 * This test covers: <ul>
 * <li><a href="http://jira.codehaus.org/browse/GROOVY-3067">GROOVY-3067</a></li>
 * <li><a href="http://jira.codehaus.org/browse/GROOVY-2983">GROOVY-2983</a></li>
 *
 * @author Guillaume Laforge
 */
class BadLineNumberOnExceptionBugTest extends GroovyTestCase {

    void testGroovy3067() {
        assertScript """
            class Foo {
                boolean hello() { true }
            }

            try {
                foo = new Foo()

                if(foo.hello()()) { // line 9
                    println "do"
                    println "do"
                    println "do"
                    println "do"
                }

                assert false
            } catch (MissingMethodException e) {
                def scriptTraceElement = e.stackTrace.find { it.declaringClass.startsWith(GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 9 == scriptTraceElement.lineNumber
            }
        """
    }

    void testGroovy2983() {
        assertScript """
            def foo() {
                integer.metaClass = null // line 3
                integer.metaClass = null
                integer.metaClass = null
                integer.metaClass = null
            }
            
            try {
                foo()

                assert false
            } catch (MissingPropertyException e) {
                def scriptTraceElement = e.stackTrace.find { it.declaringClass.startsWith(GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 3 == scriptTraceElement.lineNumber
            }
        """
    }
}