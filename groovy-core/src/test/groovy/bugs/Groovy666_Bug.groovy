package groovy.bugs

/**
 *  @author Russel Winder
 *  @version $Revision$
 */ 
class Groovy666_Bug extends GroovyTestCase {
  void testRunScript() {
    (new GroovyShell ()).evaluate("x = 1")
  }
}
