import java.io.File
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
