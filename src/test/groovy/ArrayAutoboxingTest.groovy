package groovy

class ArrayAutoboxingTest extends GroovyTestCase {
    
    void testUnwantedAutoboxingWhenInvokingMethods() {
      def cl
      cl = blah2(new int[2*2])
      assert cl == "[I"
      cl = blah2(new long[2*2])
      assert cl == "[J"
      cl = blah2(new short[2*2])
      assert cl == "[S"
      cl = blah2(new boolean[2*2])
      assert cl == "[Z"
      cl = blah2(new char[2*2])
      assert cl == "[C"
      cl = blah2(new double[2*2])
      assert cl == "[D"
      cl = blah2(new float[2*2])
      assert cl == "[F"
    }
    
    def blah2(Object o) {
       return o.class.name
    }
        
} 