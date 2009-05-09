import gls.CompilableTestSupport

class InnerClassTest extends CompilableTestSupport {
  void testStaticInnerClass() {
    assertScript """
        class A {
            static class B{} 
        }
        def x = new A.B()
        assert x!=null
    """
  }
  /*
  void testNonStaticInnerClass() {
    shouldNotCompile """
        class A {
            class B{} 
        }
        def x = new A.B()
    """
  }*/
  
  void testAnonymousInnerClass() {
    assertScript """
        class Foo {}
         
        def x = new Foo(){
          def bar(){1}
        }
        assert x.bar() == 1
    """
  }
  
  void testLocalVariable() {
    assertScript """
        class Foo {}
        final val = 2 
        def x = new Foo(){
          def bar(){val}
        }
        assert x.bar() == val
        assert x.bar() == 2
    """
  }
  
  void testConstructor() {
    shouldNotCompile """
        class Foo {}
        def x = new Foo() {
            Foo(){}
        }
    """
  }
  /*
  void testUsageOfOuterField() {
    assertScript """
        interface Run {
          def run(){}
        }
        class Foo {
           private x = 1
           def foo() {
               def runner = new Run(){
                  def run() {return x}
               }
               runner.run()
           }
           void x(y){x=y}
        }
        def foo = new Foo()
        assert foo.foo() == 1
        foo.x(2)
        assert foo.foo() == 2
    """
  }
  
  void testUsageOfOuterFieldOverriden() {
    assertScript """
        interface Run {
          def run(){}
        }
        class Foo {
           private x = 1
           def foo() {
               def runner = new Run(){
                  def run() {return x}
               }
               runner.run()
           }
           void setX(y){x=y}
        }
        class Bar extends Foo {
           def x = "string"
        }
        def bar = new Bar()
        assert bar.foo() == 1
        bar.x(2)
        assert bar.foo() == 2
        bar.x = "new string"
        assert bar.foo() == 2
    """
  }
  
  void testUsageOfOuterMethod() {
    assertScript """
        interface Run {
          def run(){}
        }
        class Foo {
           private x(){1}
           def foo() {
               def runner = new Run(){
                  def run() {return x()}
               }
               runner.run()
           }
        }
        def foo = new Foo()
        assert foo.foo() == 1
    """
  }
  
  void testUsageOfOuterMethodOverriden() {
    assertScript """
        interface Run {
          def run(){}
        }
        class Foo {
           private x(){1}
           def foo() {
               def runner = new Run(){
                  def run() {return x()}
               }
               runner.run()
           }
        }
        class Bar extends Foo{
          def x(){2}
        }
        def bar = new Bar()
        assert bar.foo() == 1
    """
  }*/
} 
