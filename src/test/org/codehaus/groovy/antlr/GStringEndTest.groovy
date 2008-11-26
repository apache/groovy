import org.codehaus.groovy.control.*

class GStringEndTest extends GroovyTestCase {
    void testInvalidEndContainsLineNumber(){
        try {
            assertScript '''
                def Target = "releases$"
            '''
        } catch (MultipleCompilationErrorsException mcee) {
            def text = mcee.toString();
            assert text.contains("line 2, column 41")
        }
  }
}