class HelloWorld extends GroovyTestCase {
  String bar = "there"

  void testCase() {
    println "Hello $bar"
  }
}