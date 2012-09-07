package gls.types

public class OperationsResultTypeTest extends gls.CompilableTestSupport {
  
  void testDeclaredTypeIsKept() {
      assertScript """
          def list = [10,20,30]
          Iterator it = list.iterator()
          try {
              it++
              assert false
          } catch  (ClassCastException) {
              assert true
          }
      """
  }

  void testDeclaredTypeIsKeptForSharedVariable() {
    assertScript '''
        float myFloat = 40f
        myFloat -= 20f
        assert myFloat.class == Float
        println "$myFloat"
        (0..1).each { i ->
            assert myFloat.class == Float
        }
    '''
  }
}
