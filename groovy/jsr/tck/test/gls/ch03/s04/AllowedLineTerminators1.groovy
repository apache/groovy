package gls.ch03.s04
/**
 * GLS 3.4:
 * This definition of lines determines the line numbers
 * produced by a Java compiler or other system component.
 */

class AllowedLineTerminators1 extends GroovyTestCase {
    void testMessageLineNumbers() {
        
        // Need to execute compiler and check for correct line numbers 
        // in messages

        // Test CR, LF and CRLF. Be careful that CRLF counts as one line, not
        // two
        assertFalse("Can't test this, yet")
    }
}

