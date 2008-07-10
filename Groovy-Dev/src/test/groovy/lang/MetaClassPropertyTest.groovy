package groovy.lang

class MetaClassPropertyTest extends GroovyTestCase {
	void testForJavaClass() {
		def foo = "hello world"
		
		def metaClass = foo.metaClass
		assertEquals String, metaClass.theClass
		assert String.metaClass instanceof ExpandoMetaClass
	}                                          
	
	void testForGroovyClass() {
		 def t = new MCPTest1()
		
		assertEquals MCPTest1, t.metaClass.theClass        
		assert MCPTest1.metaClass instanceof org.codehaus.groovy.runtime.HandleMetaClass
	}
}
class MCPTest1 {	
}