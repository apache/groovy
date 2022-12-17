/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.transform

import groovy.mock.interceptor.StubFor
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.lang.reflect.Modifier

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assume.assumeTrue

/**
 * Tests for the {@link ThreadInterrupt} AST transform.
 */
final class ThreadInterruptTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            normal 'groovy.mock.interceptor.StubFor'
        }
    }
    private static final boolean jdk12plus = isAtLeastJdk('12.0')
    private Map<String, MethodNode> oldValues = [:]

    @After
    void tearDown() {
        if (jdk12plus) return
        Thread.metaClass = null
        ['CURRENTTHREAD_METHOD', 'ISINTERRUPTED_METHOD'].each {
            def ov = ThreadInterruptibleASTTransformation.getDeclaredField(it)
            def modifiersField = ov.class.getDeclaredField('modifiers')
            modifiersField.accessible = true
            modifiersField.setInt(ov, ov.modifiers & ~Modifier.FINAL)
            ov.accessible = true
            ov.set(ThreadInterruptibleASTTransformation, oldValues[it])
        }
    }

    @Before
    void setUp() {
        // JDK12+ doesn't allow adjusting static final fields even via reflection, so
        // skip all tests on such JDK versions - it is only test code that's affected
        // and currently we have coverage from builds with lower JDK versions.
        assumeTrue(!jdk12plus)

        ['CURRENTTHREAD_METHOD', 'ISINTERRUPTED_METHOD'].each {
            def ov = ThreadInterruptibleASTTransformation.getDeclaredField(it)
            def modifiersField = ov.class.getDeclaredField('modifiers')
            modifiersField.accessible = true
            modifiersField.setInt(ov, ov.modifiers & ~Modifier.FINAL)
            ov.accessible = true
            oldValues[it] = ov.get(ThreadInterruptibleASTTransformation)
            ov.set(ThreadInterruptibleASTTransformation, null)
        }
    }

    @Test
    void testDefaultParameters_Method() {
        assertScript shell, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() { }
            }

            def mocker = new StubFor(Thread)
            def counter = new CountingThread()
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
            mocker.use {
                new C().m()
            }
            assert 1 == counter.interruptedCheckCount
        '''
    }

    @Test
    void testNoMethodCheck_Method() {
        assertScript shell, '''
            @ThreadInterrupt(applyToAllClasses=false, checkOnMethodStart=false)
            class C {
                def m() { }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
            // no exception means success
        '''
    }

    @Test
    void testDefaultParameters_ForLoop() {
        assertScript shell, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    for (int i in (1..99)) {
                        // do something
                    }
                }
            }

            def mocker = new StubFor(Thread)
            def counter = new CountingThread()
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
            mocker.use {
                new C().m()
            }
            assert 100 == counter.interruptedCheckCount
        '''
    }

    @Test
    void testDefaultParameters_WhileLoop() {
        assertScript shell, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    int x = 99
                    while (x > 0) {
                        x--
                    }
                }
            }

            def mocker = new StubFor(Thread)
            def counter = new CountingThread()
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
            mocker.use {
                new C().m()
            }
            assert 100 == counter.interruptedCheckCount
        '''
    }

    @Test
    void testDefaultParameters_Closure() {
        assertScript shell, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    99.times {
                        // do something
                    }
                }
            }

            def mocker = new StubFor(Thread)
            def counter = new CountingThread()
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
            mocker.use {
                new C().m()
            }
            assert 100 == counter.interruptedCheckCount
        '''
    }

    @Test
    void testInterrupt_Method_AndTestExceptionMessage() {
        def err = shouldFail shell, InterruptedException, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() { }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
        '''
        assert err.message == 'Execution interrupted. The current thread has been interrupted.'
    }

    @Test
    void testInterrupt_ForLoop() {
        shouldFail shell, InterruptedException, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    for (int i in (1..99)) {
                        // do something
                    }
                }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
        '''
    }

    @Test
    void testInterrupt_WhileLoop() {
        shouldFail shell, InterruptedException, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    int x = 99
                    while (x > 0) {
                        x--
                    }
                }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
        '''
    }

    @Test
    void testInterrupt_Closure() {
        shouldFail shell, InterruptedException, '''
            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    99.times {
                      // do something
                    }
                }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
        '''
    }

    @Test
    void testInterrupt_ClosureWithCustomExceptionType() {
        shouldFail shell, CustomException, '''
            @ThreadInterrupt(applyToAllClasses=false, thrown=CustomException)
            class C {
                def m() {
                    99.times {
                      // do something
                    }
                }
            }

            def mocker = new StubFor(Thread)
            mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
            mocker.use {
                new C().m()
            }
        '''
    }

    @Test
    void testEntireCompileUnitIsAffected() {
        def script = '''
            def scriptMethod() {
                // this method should inherit the checks from the annotation defined later
            }

            @ThreadInterrupt
            class C {
                def m() {
                    // this method should also be guarded
                }
            }

            scriptMethod()
            new C().m()
        '''
        def mocker = new StubFor(Thread)
        def counter = new CountingThread()
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            shell.evaluate(script)
        }
        assert counter.interruptedCheckCount == 3 // once for run(), once for scriptMethod() and once for m()
    }

    @Test
    void testOnlyScriptAffected() {
        def script = '''
            @ThreadInterrupt(applyToAllClasses=false)
            def scriptMethod() {
                // should be affected
            }

            class C {
                def m() {
                    // should not be affected
                }
            }
            scriptMethod()
            new C().m()
        '''
        def mocker = new StubFor(Thread)
        def counter = new CountingThread()
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            shell.evaluate(script)
        }
        assert counter.interruptedCheckCount == 2 // once for run() and once for scriptMethod()
    }

    @Test
    void testAnnotationOnImport() {
        def script = '''
            @ThreadInterrupt
            import java.lang.String

            3.times {
                // should be affected
            }
        '''
        def mocker = new StubFor(Thread)
        def counter = new CountingThread()
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            shell.evaluate(script)
        }
        assert counter.interruptedCheckCount == 4 // once for run() plus 3 for times loop
    }

    @Test
    void testOnlyClassAffected() {
        def script = '''
            def scriptMethod() {
                // this should not be affected
            }

            @ThreadInterrupt(applyToAllClasses=false)
            class C {
                def m() {
                    // this should be affected
                }
            }
            scriptMethod()
            new C().m()
        '''
        def mocker = new StubFor(Thread)
        def counter = new CountingThread()
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            shell.evaluate(script)
        }
        assert counter.interruptedCheckCount == 1 // once for m()
    }

    @Test
    void testThreadInterruptOnAbstractClass() {
        def script = '''
            @ThreadInterrupt
            abstract class A {
                abstract void m()
            }

            class C extends A {
                void m() {
                    99.times {
                        // do something
                    }
                }
            }

            new C().m()
        '''
        def mocker = new StubFor(Thread)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(shell, InterruptedException, script)
        }
    }

    // GROOVY-10877
    @Test
    void testThreadInterruptOnRecord() {
        assertScript '''
            @groovy.transform.ThreadInterrupt
            record Point(int x, int y) {
                String report() {
                    "$x, $y"
                }
            }

            def p = new Point(1, 2)
            assert 'Point[x=1, y=2]' == p.toString()
            assert '1, 2' == p.report()
        '''
    }
}

//--------------------------------------------------------------------------

class CountingThread extends Thread {
    int interruptedCheckCount = 0
    @Override
    boolean isInterrupted() {
        interruptedCheckCount += 1
        false
    }
}

class InterruptingThread extends Thread {
    @Override
    boolean isInterrupted() {
        true
    }
}

@groovy.transform.InheritConstructors
class CustomException extends Exception {
}
