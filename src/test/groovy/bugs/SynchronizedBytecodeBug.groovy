package groovy.bugs

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
  
  /* more tests to ensure a monitor exit is done at the right place */
    
  void testBreakInSynchronized() {
    Object lock = new Object()
    while (true) {
	    synchronized(lock) {
    		break
    	}
    }
    checkNotHavingAMonitor(lock)
  }
  
  void testContinueInSynchronized() {
    Object lock = new Object()  
    boolean b = true
    while (b) {
	    synchronized(lock) {
	        b = false
    		continue
    	}
    }
    checkNotHavingAMonitor(lock)
  }
  
  void testReturnInSynchronized() {
    Object lock = new Object()  
    methodWithReturn(lock)
    checkNotHavingAMonitor(lock)
  }
  
  def methodWithReturn(lock) {
    synchronized (lock) {
      return
    }
  }
  
  void testBreakInClosureWithSynchronized() {
    Object lock = new Object()
    def c = {
      while (true) {
	      synchronized(lock) {
    	    break
    	  }
      }
      checkNotHavingAMonitor(lock)
    }
    c()
    checkNotHavingAMonitor(lock)
  }
  
  void testContinueInClosureWithSynchronized() {
    Object lock = new Object()
    def c = {
      boolean b = true
      while (b) {
	      synchronized(lock) {
    	    b = false
    	    continue
    	  }
      }
      checkNotHavingAMonitor(lock)
    }
    c()
    checkNotHavingAMonitor(lock)
  }
  
  def checkNotHavingAMonitor(Object lock){
    // if we call notify* or wait without having the
    // monitor, we get an exception.
    try {
      lock.notifyAll()
      assert false,"should have no monitor!"
    } catch (IllegalMonitorStateException imse) {
      assert true
    }
  }
}