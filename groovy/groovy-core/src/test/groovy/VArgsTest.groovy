class VArgsTest extends GroovyTestCase {

  def primitiveMethod(){0}
  def primitiveMethod(int i) {1}  
  def primitiveMethod(int i, int j) {2}
  def primitiveMethod(int[] is) {10+is.length}
  
  void testPrimitiveMethod() {
    assert primitiveMethod()==0
    assert primitiveMethod(1)==1
    assert primitiveMethod(1,1)==2
    assert primitiveMethod(1,1,1)==13 
    // TODO: the following test case prudces a ClassCastException [Ljava.lang.Integer;
    // but only when executed as part of the test run
    //assert primitiveMethod([1,2,2,2] as int[])==14
  }
  
  def objectMethod(){0}
  def objectMethod(Object i) {1}  
  def objectMethod(Object i, Object j) {2}
  def objectMethod(Object[] is) {10+is.length}
  
  void testObjectMethod() {
    assert objectMethod()==0
    assert objectMethod(1)==1
    assert objectMethod(1,1)==2
    assert objectMethod(1,1,1)==13
    assert objectMethod([1,2,2,2] as Object[])==14
  }
  
  def gstringMethod(GString[] gstrings){gstrings.length}
  
  void testGStringVargsMethod() {
    def content = 1
    def gstring ="$content"
    assert gstringMethod() == 0
    assert gstringMethod(gstring) == 1
    assert gstringMethod(gstring,gstring,gstring) == 3
    assert gstringMethod([gstring] as GString[]) == 1
  }
  
  def stringMethod(String[] strings) {strings.length}
  
  void testStringMethod() {
    def content = 1
    def gstring ="$content"
    assert stringMethod() == 0
    assert stringMethod(gstring) == 1
    assert stringMethod(gstring,gstring,gstring) == 3
    assert stringMethod([gstring] as GString[]) == 1
    assert stringMethod() == 0
    assert stringMethod("a") == 1
    assert stringMethod("a","a","a") == 3
    assert stringMethod(["a"] as String[]) == 1
  }
}