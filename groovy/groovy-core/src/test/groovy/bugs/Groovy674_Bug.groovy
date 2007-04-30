package groovy.bugs

/**
 *  Test to ensure all the right exceptions are thrown for all the right/wrong combinations of
 *  parentheses and no parameters for print and println.
 *
 *  @author Russel Winder
 *  @version $Revision$
 */
class Groovy674_Bug extends GroovyTestCase {
  void testTopLevelPrintParenthesesNoParameter ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "print ( )" ) }
    catch ( GroovyRuntimeException gre ) { return }
    fail ( "Should have thrown GroovyRuntimeException" ) ;
  }
  void testTopLevelPrintlnParenthesesNoParameter ( ) {
    assertScript ( "println ( )" )
  }
  void testClosurePrintParenthesesNoParameter ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { print ( ) }" ) }
    catch ( GroovyRuntimeException gre ) { return }
    fail ( "Should have thrown GroovyRuntimeException" ) ;
  }
  void testClosurePrintlnParenthesesNoParameter ( ) {
    assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println ( ) }" )
  }
  void testTopLevelPrintNoParenthesesParameter ( ) { assertScript ( "print ( '' )" ) }
  void testTopLevelPrintlnNoParenthesesParameter ( ) { assertScript ( "println ( '' )" ) }
  void testClosurePrintNoParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { print ( '' ) }" ) }
  void testClosurePrintlnNoParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println ( '' ) }" ) }
  void testTopLevelPrintParenthesesParameter ( ) { assertScript ( "print ''" ) }
  void testTopLevelPrintlnParenthesesParameter ( ) { assertScript ( "println ''" ) }
  void testClosurePrintParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { print '' }" ) }
  void testClosurePrintlnParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println '' }" ) }
  void testTopLevelPrintProperty ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "print" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  void testTopLevelPrintlnProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "println" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  void testInClosurePrintProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { print }" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  void testInClosurePrintlnProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { println }" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
}
