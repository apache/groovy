package gls.ch03.s03
/**
 * GLS 3.3: ...comments (§3.7) can serve to separate tokens that, if adjacent,
 * might be tokenized in another manner.
 *
 * See also the Whitespace tests (3.6)
 */
class CommentSeparatesTokens extends GroovyTestCase {

    void testCommentSeparates() {
        def a = 1
        a -= 1
        // a -/* a comment */= 1 // fail: parse

        def/* another comment */b = 2
        assert b == 2
    }
}
