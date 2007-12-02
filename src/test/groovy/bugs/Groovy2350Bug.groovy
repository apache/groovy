package groovy.bugs

class Groovy2350Bug extends GroovyTestCase{

     void testNoArg () {
         shouldFail (org.codehaus.groovy.runtime.metaclass.MethodSelectionException) {
             def a = new DefaultNoArgCtor()
             println a
         }

         assertEquals "NULL", new DefaultNoArgCtor2().value
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
