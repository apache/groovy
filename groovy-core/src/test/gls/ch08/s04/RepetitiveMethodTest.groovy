package gls.ch08.s04

import gls.scope.CompilableTestSupport

class RepetitiveMethodTest extends CompilableTestSupport{

  void testRepetitiveMethod() {
    def text ="""
		class A  {
			void foo() {}
			void foo() {}
		}
	"""
	shouldNotCompile(text)
  }
}