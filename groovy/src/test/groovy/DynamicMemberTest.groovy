package groovy

class DynamicMemberTest extends GroovyTestCase {
  def aTestMethod(o){o}
  def aProperty
  
  public void testGStringMethodCall(){
    def name = "aTestMethod"
    assert this."$name"(1) == 1
    assert this."${name}"(2) == 2
    assert "$name"(3) == 3
    assert "${name}"(4) == 4
    name = "TestMethod"
    assert this.("a"+"TestMethod")(5) == 5
    assert this.("a"+name)(6) == 6
  }
  
  public void testGStringPropertyAccess(){
    def name = "aProperty"
    this.aProperty = "foo"
    assert this."$name" == "foo"
    assert this."${name}" == "foo"
    assert "$name" == "aProperty"
    assert "${name}" == "aProperty"
  }
  
  public void testStringMethodCallAndAttributeAccess() {
    this.aProperty = "String"
    assert this."aProperty" == "String"
    assert this."aTestMethod"("String") == "String"
    assert "aTestMethod"("String") == "String"
  }
  
  public void testDynamicAttributeAccess() {
    this.aProperty = "tada"
    def name = "aProperty"
    assert this.@"$name" == "tada"
    assert this.@"${name}" == "tada"
  }
  
  public void testDynamicMethodClosure() {
    def cl = this.&"aTestMethod"
    assert cl("String") == "String"
    def name ="aTestMethod"
    cl = this.&"$name"
    assert cl("String") == "String"
  }
  
}