package gls.ch03.s02

/**
 * GLS 3.2: The longest possible translation is used at each step, even if the 
 * result does not ultimately make a correct program while another lexical 
 * translation would.
 * 
 * This is fundamental to the way the lexer works. If there is a problem with
 * it, other tests (e.g. to test functionality of operators or identifier
 * names) would expose it quickly. Nevertheless, we test some combinations
 * here for consistency.
 *
 * @author Alan Green
 */
class Longest1 extends GroovyTestCase {

    // Increment and decrement operators
    void testPrefixIncDec() {
        def a = 20
        def b = 10
        def c = a - b
        //c = a -- b // @fail:parse 
        //c = a ++ b // @fail:parse
        //c = a +- b // @pass
        //c = a -+ b // @pass
    }
}

