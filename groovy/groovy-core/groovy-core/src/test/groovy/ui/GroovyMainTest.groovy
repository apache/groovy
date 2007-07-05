package groovy.ui

class GroovyMainTest extends GroovyTestCase {
    private baos = new ByteArrayOutputStream()
    private ps = new PrintStream(baos)

    void testHelp() {
        String[] args = ['-h']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('usage: groovy')
        ['-a', '-c', '-d', '-e', '-h', '-i', '-l', '-n', '-p', '-v'].each{
            assert out.contains(it)
        }
    }

    void testVersion() {
        String[] args = ['-v']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Groovy Version:')
        assert out.contains('JVM:')
    }

    void testNoArgs() {
        String[] args = []
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: neither -e or filename provided')
    }

    void testAttemptToRunJavaFile() {
        String[] args = ['abc.java']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: error: cannot compile file with .java extension: abc.java')
    }
}