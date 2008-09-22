package groovy.bugs

/**
 *  @author Russel Winder
 *  @version $Revision$
 */ 
class Groovy666_Bug extends GroovyShellTestCase {
  void testRunScript() {
    evaluate("x = 1")
  }
}
