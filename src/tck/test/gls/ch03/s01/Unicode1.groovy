package gls.ch03.s01;
/**
 * Except for comments, identifiers and the contents of ... string 
 * literals, all input elements are formed from ASCII characters.
 *
 * TODO: Find a better way to test these things
 * Note that this is a little hard to test since the input file is ASCII.
 */

class Unicode1 extends GroovyTestCase {
    public void testComments() {
        // Unicode is allowed in comments
        // This is a comment \u0410\u0406\u0414\u0419
        /* Another comment \u05D0\u2136\u05d3\u05d7 */

        /**/ // Tiny comment
        /***/ // Also valid

        // Need to test string literals and identifiers
        println "All is well"
    }
}

