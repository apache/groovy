class Simple {
  doSomething() {
    data = ["name": "James", "location": "London"]
    for (e in data) {
      println("entry ${e.key} is ${e.value}")
    }
  }
  
  closureExample(collection) {
    collection.each { println("value ${it}") }
  }
  
  static void main(args) {
    values = [1, 2, 3, "abc", "moo"]
    foo = new Simple()
    foo.closureExample(values)
    foo.doSomething()
  }
}
