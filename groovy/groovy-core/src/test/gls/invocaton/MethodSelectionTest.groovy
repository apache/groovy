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
}