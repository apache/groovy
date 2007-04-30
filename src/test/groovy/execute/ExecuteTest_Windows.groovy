package groovy.execute

/**
 *  Test to ensure that the execute mechanism works fine on Windows systems.
 *
 *  <p>These test are a bit trivial but at least they are here :-)</p>
 *
 *  @author Paul King
 *  @version $Revision: 6165 $
 */
class ExecuteTest_Windows extends GroovyTestCase {
  void testShellEchoOneArray() {
    def process = ( [ "cmd.exe" , "/c" , "echo 1" ] as String[] ).execute()
    process.waitFor()
    assert process.in.getText().trim() == "1"
  }
  void testShellEchoOneList() {
    def process = [ "cmd.exe" , "/c" , "echo 1" ].execute ( )
    process.waitFor()
    assert process.in.getText().trim() == "1"
  }
  void testShellDate() {
    def process = "cmd.exe /c date.exe /t".execute()
    process.waitFor()
    assert process.in.getText().trim().size() > 12
  }
  void testEchoOneArray() {
    try {
      def process = ( [ "echo 1" ] as String[] ).execute()
      process.waitFor()
      fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }
  void testEchoOneList() {
    try {
      def process = [ "echo 1" ].execute()
      process.waitFor()
      fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }
}