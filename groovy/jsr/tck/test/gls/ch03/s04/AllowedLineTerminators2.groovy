package gls.ch03.s04
/**
 * GLS 3.4:
 * ...  It also * specifies the termination of the // form of a comment
 * (§3.7).
 */
class AllowedLineTerminators2 extends GroovyTestCase {

    void testLineEndings() {
        def a = 1
        // This comment ends with a CR \u0013 a = 2
        assert a == 2
        // This comment ends with a LF \u000a a = 3
        assert a == 3
        // This comment ends with a CRLF \u0013\u000a a = 4
        assert a == 4
    }
}

