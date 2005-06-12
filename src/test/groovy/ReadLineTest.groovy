import java.io.File

/**
 * Test to ensure that readLine() method works on Reader/InputStream
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
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
        assert line == "import java.io.File"
    }
    
    void testReadOneLineFromInputStream() {
        def line
        file.withInputStream() {line = it.readLine()}
        assert line == "import java.io.File"
    }
}
