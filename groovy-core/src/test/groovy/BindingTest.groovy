package groovy

class BindingTest extends GroovyTestCase {

    void testProperties() {
    	def b = new Binding()
    	b.setVariable("foo", 123)
    	
    	assert b.foo == 123
    	
    	b.bar = 456
    	
    	assert b.getVariable("bar") == 456
    	assert b["bar"] == 456
    	
    	b["a.b.c"] = 'abc'
    	
    	assert b.getVariable("a.b.c") == 'abc'
    	assert b["a.b.c"] == 'abc'
    }
}
