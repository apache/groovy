package gls.scope

class BlockScopeVisibilityTest extends CompilableTestSupport {

  public void testForLoopVaribaleNotVisibleOutside() {
  	shouldNotCompile("""
  	   class ForLoopTest {
  	     def testme() {
  	       for (i in [1,2]) {
  	       }
  	       println i
  	     }
  	   }
  	""")
  	
  	assertScript("""
  	  i=1
  	  for (i in [1,2]) {}
  	  assert i==1
  	""")
  }

}