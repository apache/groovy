package gls.syntax

public class MethodCallValidationTest extends gls.CompilableTestSupport {
  
  void testDeclarationInMethodCall() {
    shouldNotCompile """
       foo(String a)
    """
  }
  
  void testDuplicateNamedParameters() {
	  shouldNotCompile """
		def closure = { println it }
		closure debit: 30, credit: 40, debit: 50, {}
	  """

      shouldNotCompile """
        def closure = { println it }
        closure debit: 30, credit: 40, debit: 50
      """
	  
      shouldNotCompile """
        def method(map) {
            println map
        }
        method debit: 30, credit: 40, debit: 50   
      """
  }
}