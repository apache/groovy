package groovy

class ClassTest extends GroovyTestCase {

    void testClassExpression() {
    	def c = String.class
    	println c
    	assert c instanceof Class
    	assert c.name == "java.lang.String" , c.name
    	
    	c = GroovyTestCase.class
    	println c
    	assert c instanceof Class
    	assert c.name.endsWith("GroovyTestCase") , c.name
    	
    	c = ClassTest.class
    	println c
    	assert c instanceof Class
    	assert c.name.endsWith("ClassTest") , c.name
    }

}