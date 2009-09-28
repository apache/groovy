package groovy.bugs

import java.lang.reflect.*
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.GroovyBugError

class Groovy3776Bug extends GroovyTestCase {
    void testInvalidListWithMapEntryExpressions() {
        GroovyClassLoader cl = new GroovyClassLoader();
        
        def scriptStr = """
            class InvalidListLiteral {
                def x = [
                    [foo: 1, bar: 2]
                    [foo: 1, bar: 2]
                ]
            }
        """
        try {
            cl.parseClass(scriptStr)
            fail('Compilation should have failed with MultipleCompilationErrorsException')
        } catch(MultipleCompilationErrorsException mcee) {
            // ok if failed with this error.
        } catch(GroovyBugError gbe) {
            fail('Compilation should have failed with MultipleCompilationErrorsException but failed with GroovyBugError')
        }
    }
}
