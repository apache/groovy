package gls.types

public class BooleanExpressionConversionTest extends gls.CompilableTestSupport {
    void testInt() {
        assertScript """
            boolean foo(int i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testLong() {
        assertScript """
            boolean foo(long i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testFloat() {
        assertScript """
            boolean foo(float i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testDouble() {
        assertScript """
            boolean foo(double i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testChar() {
        assertScript """
            boolean foo(char i){
                if (i) return true
                return false
            }
            assert !foo((char)0)
            assert foo((char)1)
            assert foo((char)256)
        """
    }
    
    void testByte() {
        assertScript """
            boolean foo(byte i){
                if (i) return true
                return false
            }
            assert !foo((byte)0)
            assert foo((byte)1)
            assert !foo((byte)256)
        """
    }
    
    void testShort() {
        assertScript """
            boolean foo(short i){
                if (i) return true
                return false
            }
            assert !foo((short)0)
            assert foo((short)1)
            assert foo((short)256)
        """
    }
    
}