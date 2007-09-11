package gls.syntax

public class AssertTest extends gls.CompilableTestSupport {
  
  void testSpreadStatement() {
    // don't allow spread outside a method call
    shouldNotCompile """
       *x       
    """
  }
}