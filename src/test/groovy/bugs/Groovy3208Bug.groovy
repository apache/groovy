package groovy.bugs

public class Groovy3208Bug extends GroovyTestCase {

   void testBug () {
      new Sub().each { assertEquals("ABC", it.doIt()) }
      
      assertEquals("ABC", new Sub().doItAgain())
   }
   
//    void testSubclassStaticContextProperty() {
//       assert "ABC" == Sub.doItStatically()
//       assert "ABC" == Sub.doItStaticallyAgain()
//    }
}

class Super {
   static final String PROP = "ABC"
   def doIt = { PROP }
//   static doItStatically = { PROP }
}

class Sub extends Super {
   String doItAgain() { PROP }
//   static String doItStaticallyAgain() { PROP }
}
