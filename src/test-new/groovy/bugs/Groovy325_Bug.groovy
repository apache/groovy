class Groovy325_Bug extends GroovyTestCase {
  static staticMethod() {
    println("hello world")
  }

  static void main(args) {
    c = { staticMethod() }
    c()
  }
}