package gls.ch03.s03
/**
 * GLS 3.3:
 * Implementations first recognize Unicode escapes in their input, translating 
 * the ASCII characters backslash and 'u' followed by four hexadecimal digits
 * to the Unicode character with the indicated hexadecimal value, and passing
 * all other characters unchanged.  
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

    // GLS: If an even number of backslashes precede the 'u', it is not 
    // an escape
    void testCountBackslash() {
        def a = 1
        assert \u0061 == 1 // char 61 is 'a'
        
        // Not intepreted as an escape
        // \\u0061 == 1 // @fail:parse

        assert "\u0061".length() == 1
        // Double backslash interpreted as a single backslash in string
        assert "\\u0061".length() == 6
        assert "\\\u0061".length() == 2
        
    }

    // There can be 1 or more u's after the backslash
    void testMultipleUs() {
        assert "\uu0061" == "a"
        assert "\uuu0061" == "a"
        assert "\uuuuu0061" == "a"
    }

    // GLS: If an eligible \ is followed by u, or more than one u, and the last u
    // is not followed by four hexadecimal digits, then a compile-time error
    // occurs.
    void testFourHexDigits() {
        // these next lines won't work. The backslash has been replace by a 
        // forwards slash so that the file parses. (Comments don't comment out
        // unicode escapes.)
        // assert "/u7" == "\07" //@fail:parse 
        // def /u61 = 2 //@fail:parse 
        // def /u061 = 2 //@fail:parse 

        // If five digits, only the first four count
        def \u00610 = 2 
        assert a0 == 2
    }

    void testOtherVariations() {
        // Capital 'U' not allowed
        // def \U0061 = 1 // @fail:parse 
    }

    // GLS: Implementations should use the \ uxxxx notation as an output format to
    // display Unicode characters when a suitable font is not available.
    // (to be tested as part of the standard library)

    // GLS: Representing supplementary characters requires two consecutive Unicode
    // escapes. 
    // (not sure how to test)

    // Also: test unicode escapes last in file
    // and invalid escapes at end of file
}
