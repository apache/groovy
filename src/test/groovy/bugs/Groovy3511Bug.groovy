package groovy.bugs

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation

class Groovy3511Bug extends GroovyTestCase {
    final SHOULD_HAVE_FAILED = "The conversion above should have failed"
    void testExceptionMessageStringToNumberConversion() {
        try {
            Double test = "Hello" 
            fail(SHOULD_HAVE_FAILED)
        } catch (ex) {
            verifyExceptionMsg(ex, Double.class.name)
        }
        try {
            Float test = "Hello" 
            fail(SHOULD_HAVE_FAILED)
        } catch (ex) {
            verifyExceptionMsg(ex, Float.class.name)
        }
        try {
            DefaultTypeTransformation.castToNumber("Hello", Long.class) 
            fail(SHOULD_HAVE_FAILED)
        } catch (ex) {
            verifyExceptionMsg(ex, Long.class.name)
        }
        try {
            DefaultTypeTransformation.castToNumber("Hello") 
            fail(SHOULD_HAVE_FAILED)
        } catch (ex) {
            verifyExceptionMsg(ex, Number.class.name)
        }
    }
    
    def verifyExceptionMsg(ex, className) {
        assertTrue ex.message.contains(className)       
    }
}
