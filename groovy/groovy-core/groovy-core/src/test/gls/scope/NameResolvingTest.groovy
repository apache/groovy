package gls.scope

import gls.CompilableTestSupport

class NameResolvingTest extends CompilableTestSupport {
  public void testVariableNameEqualsToAClassName() {
	Object String = ""
	assert String == ""
	assert String.class == java.lang.String
  }
  
  public void testVariableNameEqualsCurrentClassName() {
	Object NameResolvingTest = ""
	assert NameResolvingTest == ""
	assert NameResolvingTest.class == java.lang.String.class
  }  
  
  public void testClassNoVariableInStaticMethod(){
    assertScript """
      static def foo() {
   	     Class.forName('java.lang.Integer')
      }
      assert foo() == Integer
    """
  }
  
  public void testAssignmentToNonLocalVariableWithSameNameAsClass() {
    shouldNotCompile """
      String = 1    
    """
  }
}