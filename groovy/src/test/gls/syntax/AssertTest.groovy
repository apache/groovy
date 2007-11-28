package gls.syntax

public class AssertTest extends gls.CompilableTestSupport {
  
  void testAssignment() {
    // don't allow "=" here, it most certainly must be a "=="
    shouldNotCompile """
       def a = 1
       assert a = 2
    """
  }
}