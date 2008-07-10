package gls.expressions

import gls.scope.CompilableTestSupport

public class DeclarationTest extends CompilableTestSupport {

  
  public void testSingleDeclarationInParenthesis() {
      shouldNotCompile """
         (def a=1).method()
      """      
      shouldCompile """
         (a=1).method()
      """
  }
  
}