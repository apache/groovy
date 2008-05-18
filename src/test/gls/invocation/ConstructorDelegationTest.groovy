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

}