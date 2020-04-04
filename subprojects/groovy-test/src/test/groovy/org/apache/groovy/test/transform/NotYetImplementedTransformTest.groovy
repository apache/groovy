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
package org.apache.groovy.test.transform

import junit.framework.AssertionFailedError
import org.junit.Test

final class NotYetImplementedTransformTest {

    private final GroovyShell shell = new GroovyShell()

    @Test
    void testNotYetImplementedJUnit3Failure() {
        def output = shell.evaluate('''
            import groovy.test.GroovyTestCase
            import groovy.test.NotYetImplemented

            class MyTests extends GroovyTestCase {
                @NotYetImplemented void testThatFails()  {
                    assertTrue(false)
                }
            }

            junit.textui.TestRunner.run(new junit.framework.TestSuite(MyTests))
        ''')

        assert output.wasSuccessful() : 'failing test method marked with @NotYetImplemented must NOT throw an AssertionError'
    }

    @Test
    void testNotYetImplementedJUnit3Exception() {
        def output = shell.evaluate('''
            import groovy.test.GroovyTestCase
            import groovy.test.NotYetImplemented

            class MyTests extends GroovyTestCase {
                @NotYetImplemented void testThatFails()  {
                    'test'.missingMethod()
                }
            }

            junit.textui.TestRunner.run(new junit.framework.TestSuite(MyTests))
        ''')

        assert output.wasSuccessful() : 'test method throwing exception marked with @NotYetImplemented must NOT throw an AssertionError'
    }

    @Test
    @Deprecated
    void testNotYetImplementedLegacyJunit3PassThrough() {
        def output = shell.evaluate('''
            import groovy.test.GroovyTestCase
            import groovy.transform.NotYetImplemented

            class MyTests extends GroovyTestCase {
                @NotYetImplemented void testThatPasses()  {
                    assertTrue(true)
                }
            }

            junit.textui.TestRunner.run(new junit.framework.TestSuite(MyTests))
        ''')

        assert output.failureCount() == 1 : 'succeeding test method marked with legacy @NotYetImplemented must throw an AssertionFailedError'
        assert output.failures().nextElement().thrownException() instanceof AssertionFailedError : 'succeeding test method marked with legacy @NotYetImplemented must throw an AssertionFailedError'
    }

    @Test
    void testNotYetImplementedJunit3PassThrough() {
        def output = shell.evaluate('''
            import groovy.test.GroovyTestCase
            import groovy.test.NotYetImplemented

            class MyTests extends GroovyTestCase {
                @NotYetImplemented(exception=junit.framework.AssertionFailedError) void testThatPasses()  {
                    assertTrue(true)
                }
            }

            junit.textui.TestRunner.run(new junit.framework.TestSuite(MyTests))
        ''')

        assert output.failureCount() == 1 : 'succeeding test method marked with @NotYetImplemented must throw an AssertionError'
        assert output.failures().nextElement().thrownException() instanceof AssertionError : 'succeeding test method marked with @NotYetImplemented must throw an AssertionError'
    }

    @Test
    void testNotYetImplementedJUnit3EmptyMethod() {
        def output = shell.evaluate('''
            import groovy.test.GroovyTestCase
            import groovy.test.NotYetImplemented

            class MyTests extends GroovyTestCase {
                @NotYetImplemented void testShouldNotFail() {
                }
            }

            junit.textui.TestRunner.run(new junit.framework.TestSuite(MyTests))
        ''')

        assert output.wasSuccessful() : 'empty test method must not throw an AssertionError'
    }

    @Test
    void testNotYetImplementedJUnit4Failure()  {
        def output = shell.evaluate('''
            import groovy.test.NotYetImplemented
            import org.junit.Assert
            import org.junit.Test
            import org.junit.runner.JUnitCore

            class MyTests {
                @NotYetImplemented @Test void testThatFails()  {
                    Assert.assertTrue(false)
                }
            }

            new JUnitCore().run(MyTests)
        ''')

        assert output.wasSuccessful() : 'failing @Test method marked with @NotYetImplemented must NOT throw an AssertionError'
    }

    @Test // GROOVY-8457
    void testNotYetImplementedJUnit4Failure_atCompileStatic()  {
        def output = shell.evaluate('''
            import groovy.transform.CompileStatic
            import groovy.test.NotYetImplemented
            import org.junit.Test
            import org.junit.runner.JUnitCore

            @CompileStatic
            class MyTests {
                @NotYetImplemented @Test void testThatFails()  {
                    assert false
                }
            }

            new JUnitCore().run(MyTests)
        ''')

        assert output.wasSuccessful() : 'failing @Test method marked with @CompileStatic and @NotYetImplemented must NOT throw an AssertionError'
    }

    @Test
    void testNotYetImplementedJUnit4Success() {
        def output = shell.evaluate('''
            import groovy.test.NotYetImplemented
            import org.junit.Assert
            import org.junit.Test
            import org.junit.runner.JUnitCore

            class MyTests {
                @NotYetImplemented @Test void testThatPasses()  {
                    Assert.assertTrue(true)
                }
            }

            new JUnitCore().run(MyTests)
        ''')

        assert output.failureCount == 1 : 'succeeding @Test method marked with @NotYetImplemented must throw an AssertionError'
        assert output.failures.first().exception instanceof AssertionError : 'succeeding @Test method marked with @NotYetImplemented must throw an AssertionError'
    }

    @Test // GROOVY-8457
    void testNotYetImplementedJUnit4Success_atCompileStatic()  {
        def output = shell.evaluate('''
            import groovy.transform.CompileStatic
            import groovy.test.NotYetImplemented
            import org.junit.Test
            import org.junit.runner.JUnitCore

            @CompileStatic
            class MyTests {
                @NotYetImplemented @Test void testThatFails()  {
                    assert true
                }
            }

            new JUnitCore().run(MyTests)
        ''')

        assert output.failureCount == 1 : 'succeeding @Test method marked with @CompileStatic and @NotYetImplemented must throw an AssertionError'
        assert output.failures.first().exception instanceof AssertionError : 'succeeding @Test method marked with @CompileStatic and @NotYetImplemented must throw an AssertionError'
    }
}
