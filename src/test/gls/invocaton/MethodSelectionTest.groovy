package gls.invocaton

import gls.scope.CompilableTestSupport

public class MethodSelectionTest extends CompilableTestSupport {

  /**
   * This test ensures Groovy can choose a method based on interfaces.
   * Choosing such an interface should not be hidden by subclasses.
   */
  public void testMostSpecificInterface() {
    assertScript """
      interface A{}
      interface B extends A{}
      class C implements B,A{}
      class D extends C{}
      class E implements B{}

      def m(A a){1}
      def m(B b){2}

      assert m(new D()) == 2
      assert m(new C()) == 2
      assert m(new E()) == 2
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