/**
 * @version $Revision: 1.0 $
 */
class ChristofsPropertyBug extends GroovyTestCase {
     
    void testChristofsPropertyBug() {
    	this.mixedCaseProperty = "test"
    	shouldFail({this.mixedcaseproperty = "test"})
    }
    
    mixedCaseProperty
	
    getMixedCaseProperty()    { mixedCaseProperty }
    setMixedCaseProperty(val) { this.mixedCaseProperty = val }
}