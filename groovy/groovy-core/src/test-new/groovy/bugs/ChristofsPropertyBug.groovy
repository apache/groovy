/**
 * @version $Revision$
 */
class ChristofsPropertyBug extends GroovyTestCase {
     
    @Property def mixedCaseProperty

    void testChristofsPropertyBug() {
    	this.mixedCaseProperty = "test"
    	shouldFail({this.mixedcaseproperty = "test"})
    }
    
    def getMixedCaseProperty()    { mixedCaseProperty }
    def setMixedCaseProperty(val) { this.mixedCaseProperty = val }
}