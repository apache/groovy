class IntegerOperationTest extends GroovyTestCase {

    def x
    def y
    
    void testPlus() {
        x = 2 + 2
        assert x == 4
        
        y = x + 1
        assert y == 5

        z = y + x + 1 + 2
        assert z == 12
    }
    
    void testCharacterPlus() {
        Character c1 = 1
        Character c2 = 2

        x = c2 + 2
        assert x == 4

        x = 2 + c2
        assert x == 4

        x = c2 + c2
        assert x == 4
          
        y = x + c1
        assert y == 5
          
        y = c1 + x
        assert y == 5

        z = y + x + c1 + 2
        assert z == 12

        z = y + x + 1 + c2
        assert z == 12

        z = y + x + c1 + c2
        assert z == 12
    }
    
    void testMinus() {
        x = 6 - 2
        assert x == 4
        
        y = x - 1
        assert y == 3
    }
    
    void testCharacterMinus() {
        Character c1 = 1
        Character c2 = 2
        Character c6 = 6

        x = c6 - 2
        assert x == 4

        x = 6 - c2
        assert x == 4

        x = c6 - c2
        assert x == 4
        
        y = x - c1
        assert y == 3
    }
    
    void testMultiply() {
        x = 3 * 2
        assert x == 6
        
        y = x * 2
        assert y == 12        
    }
    
    void testDivide() {
        x = 80 / 4
        assert x == 20.0 , "x = " + x
        
        y = x / 2
        assert y == 10.0 , "y = " + y
    }
    
    void testIntegerDivide() {
        x = 52 \ 3
        assert x == 17 , "x = " + x
        
        y = x \ 2
        assert y == 8 , "y = " + y 
        
        y = 11
        y \= 3
        assert y == 3       
    }
    
    void testMod() {
        x = 100 % 3

        assert x == 1

        y = 11
        y %= 3
        assert y == 2
    }
    
    void testAnd() {
        /** @todo parser
        x = 1 & 3

        assert x == 1
        */

        x = 1.and(3)

        assert x == 1
    }
     
     void testOr() {
         /** @todo parser
         x = 1 | 3

         assert x == 3

         x = 1 | 4

         assert x == 5
         */

         x = 1.or(3)

         assert x == 3

         x = 1.or(4)

         assert x ==5
    }
    
    void testShiftOperators() {

        x = 8 >> 1
        assertTrue(x == 4)
        assertTrue(x instanceof Integer)

        x = 8 << 2
        assertTrue(x == 32)
        assertTrue(x instanceof Integer)

        x = 8L << 2
        assertTrue(x == 32)
        assertTrue(x instanceof Long)

        x = -16 >> 4
        assertTrue(x == -1)

        x = -16 >>> 4
        assertTrue(x == 0xFFFFFFF)

        //Ensure that the type of the right operand (shift distance) is ignored when calculating the
        //result.  This is how java works, and for these operators, it makes sense to keep that behavior.
        x = Integer.MAX_VALUE << 1L
        assertTrue(x == -2)
        assertTrue(x instanceof Integer)

        x = new Long(Integer.MAX_VALUE).longValue() << 1
        assertTrue(x == 0xfffffffe)
        assertTrue(x instanceof Long)

        //The left operand (shift value) must be an integral type
        try {
            x = 8.0F >> 2
            fail("Should catch UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }

        //The right operand (shift distance) must be an integral type
        try {
            x = 8 >> 2.0
            fail("Should catch UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        }
    }
}
