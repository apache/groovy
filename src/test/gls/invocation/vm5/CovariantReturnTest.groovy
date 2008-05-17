package gls.invocation.vm5

import gls.scope.CompilableTestSupport

public class CovariantReturnTest extends CompilableTestSupport {

  void testCovariantParameterType(){
    assertScript """
      class BaseA implements Comparable<BaseA> {
        int index
        public int compareTo(BaseA a){
          return index <=> a.index
        }
      }

      def a = new BaseA(index:1)
      def b = new BaseA(index:2)
      assert a < b
    """
  }
}
