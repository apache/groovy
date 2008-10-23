package gls.syntax

public class MethodCallValidationTest extends gls.CompilableTestSupport {
  
  void testDeclarationInMethodCall() {
    shouldNotCompile """
       foo(String a)
    """
  }
}