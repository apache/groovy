package gls.invocation

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
    
    assertScript """
      class A implements I1 {}
      class B extends A implements I2 {}
      class C extends B implements I3 {}
      interface I1 {}
      interface I2 extends I1 {}
      interface I3 extends I2 {}

      def foo(bar) {0}
      def foo(I1 bar) {1}
      def foo(I2 bar) {2}
      def foo(I3 bar) {3}

      assert foo(new A())==1
      assert foo(new B())==2
      assert foo(new C())==3
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
  
  void testMethodSelectionException() {
    assertScript """
      import org.codehaus.groovy.runtime.metaclass.MethodSelectionException as MSE
    
      def foo(int x) {}
      def foo(Number x) {}
      
      try {
        foo()
        assert false
      } catch (MSE mse) {
        assert mse.message.indexOf("foo()") >0
        assert mse.message.indexOf("#foo(int)") >0
        assert mse.message.indexOf("#foo(java.lang.Number)") >0
      }
    
    """  
  }

  void testMethodSelectionWithInterfaceVargsMethod() {
    // GROOVY-2719
    assertScript """
      public class Thing {}
      public class Subthing extends Thing {}

      def foo(Thing i) {1}
      def foo(Runnable[] l) {2}

      assert foo(new Subthing())==1
    """
  }

  void testComplexInterfaceInheritance() {
    // GROOVY-2698
    assertScript """
        import javax.swing.*

        interface ISub extends Action {}

        class Super extends AbstractAction {
        void actionPerformed(java.awt.event.ActionEvent e){}
        }
        class Sub extends AbstractAction implements ISub {
        void actionPerformed(java.awt.event.ActionEvent e){}
        }

        static String foo(Action x) { "super" }
        static String foo(ISub x) { "sub" }

        def result = [new Super(), new Sub()].collect { foo(it) }

        assert ["super","sub"] == result
    """
  }
}