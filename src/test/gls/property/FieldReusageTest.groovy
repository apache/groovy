package gls.property

import gls.CompilableTestSupport

class FieldReusage extends CompilableTestSupport {

  void testPropertyField() {
    shouldCompile """
      class A {
        def foo
        private foo
      }
    """
  }
  
  void testFieldProperty() {
    shouldCompile """
      class A {
        private foo
        def foo
      }
    """
  }
  
  void testFieldPropertyProperty() {
    shouldNotCompile """
      class A {
        private foo
        def foo
        def foo
      }
    """
  }
  
  void testPropertyFieldField() {
    shouldNotCompile """
      class A {
        def foo
        private foo
        private foo
      }
    """
  }
} 