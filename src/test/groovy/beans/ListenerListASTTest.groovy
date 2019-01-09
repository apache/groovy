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
package groovy.beans

/**
 * Unit test for ListenerList.
 */

class ListenerListASTTest extends GroovyTestCase {

    public void testDefaultFireAndName() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestListener> listeners
            }

            new TestClass()
        """)
        assert tc.listeners == []
        int count = 0
        String source = "TestSource"
        String message = "TestMessage"
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeTestListener(tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 1
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testCustomFireAndName() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList(name = "someOtherTestListener")
                List<TestListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count = 0
        String source = "TestSource"
        String message = "TestMessage"
        def evt
        assert !tc.class.methods.name.contains('addTestListener')
        assert !tc.class.methods.name.contains('removeTestListener')
        assert !tc.class.methods.name.contains('getTestListeners')
        assert tc.class.methods.name.contains('fireEventOccurred')
        assert tc.someOtherTestListeners.size() == 0
        tc.addSomeOtherTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.someOtherTestListeners.size() == 1
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeSomeOtherTestListener(tc.someOtherTestListeners[0])
        assert tc.someOtherTestListeners.size() == 0
        assert count == 1
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testMultipleMethodListener() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestTwoMethodListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count1 = 0
        int count2 = 0
        String source1 = 'TestSource'
        String source2 = 'TestSource'
        String message1 = 'TestMessage'
        String message2 = 'TestMessage'
        def evt1
        def evt2
        tc.addTestTwoMethodListener([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestTwoMethodListener)
        tc.fireEventOccurred1(new TestEvent(source1, message1))
        assert count1 == 1
        assert count2 == 0
        assert evt1.source.is(source1)
        assert evt1.message.is(message1)
        tc.fireEventOccurred2(new TestEvent(source2, message2))
        assert count1 == 1
        assert count2 == 1
        assert evt2.source.is(source2)
        assert evt2.message.is(message2)
    }

    public void testMultipleListenersConflictsDetected() {
        def message = shouldFail {
            new GroovyShell().evaluate("""
                package b
                import groovy.beans.*

                class TestClass {
                    @ListenerList
                    Set<TestListener> listeners
                    @ListenerList(name = "someOtherTestListener")
                    Vector<TestListener> listeners2
                }
            """)
        }

        assert message.contains('Class b.TestClass already has method fireEventOccurred')
    }

    void testMultipleMethodListeners() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestTwoMethodListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count1 = 0
        int count2 = 0
        String source1 = 'TestSource'
        String source2 = 'TestSource'
        String message1 = 'TestMessage'
        String message2 = 'TestMessage'
        def evt1
        def evt2
        tc.addTestTwoMethodListener([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestTwoMethodListener)
        tc.fireEventOccurred1(new TestEvent(source1, message1))
        assert count1 == 1
        assert count2 == 0
        assert evt1.source.is(source1)
        assert evt1.message.is(message1)
        tc.fireEventOccurred2(new TestEvent(source2, message2))
        assert count1 == 1
        assert count2 == 1
        assert evt2.source.is(source2)
        assert evt2.message.is(message2)
    }

    void testMapEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                  @ListenerList
                  List<TestMapListener> listeners
              }

              new TestClass ()    """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        tc.addTestMapListener([eventOccurred: { e -> count++; evt = e }] as TestMapListener)
        tc.fireEventOccurred([source: source, message: message])
        assert count == 1
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    void testError_AnnotationNotOnCollection() {
        def message = shouldFail {
            new GroovyShell().evaluate("""
                import groovy.beans.*

                class TestClass {
                    @ListenerList
                    def listeners
                }
            """)
        }
        assert message.contains('@groovy.beans.ListenerList can only annotate collection properties')
    }

    void testError_AnnotationWithoutGeneric() {
        def message = shouldFail {
            new GroovyShell().evaluate("""
                import groovy.beans.*

                class TestClass {
                    @ListenerList
                    List listeners
                }
            """)
        }
        assert message.contains('@groovy.beans.ListenerList fields must have a generic type')
    }

    void testError_AnnotationWithWildcard() {
        def message = shouldFail {
            new GroovyShell().evaluate("""
                import groovy.beans.*

                class TestClass {
                    @ListenerList
                    List<? super Object> listeners
                }
            """)
        }
        assert message.contains('@groovy.beans.ListenerList fields with generic wildcards not yet supported')
    }

    void testListEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestListListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        tc.addTestListListener([eventOccurred: { e -> count++; evt = e }] as TestListListener)
        tc.fireEventOccurred([source, message])
        assert count == 1
        assert evt[0].is(source)
        assert evt[1].is(message)
    }

    void testObjectEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestObjectListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count = 0
        def evt
        tc.addTestObjectListener([eventOccurred: { e -> count++; evt = e }] as TestObjectListener)
        def obj = new Object()
        tc.fireEventOccurred(obj)
        assert count == 1
        assert evt.is(obj)
    }

    void testDefaultForGenericList() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestObjectListener> listeners
            }

            new TestClass ()
        """)

        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        def evt
        assert tc.testObjectListeners.size() == 0
        tc.addTestObjectListener([eventOccurred: { e -> count++; evt = e }] as TestObjectListener)
        assert tc.testObjectListeners.size() == 1
        tc.fireEventOccurred(source)
        tc.removeTestObjectListener(tc.testObjectListeners[0])
        assert tc.testObjectListeners.size() == 0
        assert count == 1
        assert evt.is(source)
    }

    void testDefaultForGenericListUsingFirstAbstractMethodsParameter() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
            package b
            import groovy.beans.*

            class TestClass {
                @ListenerList
                List<TestListener> listeners
            }

            new TestClass ()
        """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeTestListener(tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 1
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

        // GROOVY-4795
        void testSynchronized() {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.ListenerList
                import java.lang.reflect.Modifier

                class C1 {
                    @ListenerList(synchronize=true) List<EventListener> listeners
                }
                class C2 {
                    @ListenerList(synchronize=false) List<EventListener> listeners
                }
                class C3 {
                    @ListenerList List<EventListener> listeners
                }

                assert Modifier.isSynchronized(C1.class.getMethod('getEventListeners').modifiers)
                assert !Modifier.isSynchronized(C2.class.getMethod('getEventListeners').modifiers)
                assert !Modifier.isSynchronized(C3.class.getMethod('getEventListeners').modifiers)
            """)
        }

        // GROOVY-4797
        void testPrimitiveTypes() {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.ListenerList
                import java.lang.reflect.Modifier

                class C {
                    @ListenerList List<Object> listeners // wait(long timeout) has primitive arg
                }

                assert C.class.getMethod('getObjects')
            """)
        }

    // GROOVY-8156
    void testListenerListWithEventClassInSameCompilationUnit() {
        assertScript '''
            class Event {}

            class EventListener {
                Event event
                void doSomething(Event e) {
                    event = e
                }
            }

            class EventHandler {
                @groovy.beans.ListenerList
                List<EventListener> listeners
            }

            def listener = new EventListener()
            def eh = new EventHandler()
            eh.addEventListener(listener)
            def testEvent = new Event()
            eh.fireDoSomething(testEvent)

            assert listener.event.is(testEvent)
        '''
    }
}
