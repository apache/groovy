/**
 * check that groovy Socket methods do their job.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import java.io.*
import java.net.*

class SocketTest extends GroovyTestCase {
    @Property mySocket
    
    void setUp() {
        mySocket = new MockSocket()
    }
    
    void testSocketAppendBytes() {
        myBytes = "mooky".getBytes()
                  
        mySocket << myBytes
                  
        result = mySocket.outputStream.toByteArray()          
        assert result != null
        assert Arrays.equals(myBytes,result)
    }
    void testSocketAppendTwoByteArrays() {
        myBytes1 = "foo".getBytes()
        myBytes2 = "bar".getBytes()
                  
        mySocket << myBytes1 << myBytes2
                  
        result = mySocket.outputStream.toByteArray()
        assert result != null
        assert result.size() == myBytes1.size() + myBytes2.size()          
    }
    
    void testSocketAppend() {
        mySocket << "mooky"
        assert "mooky" == mySocket.outputStream.toString()
    }
    
    void testSocketWithStreamsClosure() {
        mySocket.withStreams {i,o|
            assert i instanceof InputStream
            assert i != null
                      
            assert o instanceof OutputStream          
            assert o != null          
        }
    }
    
    void tearDown() {
        mySocket.close()
    }
}

/**
 * simple, unconnected Socket, used purely for test cases
 */
class MockSocket extends Socket {
    private i
    private o
    public MockSocket() {
        i = new MockInputStream()
        o = new ByteArrayOutputStream()
    }
    public InputStream getInputStream() { return i }
    public OutputStream getOutputStream() { return o }
}

/**
 * only needed for workaround in groovy, 
 *     new ByteArrayInputStream(myByteArray) doesn't work at mo... (28-Sep-2004)
 */
class MockInputStream extends InputStream {
    int read() { return -1 }
}
