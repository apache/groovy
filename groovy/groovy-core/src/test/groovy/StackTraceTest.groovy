/**
* This test case is added to ensure an exception thrown from inside
* groovy does always contain a valid line number and file name for
* the script method the exception is thrown from.
*
* See also GROOVY-726
*/
class StackTraceTest extends GroovyTestCase {


	public void testTrace() {
		def className = this.class.name
		def assertDone = false	
		try {
		  throw new Exception("e")
		} catch (Exception e) {
		  assert e.message == "e"
		  def trace = e.stackTrace
		  trace.each {
		    if (it.className == className && it.methodName == "testTrace") {
		      assert it.lineNumber>0
		      assert it.fileName != null
		      assert it.fileName.length() > 0
		      assertDone = true
		    }
		  }
		}
		assert assertDone==true
	}

}