package groovy.bug

/**
 *  A test case to ensure that Groovy can compile class names and variable names with non-ASCII
 *  characters and that non-ASCII characters in Strings do the right thing.
 *
 *  @suthor Russel Winder
 *  @version $LastChangedRevision$ $LastChangedDate$
 */
class Groøvy965_Bug extends GroovyTestCase {
  void testUnicodeNamesAndStrings ( ) {
    def âøñè = 'âøñè'
    assertEquals ( 'âøñè' , âøñè )
  }
}
