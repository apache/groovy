package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy3304Bug extends GroovyTestCase {
    void testBreakAfterSwitchCausesSyntaxError() {
        try {
            new GroovyShell().parse("switch(x) {}\nbreak")
            fail()
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError
            assert syntaxError.line == 2
        }
    }
}
