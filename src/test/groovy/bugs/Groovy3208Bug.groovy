package groovy.bugs

public class Groovy3208Bug extends GroovyTestCase {

   void testBug () {
      new Sub().each { it.doIt() }
   }
}

class Super {
   static final String PROP = "ABC"
   def doIt = { PROP }
}

class Sub extends Super {
}
