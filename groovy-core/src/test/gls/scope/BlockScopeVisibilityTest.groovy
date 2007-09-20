package gls.scope

class BlockScopeVisibilityTest extends CompilableTestSupport {

  public void testForLoopVariableNotVisibleOutside() {
 	
  	assertScript("""
  	  i=1
  	  for (i in [1,2]) {}
  	  assert i==1
  	""")
  }

}