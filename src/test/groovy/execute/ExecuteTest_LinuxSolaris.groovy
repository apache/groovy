#! /usr/bin/env groovy 

package groovy.execute

/**
 * Test to ensure that the execute mechanism works fine on *nix-like systems.  For these OSs we
 * can effectively guarantee the existence of some programs that we can run.  Assume the search
 * path is partway reasonable so we can access sh and echo.
 * <p>
 * These test are a bit trivial but at least they are here :-)
 *
 * @author Russel Winder
 */
class ExecuteTest_LinuxSolaris extends GroovyTestCase {
  void testShellEchoOneArray ( ) {
    def process = ( [ "sh" , "-c" , "echo 1" ] as String[] ).execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }
  void testShellEchoOneList ( ) {
    def process = [ "sh" , "-c" , "echo 1" ].execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }
  void testEchoOneArray ( ) {
    try {
      def process = ( [ "echo 1" ] as String[] ).execute ( )
      process.waitFor ( )
       fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }
  void testEchoOneList ( ) {
    try {
      def process = [ "echo 1" ].execute ( )
      process.waitFor ( )
       fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }
  void testEchoOneScalar ( ) {
    def process = "echo 1".execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }
  void testEchoArray ( ) {
    def process = ( [ "echo" , "1" ] as String[] ).execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
   }
  void testEchoList ( ) {
    def process = [ "echo" , "1" ].execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
   }
}
