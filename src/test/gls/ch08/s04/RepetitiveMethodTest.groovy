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

  void testRepetitiveMethodsCreationForBooleanProperties() {
      shouldCompile """
          class BoolTest {
              boolean success
              boolean isSuccess() {
                  return success
              }
          }
      """
  }
}