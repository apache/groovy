package gls.ch03.s10;
/**
 * JLS: 3.10.3
 * The boolean type has two values, represented by the literals true and false,
 * formed from ASCII letters.  A boolean literal is always of type boolean.
 */
class BooleanLiterals extends GroovyTestCase {

    void testBool() {
        // Try some simple things with literals
        assert (true != false)
        assert true
        assert !false

        // boolean results must be equal to a boolean literal
        assert (1 > 2) == false
        assert (3 < 4) == true
    }

}
