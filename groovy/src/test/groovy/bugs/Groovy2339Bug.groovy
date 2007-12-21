package groovy.bugs

class Groovy2339Bug extends GroovyTestCase {

    void testBug () {
        List list = ['groovy', 'java']
        Map map = [a: 1, b: 2]
        
		shouldFail (MissingMethodException) {
	        list.each {
	            map.keySet().each {Date d ->
	                println d
	            }
	        }
        }
    }
}