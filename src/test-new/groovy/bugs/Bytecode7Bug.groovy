/**
 * @version $Revision$
 */
class Bytecode7Bug extends GroovyTestCase {

    void testDuplicateVariables() {
		if (true) {
			a = 123
		}
		if (true) {
			a = 456
		}
    }

    void testDuplicateVariablesInClosures() {
    		coll = [1]
    		
		coll.each {
			a = 123
		}
		coll.each {
			a = 456
		}
    }
}