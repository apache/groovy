package groovy.operator

import java.math.BigDecimal;

class BigDecimalOperatorsTest extends GroovyTestCase {

    def x, y

    void testPlus() {

        x = 0.1 + 1.1
        assert x instanceof BigDecimal;
        assert x == 1.2

        x = 3 + 2.2
        assert x == 5.2
        assert x instanceof BigDecimal;

        x = 2.2 + 4
        assert x instanceof BigDecimal;
        assert x == 6.2

        y = x + 1
        assert y instanceof BigDecimal;
        assert y == 7.2

        def z = y + x + 1 + 2
        assert z instanceof BigDecimal;
        assert z == 16.4
    }

    void testMinus() {
        x = 1.1-0.01
        assert x == 1.09

        x = 6 - 2.2
        assert x == 3.8

        x = 5.8 - 2
        assert x == 3.8

        y = x - 1
        assert y == 2.8
    }

    void testMultiply() {
        x = 3 * 2.0
        assert x == 6.0

        x = 3.0 * 2
        assert x == 6.0

        x = 3.0 * 2.0
        assert x == 6.0

        y = x * 2
        assert y == 12.0

        y = 11 * 3.333
        assert y == 36.663 , "y = " + y

        y = 3.333 * 11
        assert y == 36.663 , "y = " + y
    }

    void testDivide() {
        x = 80.0 / 4
        assert x == 20.0 , "x = " + x

        x = 80 / 4.0
        assert x == 20.0 , "x = " + x

        y = x / 2
        assert y == 10.0 , "y = " + y
        assert y == 10 , "y = " + y

        y = 34 / 3.000
        assert y == 11.3333333333 , "y = " + y

        y = 34.00000000000 / 3
        assert y == 11.33333333333 , "y = " + y
    }
    
    BigDecimal echoX ( BigDecimal x, BigDecimal y) {x}
    
    // test for Groovy-1250
    void testBigDecimalCoerce() {
        assert echoX(9.95, 1.0) == echoX(9.95, 1)
    }
    
    void testAssign() {
        BigDecimal foo
        foo = (byte) 20
        assert foo.class == BigDecimal.class
        assert foo == 20

        foo = (short) 20
        assert foo.class == BigDecimal.class
        assert foo == 20

        foo = (int) 20
        assert foo.class == BigDecimal.class
        assert foo == 20

        foo = (long) 20
        assert foo.class == BigDecimal.class
        assert foo == 20

        foo = (float) 0.5f
        assert foo.class == BigDecimal.class
        assert foo == 0.5

        foo = (double) 0.5d
        assert foo.class == BigDecimal.class
        assert foo == 0.5
        
        foo = 10G
        assert foo.class == BigDecimal.class
        assert foo == 10
        
        double d = 1000
        d *= d
        d *= d
        d *= d
        assert (long)d != d
		assert (BigDecimal) d == d
    }
}
