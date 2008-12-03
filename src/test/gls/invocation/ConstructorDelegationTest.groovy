package gls.invocation

import gls.scope.CompilableTestSupport

public class ConstructorDelegationTest extends CompilableTestSupport {

  public void testThisCallWithParameter() {
    assertScript """
       class A {
         def foo
         A(String x){foo=x}
         A(){this("bar")}
       }
       def a = new A()
       assert a.foo == "bar"
    """   
  }
  
  public void testThisCallWithoutParameter() {
    assertScript """
       class A {
         def foo
         A(String x){this(); foo=x}
         A(){foo="bar"}
       }
       def a = new A("foo")
       assert a.foo == "foo"
    """   
  }

  public void testThisConstructorCallNotOnFirstStmt() {
	  shouldNotCompile """
          class ThisConstructorCall {
              public ThisConstructorCall() {
                  println 'dummy first statement'
                  this(19)
              }
              public ThisConstructorCall(int b) {
                  println 'another dummy statement'
              }
          }
          1
      """
  }

  public void testSuperConstructorCallNotOnFirstStmt() {
	  shouldNotCompile """
          class SuperConstructorCall {
              public SuperConstructorCall() {
                  println 'dummy first statement'
                  super()
              }
          }
          1
      """
  }
}