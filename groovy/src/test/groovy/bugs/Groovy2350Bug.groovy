package groovy.bugs

class Groovy2350Bug extends GroovyTestCase{

     void testNoArg () {
         shouldFail (org.codehaus.groovy.runtime.metaclass.MethodSelectionException) {
             def a = new DefaultNoArgCtor()
             println a
         }

         assertEquals "NULL", new DefaultNoArgCtor2().value
     }

     void testNoDefCtor () {
         def a = new NoDefaultCtor("first")
         assertEquals "toS: first", a.toString()

         def b = new NoDefaultCtor()
         assertEquals "toS: null", b.toString()
     }
}

class NoDefaultCtor {
    def field

    NoDefaultCtor(param) { field= param }

    String toString() {
      return "toS: ${field}"
    }
}


class DefaultNoArgCtor {
  DefaultNoArgCtor(String s) {}

  DefaultNoArgCtor(int s) {}
}

class DefaultNoArgCtor2 {
  String value

  DefaultNoArgCtor2(String s) {
      value = s ? s : "NULL"
  }
}
