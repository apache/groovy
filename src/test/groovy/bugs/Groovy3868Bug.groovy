package groovy.bugs

class Groovy3868Bug extends GroovyTestCase {
    void testAsTypeCallWithPrimitiveType() {
        callAndcheckResults(Long)
        callAndcheckResults(Integer)
        callAndcheckResults(Short)
        callAndcheckResults(Byte)
        callAndcheckResults(Character)
        callAndcheckResults(Double)
        callAndcheckResults(Float)
    }
    def callAndcheckResults(klazz) {
        def num = "1"
        def result = num.asType(klazz.TYPE) // get the primitive type of this class
        
        if(klazz == Character) num = num as char // Character.valueOf(String) is not there
        
        assert result == klazz.valueOf(num)
        assert result.class == klazz 
    }
}
