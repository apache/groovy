package groovy.transform

import groovy.mock.interceptor.StubFor
import org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation

/**
 * Test for @ThreadInterrupt.
 *
 * @author Hamlet D'Arcy
 */
class ThreadInterruptTest extends GroovyTestCase {
    @Override protected void tearDown() {
        Thread.metaClass = null
    }


    public void testDefaultParameters_Method() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() { }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 1 == counter.interruptedCheckCount
    }

    public void testNoMethodCheck_Method() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt(checkOnMethodStart = false)
            class MyClass {
              def myMethod() { }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            c.newInstance().myMethod()
        }
        // no exception means success
    }

    public void testDefaultParameters_ForLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  for (int i in (1..99)) {
                      // do something
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    public void testDefaultParameters_WhileLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  int x = 99
                  while (x > 0) {
                      x--
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    public void testDefaultParameters_Closure() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    public void testInterrupt_Method_AndTestExceptionMessage() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() { }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            def message = shouldFail(InterruptedException) { c.newInstance().myMethod() }
            assert message == 'Execution interrupted. The current thread has been interrupted.'
        }
    }

    public void testInterrupt_ForLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  for (int i in (1..99)) {
                      // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    public void testInterrupt_WhileLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  int x = 99
                  while (x > 0) {
                      x--
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    public void testInterrupt_Closure() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    public void testInterrupt_ClosureWithCustomExceptionType() {

        def c = new GroovyClassLoader(this.class.classLoader).parseClass("""
            @groovy.transform.ThreadInterrupt(thrown=groovy.transform.CustomException)
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(CustomException) { c.newInstance().myMethod() }
        }
    }

    public void testEntireCompileUnitIsAffected() {

        def script = '''
            def scriptMethod() {
                // this method should inherit the checks from the annotation defined later
            }

            @groovy.transform.ThreadInterrupt
            class MyClass {

              def myMethod() {
                // this method should also be guarded
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 3 is once for run(), once for scriptMethod() and once for myMethod()
        assert 3 == counter.interruptedCheckCount
    }

    public void testOnlyScriptAffected() {

        def script = '''
            @groovy.transform.ThreadInterrupt(applyToAllClasses = false)
            def scriptMethod() {
                // should be affected
            }

            class MyClass {
              def myMethod() {
                // should not be affected
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 2 is once for run() and once for scriptMethod()
        assert 2 == counter.interruptedCheckCount
    }

    public void testAnnotationOnImport() {

        def script = '''
            @groovy.transform.ThreadInterrupt
            import java.lang.String

            3.times {
                // should be affected
            }
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 2 is once for run() and once for scriptMethod()
        assert 4 == counter.interruptedCheckCount
    }

    public void testOnlyClassAffected() {

        def script = '''
            def scriptMethod() {
                // this should not be affected
            }

            @groovy.transform.ThreadInterrupt(applyToAllClasses = false)
            class MyClass {
              def myMethod() {
                // this should be affected
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell(ThreadInterruptibleASTTransformation.getClassLoader()).evaluate(script)
        }
        // 1 is once for myMethod()
        assert 1 == counter.interruptedCheckCount
    }

    public void testThreadInterruptOnAbstractClass() {
        def script = '''
            @groovy.transform.ThreadInterrupt
            abstract class MyAbstractClass {
                abstract void myMethod()
            }

            class Concrete extends MyAbstractClass {
                void myMethod() {
                    99.times {
                        // do something
                    }
                }
            }

            new Concrete().myMethod()
        '''
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { new GroovyShell(ThreadInterruptibleASTTransformation.getClassLoader()).evaluate(script) }
        }

    }
}

class InterruptingThread extends Thread {
    @Override
    boolean isInterrupted() {
        true
    }
}

class CountingThread extends Thread {
    def interruptedCheckCount = 0

    @Override
    boolean isInterrupted() {
        interruptedCheckCount++
        false
    }

}

class CustomException extends Exception {

    public CustomException(final String message) {
        super(message)
    }
}