package groovy.bugs

class Groovy325_Bug extends GroovyTestCase {
  static boolean staticMethod() {
    return true
  }

  void testCallStaticMethodFromClosure() {
    def c = { staticMethod() }
    assert c()
  }
}