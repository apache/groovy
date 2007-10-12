// Do not remove this line: it is used in test below
package groovy

/**
 * Test to ensure that readLine() method works on Reader/InputStream
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Joachim Baumann
 * @version $Revision$
 */

class ReadLineTest extends GroovyTestCase {
    def file
    void setUp() {
        file = new File("src/test/groovy/ReadLineTest.groovy")
    }
    void testReadOneLineFromReader() {
        def line
        file.withReader() {line = it.readLine()}
        assert line == "// Do not remove this line: it is used in test below"
    }
    
    static testString = " ä\n ö\n\n ü\r\n 5\r\r 7\n\r 9"
    static expectedLines = [ " ä", " ö", "", " ü", " 5", "", " 7", "", " 9" ]
    static String[] expectedLinesSlow = [ " ä", " ö", " ü", " 5", " 7" ]
    static int[] expectedChars = [' ', '9', -1];

    void readFromReader(Reader reader) throws IOException {
        expectedLines.each { expected ->
            def line = reader.readLine()
            assertEquals("Readline should return correct line", expected, line)
        }
        assertNull("Readline should return null", reader.readLine())
    }

    public void testBufferedReader() throws IOException {        
        Reader reader = new BufferedReader(new StringReader(testString))
        readFromReader(reader)
    }

    public void testReaderSupportingMark() throws IOException {        
        Reader reader = new StringReader(testString)
        readFromReader(reader)
    }

    /*
     * In this case we cannot read more than one line separator
     * Thus empty lines can be returned if line separation is \r\n.
     */
    public void testReaderSlow() throws IOException {        
        Reader reader = new SlowStringReader(testString);
        expectedLinesSlow.each { expected ->
            String line = reader.readLine()
            while(line != null && line.length() == 0) {
                line = reader.readLine()
            } 
            assertEquals("Readline should return correct line", expected, line);    
        }
        assertEquals("Readline should return empty string", "", reader.readLine());

        expectedChars.each { expected ->
            assertEquals("Remaining characters incorrect", expected, reader.read())        
        }
        assertNull(reader.readLine());
    }
}
class SlowStringReader extends StringReader {
    public SlowStringReader(String s) { super(s); }
    public boolean markSupported() { return false }
}
