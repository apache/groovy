package gls.invocation

import gls.CompilableTestSupport

public class StaticMethodInvocationTest extends CompilableTestSupport {

    void testDifferentCalls() {
        // GROOVY-2409
        assertScript """
class Test { 
  // all errors go away if method is declared non-private 
  private static foo() {} 
  
  static callFooFromStaticMethod() { 
    Test.foo()         
    foo()              
    this.foo()         
    new Test().foo()   
  } 
  
  def callFooFromInstanceMethod() { 
    Test.foo()        
    foo()             
    this.foo()        
    new Test().foo()  
  } 
} 

Test.callFooFromStaticMethod() 
new Test().callFooFromInstanceMethod()          
        """
    }
}
