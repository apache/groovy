package gls.ch03.s10;
/**
 * JLS: 3.10.7
 * The null type has one value, the null reference, represented by the literal
 * null, which is formed from ASCII characters. A null literal is always of the
 * null type.  
 *
 *
 * Test implementation notes:
 * Can show that the null literal exists, however the null type has no name
 * and can be implicitly converted to any reference type, so it is hard to 
 * tell if null is of the null type. 
 */
class NullLiteral extends GroovyTestCase {

    void testNull() {
        // Null literal exists
        String s = null
        assert s == null

        try {
            print s.length()
            fail("Should have thrown exception")
        } catch (NullPointerException e) {
            // OK
        }
    }

}
