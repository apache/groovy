package groovy

/**
 * @version $Revision$
 */
class CompareTypesTest extends GroovyTestCase { 
    void testCompareByteToInt() { 
        Byte a = 12
        Integer b = 10
        
        assert a instanceof Byte
        assert b instanceof Integer
        
        assert a > b
    } 
    
    void testCompareByteToDouble() { 
        Byte a = 12
        Double b = 10
        
        assert a instanceof Byte
        assert b instanceof Double
        
        assert a > b
    } 
     
    void testCompareLongToDouble() { 
        Long a = 12
        Double b = 10
        
        assert a instanceof Long
        assert b instanceof Double
        
        assert a > b
    } 
     
    void testCompareLongToByte() { 
        Long a = 12
        Byte b = 10
        
        assert a instanceof Long
        assert b instanceof Byte
        
        assert a > b
    } 
     
    void testCompareIntegerToByte() { 
        Integer a = 12
        Byte b = 10
        
        assert a instanceof Integer
        assert b instanceof Byte
        
        assert a > b
    }
    
    void testCompareCharToLong() {
        def a = Integer.MAX_VALUE
        def b = ((long) a)+1
        a=(char) a
        
        assert a instanceof Character
        assert b instanceof Long
        
        assert a < b
    }
    
    void testCompareCharToInteger() {
        Character a = Integer.MAX_VALUE
        Integer b = a-1
        
        assert a instanceof Character
        assert b instanceof Integer
        
        assert a > b
    } 
} 


