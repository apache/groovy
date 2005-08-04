package gls.ch03.s01;
/**
 * Except for comments, identifiers and the contents of ... string 
 * literals, all input elements are formed from ASCII characters.
 *
 * TODO: Find a better way to test these things
 * Note that this is a little hard to test since the input file is ASCII.
 *
 * @author Jeremy Rayner
 */

class Unicode2 extends GroovyTestCase {

//todo - this doesn't seem to work in raw Java5.0 either
//    public void testUTF16SupplementaryCharacters() {
//        assert 1 == "\uD840\uDC00".length()
//    }

    public void testIdentifiers() {
        def foo\u0044 = 12
        assert 20 == foo\u0044 + 8
    }
}

