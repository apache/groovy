import gls.CompilableTestSupport

class GroovyObjectInheritanceTest extends CompilableTestSupport {
  void testInheritanceWithGetProperty() {
    assertScript """
        class Foo {
          def getProperty(String name) {1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        assert bar.foo==1
    """
  }
  
  void testInheritanceWithSetProperty() {
    assertScript """
        class Foo {
          def foo
          void setProperty(String name, x) {this.foo=1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        bar.foo = 2
        assert bar.foo == 1
    """
  }
  
  void testInheritanceWithInvokeMethod() {
    assertScript """
        class Foo {
          def invokeMethod(String name, args) {1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        assert bar.foo() == 1
    """
  }
}