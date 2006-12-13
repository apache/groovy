

public class OldPropertySynatxRemovalTest extends gls.CompilableTestSupport {
  
  void testMultipleParameters() {
    shouldNotCompile """
       class C {
         @Property foo
       }
    """
  }
}