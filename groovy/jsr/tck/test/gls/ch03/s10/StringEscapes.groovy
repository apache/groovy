package gls.ch03.s10;
/**
 * JLS: 3.10.5
 * [S]tring escape sequences allow for the representation of some nongraphic
 * characters as well as the single quote, double quote, and backslash
 * characters in ... string literals (§3.10.5).
 *
 * GLS 3.10.5
 * In all strings, the escape sequence '\$' is legal, and stands for the ASCII
 * dollar character.
 */
class StringEscapes extends GroovyTestCase {

    void testStandardJavaEscapeChars() {
        String s = "\b\t\n\f\r\"\'\\"
        assert s.charAt(0) == 0x8
        assert s.charAt(1) == 0x9
        assert s.charAt(2) == 0xa
        assert s.charAt(3) == 0xc
        assert s.charAt(4) == 0xd
        assert s.charAt(5) == 0x22
        assert s.charAt(6) == 0x27
        assert s.charAt(7) == 0x5c

        // A selction of invalid, non-standard escapes
        //String s2 = "\q"//@fail:parse
        //String s2 = "\}"//@fail:parse
        //String s2 = "\^"//@fail:parse
        //String s2 = "\N"//@fail:parse
        //String s2 = "\@"//@fail:parse
        //String s2 = "\a"//@fail:parse
        //String s2 = "\A"//@fail:parse
    }

    void testOctalEscapes() {
        String s = "\0\00\000\1\377\042\42"
        assert s.charAt(0) == 0
        assert s.charAt(1) == 0
        assert s.charAt(2) == 0
        assert s.charAt(3) == 1
        assert s.charAt(4) == 255
        assert s.charAt(5) == 042
        assert s.charAt(6) == 042

        String s2 = "\0007"
        assert s2.length() == 2
        assert s2.charAt(0) == 0
        assert s2.charAt(1) == "7"

        // A selection of invalid escapes
    }
}
