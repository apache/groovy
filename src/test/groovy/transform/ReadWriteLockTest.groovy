package groovy.transform

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.lang.reflect.Modifier

/**
 * Unit test for WithReadLock and WithWriteLock annotations.
 *
 * @author Hamlet D'Arcy
 */
class ReadWriteLockTest extends GroovyTestCase {

    public void testLockFieldDefaultsForReadLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithReadLock
            public void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$reentrantlock')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert !Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    public void testLockFieldDefaultsForWriteLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithWriteLock
            public void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$reentrantlock')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert !Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    public void testLockFieldDefaultsForStaticReadLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithReadLock
            public static void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$REENTRANTLOCK')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    public void testLockFieldDefaultsForStaticWriteLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithWriteLock
            public static void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$REENTRANTLOCK')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    public void testLocking() {

        def tester = new MyClass()
        tester.readerMethod1()
        tester.readerMethod2()
        assert tester.readerMethod1Called
        assert tester.readerMethod2Called

        tester.writerMethod1()
        tester.writerMethod2()
        assert tester.writerMethod1Called
        assert tester.writerMethod2Called

        tester.writerMethod2()
        tester.writerMethod1()
        tester.readerMethod2()
        tester.readerMethod1()
    }

    public void testStaticLocking() {

        def tester = new MyClass()
        tester.staticReaderMethod1()
        tester.staticReaderMethod2()
        assert tester.staticReaderMethod1Called
        assert tester.staticReaderMethod2Called

        tester.staticWriterMethod1()
        tester.staticWriterMethod2()
        assert tester.staticWriterMethod1Called
        assert tester.staticWriterMethod2Called

        tester.staticWriterMethod2()
        tester.staticWriterMethod1()
        tester.staticReaderMethod2()
        tester.staticReaderMethod1()
    }

    public void testDeadlockingDoesNotOccur() {
        def tester = new MyClass()

        // this tests for deadlocks from not releaseing in finally block 
        shouldFail { tester.namedReaderMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedWriterMethod2() }

        shouldFail { tester.namedWriterMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedReaderMethod1() }
    }

    public void testCompileError_NamingConflict() {
        shouldFail("lock field with name 'unknown' not found") {
            '''
            class MyClass {
                @groovy.transform.WithWriteLock('unknown')
                public static void readerMethod1() { }
            } '''
        }

        shouldFail("lock field with name 'myLock' should be static") {
            '''
            class MyClass {
                def myLock = new java.util.concurrent.locks.ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public static void readerMethod1() { }
            } '''
        }

        shouldFail("lock field with name 'myLock' should not be static") {
            '''
            class MyClass {
                static def myLock = new java.util.concurrent.locks.ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public void readerMethod1() { }
            } '''
        }
    }

    def shouldFail(String expectedText, Closure c) {
        String script = c()
        try {
            new GroovyClassLoader().parseClass(script)
            fail('Failure Expected')
        } catch (Exception e) {
            assert e.getMessage().contains(expectedText)
        }
    }
}

class MyClass {

    def readerMethod1Called = false
    def readerMethod2Called = false
    def writerMethod1Called = false
    def writerMethod2Called = false
    def staticReaderMethod1Called = false
    def staticReaderMethod2Called = false
    def staticWriterMethod1Called = false
    def staticWriterMethod2Called = false
    def myLock = new ReentrantReadWriteLock()
    
    @groovy.transform.WithReadLock
    public void readerMethod1() {
        readerMethod1Called = true
    }
    @groovy.transform.WithReadLock
    public void readerMethod2() {
        readerMethod2Called = true
    }
    @groovy.transform.WithWriteLock
    public void writerMethod1() {
        writerMethod1Called = true
    }
    @groovy.transform.WithWriteLock
    public void writerMethod2() {
        writerMethod2Called = true
    }

    @groovy.transform.WithReadLock('myLock')
    public void namedReaderMethod1() {
        throw new Exception()
    }
    @groovy.transform.WithReadLock('myLock')
    public void namedReaderMethod2() {
        throw new Exception()
    }
    @groovy.transform.WithWriteLock('myLock')
    public void namedWriterMethod1() {
        throw new Exception()
    }
    @groovy.transform.WithWriteLock('myLock')
    public void namedWriterMethod2() {
        throw new Exception()
    }

    @groovy.transform.WithReadLock
    public void staticReaderMethod1() {
        staticReaderMethod1Called = true
    }
    @groovy.transform.WithReadLock
    public void staticReaderMethod2() {
        staticReaderMethod2Called = true
    }
    @groovy.transform.WithWriteLock
    public void staticWriterMethod1() {
        staticWriterMethod1Called = true
    }
    @groovy.transform.WithWriteLock
    public void staticWriterMethod2() {
        staticWriterMethod2Called = true
    }
}
