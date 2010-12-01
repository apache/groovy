package groovy.transform

import groovy.mock.interceptor.StubFor
import java.util.concurrent.TimeoutException
import org.codehaus.groovy.control.MultipleCompilationErrorsException

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

            @TimedInterrupt(value = 5L, unit = TimeUnit.HOURS)
            def myMethod() { }
        ''')

        def system = new StubFor(System)

        // start time initialized to the Long of the Beast
        system.demand.nanoTime() { 666L }

        def instance
        system.use {
            instance = c.newInstance()
        }
        assert instance.TimedInterrupt$expireTime == 18000000000666 //5 days in future

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
            assert e.contains('Execution timed out after 5 units')
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
}
