package groovy.transform

import groovy.mock.interceptor.StubFor
import java.util.concurrent.TimeoutException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.transform.TimedInterruptibleASTTransformation

/**
 * Test for TimedInterrupt.
 */
class TimedInterruptTest extends GroovyTestCase {

    public void testClassMethodIsVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 1L)
            class MyClass {
              def myMethod() { }
            }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.myMethod()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.myMethod()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }

    public void testClassMethodIsVisitedAndCustomExceptionThrown() {

        def c = new GroovyClassLoader(this.class.classLoader).parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(thrown=groovy.transform.CustomException,value = 1L)
            class MyClass {
              def myMethod() { }
            }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.myMethod()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(CustomException) {
                instance.myMethod()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }


    public void testScriptMethodIsVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 1L)
            def myMethod() { }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.myMethod()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.myMethod()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }


    public void testStaticMethodIsNotVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 1L)
            class MyClass {
              static def myMethod() { }
            }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            instance.myMethod()
        }
    }

    public void testClosureFieldIsVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 1L)
            class MyClass {
              def myMethod = { }
            }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.myMethod()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.myMethod()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }

    public void testClosureInScriptIsVisited_CheckOnMethodStartIsFalse() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(checkOnMethodStart = false, value = 1L)
            def myMethod = {

            }
            myMethod()
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.run()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.run()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }

    public void testWhileInScriptIsVisited_CheckOnMethodStartIsFalse() {

        def c = new GroovyClassLoader().parseClass('''
            @TimedInterrupt(checkOnMethodStart = false, value = 1L)
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            int x = 1
            while (x < 2) { x = 2 }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.run()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.run()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }

    public void testForInScriptIsVisited_CheckOnMethodStartIsFalse() {

        def c = new GroovyClassLoader().parseClass('''
            @TimedInterrupt(checkOnMethodStart = false, value = 1L)
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            def x = [1]
            for (def o : x) { o++ }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 1000000666L //one second in future

        system.demand.nanoTime() { 1000000666L }
        system.use {
            instance.run()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.run()
            }
            assert e.contains('Execution timed out after 1 units')
        }
    }

    public void testStaticClosureFieldNotVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 1L)
            class MyClass {
              static def myMethod = { }
            }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 1000000667L }
        system.use {
            instance.myMethod()
        }
    }

    public void testAnnotationParameters() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.TimedInterrupt
            import java.util.concurrent.TimeUnit

            @TimedInterrupt(value = 18000000L, unit = TimeUnit.MILLISECONDS)
            def myMethod() { }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 18000000000666 //5 hours in future

        system.demand.nanoTime() { 18000000000666L }
        system.use {
            instance.myMethod()
        }

        // one nanosecond later, but still in the neighborhood of the beast
        system.demand.nanoTime() { 18000000000667L }
        system.use {
            def e = shouldFail(TimeoutException) {
                instance.myMethod()
            }
            assert e.contains('Execution timed out after 18000000 units')
        }
    }

    public void testErrorHandling() {
        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = "5")
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = foo())
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, applyToAllClasses = 5)
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, applyToAllClasses = foo())
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, checkOnMethodStart = 5)
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, checkOnMethodStart = foo())
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, unit = 5)
                def myMethod() { }
            ''')
        }

        shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TimedInterrupt
                @TimedInterrupt(value = 5L, unit = foo())
                def myMethod() { }
            ''')
        }
    }

    public void testTimedInterruptOnAbstractClass() {
        def script = '''
            @groovy.transform.TimedInterrupt(value = 1L)
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

            new Concrete()
        '''

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime(4) { 666L } // 4 times to cover full instantiation

        system.use {
            def instance = new GroovyShell(TimedInterruptibleASTTransformation.getClassLoader()).evaluate(script)
            assert instance.TimedInterrupt$expireTime == 1000000666L //5 hours in future
            system.demand.nanoTime() { 1000000667L }

            shouldFail(TimeoutException) {
                instance.myMethod()
            }
        }

    }

}
