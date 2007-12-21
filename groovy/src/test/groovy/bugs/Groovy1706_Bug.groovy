package groovy.bugs

class Groovy1706_Bug extends GroovyTestCase {
   void testStaticMethodIsCalledFromSubclass() {
      // disclaimer: static methods shouldn't be
      // called on instances
      Groovy1706A a = new Groovy1706A()
      Groovy1706B b = new Groovy1706B()
      assert "A" == a.doit()
      assert "B" == b.doit()
   }

   void testStaticMethodIsCalledInCorrectInstance() {
      // disclaimer: static methods shouldn't be
      // called on instances
      Groovy1706A i = new Groovy1706B()
      assert "B" == i.doit()
      // in Java the answer would be "A"
   }
}

class Groovy1706A { static doit() { "A" } }
class Groovy1706B extends Groovy1706A { static doit() { "B" } }
