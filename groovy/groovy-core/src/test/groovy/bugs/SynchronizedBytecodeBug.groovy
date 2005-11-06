/**
 * @author Guillaume Laforge
 */
class SynchronizedBytecodeBug extends GroovyTestCase {

    /**
     * Groovy's bytecode associated with syncrhonized(foo) construct used to generate invalid bytecode
     * This test method shows that the standard wait()/notify() works.
     */
    void testSynchronized() {
        Integer foo = 0

        Thread.start{
            println "sleeping for a moment"
            sleep 1000
            println "slept and synchronizing from thread"
            synchronized(foo) {
                println "notifying"
                foo.notify()
                println "notified"
            }
        }

        println "synchronizing"
        synchronized(foo) {
            println "starting to wait"
            foo.wait()
            println "waited"
        }

        // if this point is reached, the test worked :-)
        assert true
    }
}