package gls.ch03.s03
/**
 * GLS 3.3:
 * Implementations first recognize Unicode escapes in their input, translating 
 * the ASCII characters backslash and 'u' followed by four hexadecimal digits
 * to the Unicode character with the indicated hexadecimal value, and passing
 * all other characters unchanged.  
 *
 * @author Alan Green
 * @author Jeremy Rayner
 */

class UnicodeEscapes1 extends GroovyTestCase {

    void testAllHexDigits() {
        // All hex digits work (char def0 is a special codepoint)
        def s = "\u1234\u5678\u9abc\u0fed\u9ABC\u0FEC"
        assert s.charAt(0) == 0x1234
        assert s.charAt(1) == 0x5678
        assert s.charAt(2) == 0x9abc
        assert s.charAt(3) == 0x0fed
        assert s.charAt(4) == 0x9abc
        assert s.charAt(5) == 0x0fec
    }

    // There can be 1 or more u's after the backslash
    void testMultipleUs() {
        assert "\uu0061" == "a"
        assert "\uuu0061" == "a"
        assert "\uuuuu0061" == "a"
    }

    void testOtherVariations() {
        // Capital 'U' not allowed
        // assert "\U0061" == "a" // @fail:parse 
    }

    // todo: Implementations should use the \ uxxxx notation as an output format to
    // display Unicode characters when a suitable font is not available.
    // (to be tested as part of the standard library)

    // todo: Representing supplementary characters requires two consecutive Unicode
    // escapes. 
    // (not sure how to test)
    // see: gls.ch03.s01.Unicode2.testUTF16SupplementaryCharacters()

    // todo: test unicode escapes last in file
    // and invalid escapes at end of file
}
