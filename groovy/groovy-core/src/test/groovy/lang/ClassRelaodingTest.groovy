package groovy.lang

class ClassRelaodingTest extends GroovyTestCase {

	public void testRealoding() {
		def cl = new GroovyClassLoader(this.class.classLoader);
		def currentDir = new File(".").getAbsolutePath() 
		cl.addClasspath(currentDir)
		
		def file = new File("TestReload.groovy")
		assert !file.exists()
		
		file.write """
		  class TestReload {
		    @Property hello = "hello"
		  }
		  """
		def groovyClass = cl.loadClass("TestReload",true,false)
		def object = groovyClass.newInstance()
		assert "hello"== object.hello
					
		// change class
		file.write """
		  class TestReload {
		    @Property hello = "goodbye"
		  }
		  """
		
		// reload		
		groovyClass = cl.loadClass("TestReload",true,false)
		object  = groovyClass.newInstance()
		assert "goodbye" == object.hello
		file.delete()
	}
}