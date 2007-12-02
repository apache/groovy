package groovy.bugs

class Groovy2351Bug extends GroovyTestCase {
   void testVarArgs () {

       def a = new VarArgs()
       assertEquals( "method with Integer", a.method(1, 2, 3, 4, 5))
       assertEquals( "method with Objects", a.method("", 2, "22", 4, 5))
   }
}

class VarArgs {
    def method(Object... args) { "method with Objects" }

    def method(Integer... args) { "method with Integer" }
}
