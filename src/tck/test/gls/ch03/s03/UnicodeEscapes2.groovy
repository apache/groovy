package gls.ch03.s03
/**
 * GLS 3.3:
 * Implementations first recognize Unicode escapes in their input, translating 
 * the ASCII characters backslash and 'u' followed by four hexadecimal digits
 * to the Unicode character with the indicated hexadecimal value, and passing
 * all other characters unchanged.  
 *
 * @author Alan Green
 */

class UnicodeEscapes2 extends GroovyTestCase {

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
    void testInvalidHexDigits() {
        // invalid hex digits
        // assert "\ufffg" == "a" // @fail:parse
        // assert "\uu006g" == "a" // @fail:parse
        // assert "\uab cd" == "acd" // @fail:parse
    }
}
