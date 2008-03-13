package groovy.lang

class InnerClassResolvingTest extends GroovyTestCase {
    public void testInnerClass() {
        // Thread.UncaughtExceptionHandler was added in Java 1.5
        if (System.properties.'java.version'[2] >= '5') {
            def script = '''
                def caught = false
                def t = Thread.start {
                    Thread.setDefaultUncaughtExceptionHandler(
                        {thread,ex -> caught=true} as Thread.UncaughtExceptionHandler)
                    throw new Exception("huhu")
                }
                t.join()
                assert caught==true
            '''
            new GroovyShell().evaluate(script)
        }
    }

    public void testInnerClassWithPartialMatchOnImport() {
        if (System.properties.'java.version'[2] >= '5') {
            def script = '''
                import java.lang.Thread as X
                X.UncaughtExceptionHandler y = null
            '''
            new GroovyShell().evaluate(script)
        }
    }


}
