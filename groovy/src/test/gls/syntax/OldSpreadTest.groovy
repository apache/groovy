package gls.syntax

public class OldSpreadTest extends gls.CompilableTestSupport {
  
  void testSpreadStatement() {
    // don't allow spread outside a method call
    shouldNotCompile """
       *x       
    """
  }
}