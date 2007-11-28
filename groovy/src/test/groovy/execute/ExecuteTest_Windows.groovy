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
  void testCmdEchoOneArray() {
    def process = ( [ "cmd.exe" , "/c" , "echo 1" ] as String[] ).execute()
    process.waitFor()
    assert process.in.text.trim() == "1"
  }

  void testCmdEchoOneList() {
    def process = [ "cmd.exe" , "/c" , "echo 1" ].execute ( )
    process.waitFor()
    assert process.in.text.trim() == "1"
  }

  void testCmdDate() {
    def process = "cmd.exe /c date.exe /t".execute()
    process.waitFor()
    def theDate = process.in.text.trim()
    def minLen = 8
      // dk: the length depends on the locale settings and usually contains two digits for
      //     each day/month/year where the separation char may differ. This test may fail for
      //     locales with even shorter date representations. As soon as this happens, please
      //     adapt the minLen value.
    assert theDate.size() >= minLen, "Expected '$theDate' to be at least $minLen chars long"
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