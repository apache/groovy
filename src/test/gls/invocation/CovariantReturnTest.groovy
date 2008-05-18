package gls.invocation

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
  
  void testCovariantReturnOverwritingObjectMethod() {
    shouldNotCompile """
      class X {
        Long toString() { 333L } 
        String hashCode() { "hash" }
      }
    """
  }
  
  void testCovariantOverwritingMethodWithPrimitives() {
    assertScript """
      class Base {
         Object foo(boolean i) {i}
      }
      class Child extends Base {
         String foo(boolean i) {""+super.foo(i)}
      }
      def x = new Child()
      assert x.foo(true) == "true"
      assert x.foo(false) == "false"
    """
  }

  void testCovariantOverwritingMethodWithInterface() {
    assertScript """
      interface Base {
        List foo()
        Base baz()
      }
      interface Child extends Base {
        ArrayList foo()
        Child baz()
      }
      class GroovyChildImpl implements Child {
        ArrayList foo() {}
        GroovyChildImpl baz() {}
      }

      def x = new GroovyChildImpl()
      x.foo()
      x.baz()
    """
  }

  void testCovariantOverwritingMethodWithInterfaceAndInheritance() {
    assertScript """
      interface Base {
        List foo()
        List bar()
        Base baz()
      }
      interface Child extends Base {
        ArrayList foo()
      }
      class MyArrayList extends ArrayList { }
      class GroovyChildImpl implements Child {
        MyArrayList foo() {}
        MyArrayList bar() {}
        GroovyChildImpl baz() {}
      }
      def x = new GroovyChildImpl()
      x.foo()
      x.bar()
      x.baz()
    """
  }

}
