package gls.ch03.s10;
/**
 * GLS 3.10.1
 *
 * Integer literals
 * -- explicitly integer if ends with 'i' type specifier
 *    (can also create integers in other ways too)
 * -- come in Decimal, Octal and Hex varieties
 * -- represented in 32 bits, two's complement
 */
class IntLit extends GroovyTestCase {

    void testDecInt() {
        assertEquals(2i, (1i + 1i))
        assertEquals(0i, (1i - 1i))

        // Play games with most significant bit
        assertEquals((-2147483647i - 1), (2147483647i + 1))

        // Large positive literals
        //def a = 2147483647i //@pass
        //def a = 2147483648i //@fail:parse
        //def a = 2147483649i //@fail:parse

        // Large negative literals
        //def a = -2147483647i //@pass
        //def a = -2147483648i //@pass
        //def a = -2147483649i //@fail:parse

        assertEquals(99i, 99I)
    }

    void testHexInt() {
        // Check literals get correct values
        assertEquals(0, 0x0i)
        assertEquals(1, 0x1i)
        assertEquals(2, 0x2i)
        assertEquals(3, 0x3i)
        assertEquals(4, 0x4i)
        assertEquals(5, 0x5i)
        assertEquals(6, 0x6i)
        assertEquals(7, 0x7i)
        assertEquals(8, 0x8i)
        assertEquals(9, 0x09i)
        assertEquals(10, 0xai)
        assertEquals(11, 0xbi)
        assertEquals(12, 0xci)
        assertEquals(13, 0xdi)
        assertEquals(14, 0xei)
        assertEquals(15, 0xfi)
        assertEquals(16, 0x10i)

        assertEquals(65535i, 0xffffi)
        assertEquals(10140894i, 0x9abcdei)
        assertEquals(11259375i, 0xabcdefi)
        assertEquals(305419896i, 0x12345678i)
        assertEquals(2147483647i, 0x7fffffffi)

        // extra zeroes after the 'x' should not affect result
        assertEquals(1, 0x01i)
        assertEquals(1, 0x000001i)
        assertEquals(1, 0x0000000000000000000000000001i)

        //def a = 0x7fffffffi//@pass
        //def a = 0x80000000i//@fail:parse
        //def a = 0x80000001i//@fail:parse

        //def a = -0x7fffffffi//@pass
        //def a = -0x80000000i//@pass
        //def a = -0x80000001i//@fail:parse

        // Check that we handle the unary '-' as a separate token
        //def a = - 0x80000000i//@pass
        //def a = -/* */0x80000000i//@pass
        //def a = - 0x80000001i//@fail:parse
        //def a = - 0x80000001i//@fail:parse
        //def a = -/* */0x80000001i//@fail:parse
    }

    void testOctInt() {
        // Check literals get correct values
        assertEquals(0i, 00)
        assertEquals(1i, 01i)
        assertEquals(2i, 02i)
        assertEquals(3i, 03i)
        assertEquals(4i, 04i)
        assertEquals(5i, 05i)
        assertEquals(6i, 06i)
        assertEquals(7i, 07i)

        assertEquals(65535, 0177777i)
        assertEquals(10140894, 046536336)
        assertEquals(11259375, 052746757i)
        assertEquals(305419896, 02215053170)
        assertEquals(2147483647, 017777777777i)

        // extra zeroes should not affect result
        assertEquals(1, 001i)
        assertEquals(1, 0000001i)
        assertEquals(1, 00000000000000000000000000001i)

        //def a = 017777777777i//@pass
        //def a = 020000000000i//@fail
        //def a = 020000000001i//@fail

        //def a = -017777777777i//@pass
        //def a = -020000000000i//@pass
        //def a = -020000000001i//@fail


        //def a = - 020000000000i//@pass
        //def a = -/* */020000000000i//@pass
        //def a = - 020000000001i//@fail
        //def a = -/* */020000000001i//@fail
    }
}
