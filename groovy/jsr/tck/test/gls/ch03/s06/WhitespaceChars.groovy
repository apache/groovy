package gls.ch03.s06

/**
 * GLS 3.6:
 * White space is defined as the ASCII space, horizontal tab, and form feed
 * characters, as well as line terminators.
 */
class WhitespaceChars extends GroovyTestCase {

    void testWhitespace() {
        // Space (would otherwise be "defa = 1")
        def\u0020a = 1;
        assert a == 1

        // Tab
        def\u0009b = 2
        assert b == 2

        // Formfeed
        def\u000cc = 3 
        assert c == 3

        // Newline - careful, can also be a terminator
        def\u000dd = 4
        assert d == 4
    }

}
