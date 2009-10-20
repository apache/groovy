package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy3827Bug extends GroovyTestCase {
    void testDuplicateCompilationErrorOnProperty() {
        GroovyClassLoader cl = new GroovyClassLoader();
        
        def scriptStr = """
            class NewGroovyClass {
                Test x
            }
        """
        try {
            cl.parseClass(scriptStr)
        } catch(MultipleCompilationErrorsException mcee) {
            assert mcee.errorCollector.errors.size() == 1
        }
    }
}
