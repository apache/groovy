package org.codehaus.groovy.tools

class UtilitiesTest extends GroovyTestCase {

  void testValidJavaIdentifiers() {
    assert Utilities.isJavaIdentifier("abc")
    assert Utilities.isJavaIdentifier("\$abc")
    assert Utilities.isJavaIdentifier("_a_b_c")
    assert Utilities.isJavaIdentifier("abc1")
  }

  void testInvalidJavaIdentifiers() {
    assert !Utilities.isJavaIdentifier("")
    assert !Utilities.isJavaIdentifier("a b c")
    assert !Utilities.isJavaIdentifier("a,b,c")
    assert !Utilities.isJavaIdentifier("abc!")
    assert !Utilities.isJavaIdentifier("abc?")
    assert !Utilities.isJavaIdentifier("1abc")
    assert !Utilities.isJavaIdentifier("abc()")
  }
}