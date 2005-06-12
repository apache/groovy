class Groovy325_Bug extends GroovyTestCase {
  static void staticMethod() {
    println("hello world")
  }

  static void main(args) {
    def c = { staticMethod() }
    c()
  }
}