package groovy
/**
 * check that text def is available on...
 *
 * myFile.text,  myFile.text(charset),  
 * myURL.text,  myURL.text(charset),
 * myInputStream.text,  myInputStream.text(charset),
 * myReader.text,
 * myBufferedReader.text,
 * myProcess.text
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class TextPropertyTest extends GroovyTestCase {
    def myReader
    def myInputStream
    def myBigEndianEncodedInputStream
    
    void setUp() {
        myReader = new StringReader("digestive")
        myInputStream = new ByteArrayInputStream("chocolate chip".bytes)
        myBigEndianEncodedInputStream = new ByteArrayInputStream("shortbread".getBytes("UTF-16BE"))
    }
    
    void testBigEndianEncodedInputStreamText() {
        assert "shortbread" == myBigEndianEncodedInputStream.getText("UTF-16BE")
    }
    
    void testInputStreamText() {
        assert "chocolate chip" == myInputStream.text
    }
    
    void testReaderText() {
        assert "digestive" == myReader.text
    }
    
    void tearDown() {
        myInputStream = null
        myReader = null
    }
}
