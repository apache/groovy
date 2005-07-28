package gls.ch03.s02

class LexicalTranslation1 extends GroovyTestCase {
    void testTranslationOfUnicodeEscapes() {
        assert "A" == "\u0041"
    }
    //todo: test that we have a stream of tokens (RI is antlr specific...)
}

