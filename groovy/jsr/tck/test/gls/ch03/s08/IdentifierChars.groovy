package gls.ch03.s08
/**
 * JLS 3.8:
 * An identifier is an unlimited-length sequence of Java letters and Java
 * digits, the first of which must be a Java letter. ... 
 *
 * Letters and digits may be drawn from the entire Unicode character set... 
 *
 * A “Java letter” is a character for which the method
 * Character.isJavaIdentifierStart(int) returns true. A “Java letter-or-digit”
 * is a character for which the method Character.isJavaIdentifierPart(int)
 * returns true.  
 */
class IdentifierChars extends GroovyTestCase {

    void testASCIIChars() {
        // Identifier may not start with a digit
        //def 1a = 1 //@fail:parse

        // Identifier may contain digits after the first letter
        def a1 = 2 
        assert a1 == 2

        // Identifier may contain any of the ASCII digits
        def a1234567890 = "Hello"
        assert a1234567890 == "Hello"

        // Identifier may contain any of the ASCII upper or lower case letters
        def ZYXWVUTSRQPONMLKJIHGFEDCBA = "backwords"
        assert ZYXWVUTSRQPONMLKJIHGFEDCBA == "backwords"

        // Identifier may contain any of the ASCII upper or lower case letters
        def zyxwvutsrqponmlkjighfedcba = "BACKWORDS"
        assert zyxwvutsrqponmlkjighfedcba == "BACKWORDS"
    }

    void testNonAsciiChars() {
        // Non-ascii chars encoded directly in the stream of text
        def a = 2
        def a² = a * a  // a-squared in latin-1 encoding
        assert a² == 4
        def µ = 10e-6 
        assert µ = 10e-6
    }

    void testEncodedAsciiChars() {
        // Non-ascii chars encoded directly in the stream of text
        def a = 2
        def a² = a * a  // a-squared in latin-1 encoding
        assert a² == 4
        def µ = 10e-6 
        assert µ = 10e-6
    }

    
    void testComposedChar() {
        // Example taken from JLS
        def \u00c1 = "LATIN CAPITAL LETTER A ACUTE"
        //assert \u004a\0301 == "LATIN CAPITAL LETTER A ACUTE" //@fail
    }

    void testVisualEquivalents() {
        // Example taken from JLS
        def A = "A"
        //assert a == A //@fail
        //assert \u0391 == A //@fail
        //assert \u0430 == A //@fail
        //assert \ud835\udc82 == A //@fail
    }
}

