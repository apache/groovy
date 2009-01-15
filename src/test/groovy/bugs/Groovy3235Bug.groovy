package groovy.bugs

public class Groovy3235Bug extends GroovyTestCase {

void testBug3235 () {
      def d = """This is one line.

      That was an empty line.
      Another empty line follows.

      All these lines should be written.
"""
      def f = File.createTempFile("groovy.bugs.Groovy3235Bug", ".txt")
      
      f.withWriter { w ->
          d.eachLine { w.println it }
      }

      def t = f.text

      assert d == t.normalize()
      
      assert d.denormalize() == t
   }
}
