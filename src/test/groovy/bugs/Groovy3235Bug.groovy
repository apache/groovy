package groovy.bugs

public class Groovy3235Bug extends GroovyTestCase {

   void testBug () {
      def d = """This is one line.
      This is another.
      All these lines should be written.
"""
      def f = File.createTempFile("groovy.bugs.Groovy3235Bug", ".txt")
      
      f.withWriter { w ->
          d.eachLine { w.println it }
      }

      assert d.readLines() == f.readLines()
   }
}
