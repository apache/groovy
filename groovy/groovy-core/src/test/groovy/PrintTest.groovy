class PrintTest extends GroovyTestCase {

    void testPrint() {
        assertConsoleOutput("hello", "'hello'")
        
        assertConsoleOutput([], "[]")
        assertConsoleOutput([1, 2, 'hello'], "[1, 2, 'hello']")
        
        assertConsoleOutput([1:20, 2:40, 3:'cheese'], "[1:20, 2:40, 3:'cheese']")
        assertConsoleOutput([:], "[:]")
	}
}