package gls.statements

import gls.CompilableTestSupport

public class DeclarationTest extends CompilableTestSupport {

  
  public void testSingleDeclarationInParenthesis() {
      shouldNotCompile """
         (def a=1).method()
      """      
      shouldCompile """
         (a=1).method()
      """
  }
  
  public void testNullAssignmentToPrimitive() {
      shouldFail (org.codehaus.groovy.runtime.typehandling.GroovyCastException, """
          int x = null
      """)
      assertScript """
          Integer x = null
      """
  }
  
  public void testNullAssignmentToPrimitiveForSharedVariable() {
      shouldFail(org.codehaus.groovy.runtime.typehandling.GroovyCastException, """
          int i = null
          def c = {i}
      """)
  }
}