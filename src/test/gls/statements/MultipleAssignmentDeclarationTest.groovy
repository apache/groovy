package gls.statements

import gls.CompilableTestSupport

class MultipleAssignmentDeclarationTest extends CompilableTestSupport {

  void testDef() {
    assertScript """
      def (a,b) = [1,2]
      assert a==1
      assert b==2
    """
  }
  
  void testDefWithoutLiteral() {
    def list = [1, 2]
    def (c, d) = list
    assert c==1
    assert d==2
  }
  
  void testMixedTypes() {
    assertScript """
      def x = "foo"
      def (int i, String j) = [1,x]
      assert x == "foo"
      assert i == 1
      assert i instanceof Integer
      assert j == "foo"
      assert j instanceof String
    """
  }
  
  void testMixedTypesWithConversion() {
    assertScript '''
      def x = "foo"
      def (int i, String j) = [1,"$x $x"]
      assert x == "foo"
      assert i == 1
      assert i instanceof Integer
      assert j == "foo foo"
      assert j instanceof String
    '''
  }
  
  void testDeclarationOrder() {
    assertScript """
      try {
        def (i,j) = [1,i]
        assert false
      } catch (MissingPropertyException mpe) {
        assert true
      }
    """
  }
  
  void testNestedScope() {
    assertScript """
       def c = {
         def (i,j) = [1,2]
         assert i==1
         assert j==2
       }
       c()
       
       try {
         println i
         assert false
       } catch (MissingPropertyException mpe) {
         assert true
       }
       
       try {
         println j
         assert false
       } catch (MissingPropertyException mpe) {
         assert true
       }
              
       def (i,j) = [2,3]
       assert i==2
       assert j==3
       c()
       
       assert i==2
       assert j==3
    """   
  }
}
