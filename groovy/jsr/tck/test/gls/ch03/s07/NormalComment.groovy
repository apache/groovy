package gls.ch03.s07
/**
 * GLS 3.7:
 *    There are two kinds of comments:
 *        /* text * / 
 *            A traditional comment: all the text from the ASCII
 *            characters /* to the ASCII characters * / is ignored
 *            (as in C and C++).
 *        // text 
 *            A end-of-line comment: all the text from the ASCII
 *            characters // to the end of the line is ignored (as in 
 *            C++).
 */
class NormalComment extends GroovyTestCase {

    void testEolComment() {
        def a = 1

        def b = 1
        // a = a + (
        b = 2
        // )
        assert a == 1
        assert b == 2

        // GLS: ... some line terminators are transformed into significant
        // newlines.
        a = 3 // Significant new line follows this comment
        // / b //@fail:parse 
    }

    void testTraditionalComment() {
        // "def a" doesn't work
        def a = 1

        a = 10/*comment*//2
        assert a == 5
        a = 20//*comment*/2
        assert a == 10

        a = 10*/*comment*/2
        assert a == 20
        a = 20/*comment*/*2
        assert a == 40
    }

}
