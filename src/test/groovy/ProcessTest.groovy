/**
 * check that groovy Process methods do their job.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import java.io.*

class ProcessTest extends GroovyTestCase {
    property myProcess
    
    void setUp() {
        myProcess = new MockProcess()
    }
    
    void testProcessAppend() {
        myProcess << "mooky"
        assert "mooky" == myProcess.outputStream.toString()
    }
    
    void testProcessInputStream() {
        assert myProcess.in instanceof InputStream
        assert myProcess.in != null
    }
    
    void testProcessText() {
        assert "" == myProcess.text
    }
    
    void testProcessErrorStream() {
        assert myProcess.err instanceof InputStream
        assert myProcess.err != null
    }
    
    void testProcessOutputStream() {
        assert myProcess.out instanceof OutputStream
        assert myProcess.out != null
    }
    
    // @todo - ps.waitForOrKill(secs) creates it's own thread, leave this out of test suite for now...
    
    void tearDown() {
        myProcess.destroy()
    }
}

/**
 * simple Process, used purely for test cases
 */
class MockProcess extends Process {
    private e
    private i
    private o
    public MockProcess() {
        e = new AnotherMockInputStream()
        i = new AnotherMockInputStream()
        o = new ByteArrayOutputStream()
    }
    void destroy() {}
    int exitValue() { return 0 }
    InputStream getErrorStream() { return e }
    public InputStream getInputStream() { return i }
    public OutputStream getOutputStream() { return o }
    int waitFor() { return 0 }
}

/**
 * only needed for workaround in groovy, 
 *     new ByteArrayInputStream(myByteArray) doesn't work at mo... (28-Sep-2004)
 */
class AnotherMockInputStream extends InputStream {
    int read() { return -1 }
}
