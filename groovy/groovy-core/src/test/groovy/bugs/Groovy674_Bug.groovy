package groovy.bugs

/**
 *  @author Russel Winder
 *  @version $Revision$
 */ 
class Groovy674_Bug extends GroovyTestCase {
  void testRunScript() {
    (new GroovyShell ()).evaluate("[ 1, 2, 3, 4, 5 ].each { test | println() }")
  }
}
