package gls.sizelimits

class StringSizeTest extends gls.CompilableTestSupport {
  
  void testNormalString() {
    def string = "x"*65535
    
    assertScript """
      def test="$string"
    """
    
    shouldNotCompile """
      def test="x $string"
    """
  }
  
  void testGString() {
    def string = "x"*65534 
    // not 65535, because we use one additional space
    // in the gstring test script
    
    assertScript """
      def x = 1
      def test = "\$x $string"
    """
    shouldNotCompile """
      def x = 1
      def test = "\$x  $string"
    """
  }
}