package groovy
/**
 * check that groovy Socket methods do their job.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class SocketTest extends GroovyTestCase {
    def mySocket
    
    void setUp() {
        mySocket = new MockSocket()
    }
    
    void testSocketAppendBytes() {
        def myBytes = "mooky".getBytes()
                  
        mySocket << myBytes
                  
        def result = mySocket.outputStream.toByteArray()
        assert result != null
        assert Arrays.equals(myBytes,result)
    }
    void testSocketAppendTwoByteArrays() {
        def myBytes1 = "foo".getBytes()
        def myBytes2 = "bar".getBytes()
                  
        mySocket << myBytes1 << myBytes2
                  
        def result = mySocket.outputStream.toByteArray()
        assert result != null
        assert result.size() == myBytes1.size() + myBytes2.size()          
    }
    
    void testSocketAppend() {
        mySocket << "mooky"
        assert "mooky" == mySocket.outputStream.toString()
    }
    
    void testSocketWithStreamsClosure() {
        mySocket.withStreams {i,o->
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
    private def i
    private def o

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
