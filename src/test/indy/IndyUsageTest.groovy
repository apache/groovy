class IndyUsageTest extends GroovyTestCase {
  void testIndyIsUsedNested() {
    assertScript """
      def foo(){
         throw new Exception("blah")
      }
      try {
        foo()
      } catch (Exception e) {
        assert e.stackTrace[1].className == "org.codehaus.groovy.vmplugin.v7.IndyInterface"
      }
    """
  }
}
