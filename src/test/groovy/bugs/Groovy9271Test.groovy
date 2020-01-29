package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration

class Groovy9271Test extends GroovyTestCase {
    private cc = new CompilerConfiguration(optimizationOptions: [indy: true])
    void testBracketsInMethodNameWithIndy() {
        new GroovyShell(cc).evaluate '''
        class Bar {
            private char letter = 'o'
            int 'foo$()bar'() { 'Goodbye'.toList().count{ it == letter } }
        }
        assert new Bar().'foo$()bar'() == 2
        '''
    }
}
