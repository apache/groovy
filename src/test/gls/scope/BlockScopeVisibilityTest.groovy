package gls.scope

class BlockScopeVisibilityTest extends CompilableTestSupport {

  public void testForLoopVariableNotVisibleOutside() {
  	assertScript("""
  	  i=1
  	  for (i in [1,2]) {}
  	  assert i==1
  	""")
  }
  
  public void testCatchParameterNotVisibleInOtherCatch() {
    shouldFail(MissingPropertyException) {    
        try {
            throw new RuntimeException("not important");
        } catch(AssertionError e) {
           // here e is defined
        } catch(Throwable t) {
           // here e should not be accessible
           println e 
        }
    }
  }

}