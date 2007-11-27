package gls.statements

import gls.scope.CompilableTestSupport

public class ReturnTest extends CompilableTestSupport {

  public void testObjectInitializer() {
      shouldNotCompile """
         class A {
            {return}
         }      
      """
  }
  
  public void testStaticInitializer() {
      assertScript """
         class A {
             static foo=2
             static { return; foo=1 }
         }
         assert A.foo==2
      """      
  }
}