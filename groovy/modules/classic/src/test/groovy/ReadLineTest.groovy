import java.io.File

/**
 * Test to ensure that readLine() method works on Reader/InputStream
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class ReadLineTest extends GroovyTestCase {
    property file
    void setUp() {
        file = new File("src/test/groovy/ReadLineTest.groovy")
    }
    void testReadOneLineFromReader() {
        file.withReader() {line = it.readLine()}
        assert line == "import java.io.File"
    }
    
    void testReadOneLineFromInputStream() {
        file.withInputStream() {line = it.readLine()}
        assert line == "import java.io.File"
    }
}
