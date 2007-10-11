package gls.invocaton

import gls.scope.CompilableTestSupport

public class CovariantReturnTest extends CompilableTestSupport {

  /**
   * This test ensures Groovy can choose a method based on interfaces.
   * Choosing such an interface should not be hidden by subclasses.
   */
  public void testMostSpecificInterface() {
    assertScript """
      class A {
        Object foo() {1}
      }
   
      class B extends A{
        String foo(){"2"}
      }
      def b = new B();
      assert b.foo()=="2"
      assert B.declaredMethods.findAll{it.name=="foo"}.size()==2
    """   
  }
  
  public void testMostGeneralForNull() {
    // we use the same signatures with different method orders,
    // because we want to catch method ordering bugs
    assertScript """
      def m(String x){1}
      def m(Integer x){2}
      assert m(null) == 1
      
      def n(Integer x){2}
      def n(String x){1}
      assert n(null) == 1
    """
  }
}