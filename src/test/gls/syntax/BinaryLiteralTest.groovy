package gls.syntax

import gls.CompilableTestSupport

/**
 * 
 * @author Guillaume Laforge
 */
class BinaryLiteralTest extends CompilableTestSupport {

    void testSomeBinaryLiteralNumbers() {
        assertScript '''
            assert  0b0   ==  0
            assert  0b000 ==  0
            assert  0b1   ==  1
            assert  0b11  ==  3
            assert -0b11  == -3

            // uppercase B letter works too
            assert 0B101  == 0b101

            int x = 0b10101111
            assert x == 175

            byte aByte = (byte)0b00100001
            assert aByte == 33

            int anInt = (int)0b1010000101000101
            assert anInt == 41285

        '''
    }

}
