package groovy.jmx.builder.vm5

class MockManagedGroovyObject {
  int id
  String name
  def location
  boolean available

  def doSomething() {
    // do sometihng
  }

  def doSomethingElse() {
    // do something else
  }

  static descriptor = [
          name: "jmx.builder:type=EmbeddedObject"
  ]
}