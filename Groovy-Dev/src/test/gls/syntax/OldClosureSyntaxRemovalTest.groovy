package gls.syntax

public class OldClosureSyntaxRemovalTest extends gls.CompilableTestSupport {
  def a = 2
  def b = 3
  
  void testOneParameter(){
    def newClosure = {a -> a}
    def oldClosure = {a|b}
    assert newClosure(1)==1
    assert oldClosure.getMaximumNumberOfParameters() == 1
    // the old closure would have cimply returned b
    // after removal this is the logic or
    assert oldClosure(1)==(a|b)
  }
  
  void testMultipleParameters() {
    shouldNotCompile """
       c = {a,b|a+b}
    """
    shouldCompile   """
       c = { a,b -> a+b }
    """
  }
}