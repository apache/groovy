package gls.syntax

public class OldPropertySyntaxRemovalTest extends gls.CompilableTestSupport {
  
  void testMultipleParameters() {
    shouldNotCompile """
       class C {
         @Property foo
       }
    """
  }
}