package groovy.bugs

/**
 * @version $Revision: 1.4 $
 */
class ChristofsPropertyBug extends GroovyTestCase {
     
    def mixedCaseProperty

    void testChristofsPropertyBug() {
    	this.mixedCaseProperty = "test"
    	shouldFail({this.mixedcaseproperty = "test"})
    }
    
    def getMixedCaseProperty()    { mixedCaseProperty }
    void setMixedCaseProperty(val) { this.mixedCaseProperty = val }
}