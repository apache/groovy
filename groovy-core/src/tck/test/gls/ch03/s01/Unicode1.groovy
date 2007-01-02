package gls.ch03.s01;
/**
 * Except for comments, identifiers and the contents of ... string 
 * literals, all input elements are formed from ASCII characters.
 *
 * TODO: Find a better way to test these things
 * Note that this is a little hard to test since the input file is ASCII.
 *
 * @author Alan Green
 * @author Jeremy Rayner
 */

class Unicode1 extends GroovyTestCase {
    //TODO: find some way to assert that Unicode3.0 + is available

    /**
      * This doc comment checks that Unicode is allowed in javadoc.
      * e.g. \u05D0\u2136\u05d3\u05d7
      */
    public void testComments() {
        // Unicode is allowed in comments
        // This is a comment \u0410\u0406\u0414\u0419
        /* Another comment \u05D0\u2136\u05d3\u05d7 */

        /**/ // Tiny comment
        /***/ // Also valid
    }

    public void testStringLiterals() {
        assert 1 == "\u0040".length()
        assert "A" == "\u0041"
    }

    public void testCharNotAvailableAsLiteral() {
        char a = 'x'
        char b = "x"
        def c = "x".charAt(0)
        assert a == b
        assert a == c 
    }

}

