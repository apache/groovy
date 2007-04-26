package groovy.lang;

class InnerClassResolvingTest extends GroovyTestCase {
  public void testInnerClass() {
    def caught = false
    def t = Thread.start {
	  Thread.setDefaultUncaughtExceptionHandler(
        {thread,ex -> caught=true} as Thread.UncaughtExceptionHandler)
      throw new Exception("huhu")
    }
    t.join()
    assert caught==true
  }
}
    