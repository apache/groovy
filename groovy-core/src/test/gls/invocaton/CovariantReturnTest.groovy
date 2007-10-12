package gls.invocaton

import gls.scope.CompilableTestSupport

public class CovariantReturnTest extends CompilableTestSupport {

  void testCovariantReturn() {
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
  
  void testCovariantReturnOverwritingAbstractMethod() {
    assertScript """
       abstract class Numeric {
         abstract Numeric eval();
       }

       class Rational extends Numeric {
         Rational eval() {this}
       }
     
       assert Rational.declaredMethods.findAll{it.name=="eval"}.size()==2    
    """
  }
}