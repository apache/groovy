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
  
  public void testInAsDefAllowedInPackageNames() {
    shouldCompile """
      package as.in.def
      class X {}
    """
  }
  
  public void testAssignmentToNonLocalVariableWithSameNameAsClass() {
    shouldNotCompile """
      String = 1
    """
  }

  public void testClassUsageInSuper(){
     shouldCompile """
       class A {A(x){}}
       class B extends A {
         B(x){super(Thread)}
       }
     """   
  }
}