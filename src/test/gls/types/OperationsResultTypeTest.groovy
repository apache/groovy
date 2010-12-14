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
}