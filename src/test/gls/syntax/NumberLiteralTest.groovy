package gls.syntax

public class NumberLiteralTest extends gls.CompilableTestSupport {

    void testLargeNumbersOverflowing() {
        shouldCompile '''
            int a = 0x80000000
            a = 0x80000000i

            long longNumber = 0x8000000000000000L
            longNumber = 0x8000_0000_0000_0000L
            longNumber = 010_0000_0000_0000_0000_0000L
            longNumber = 0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000L
        '''
    }

}