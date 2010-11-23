package groovy.beans

/**
 * These test event is used as a sample event.
 */
class TestEvent {
    def source
    String message

    TestEvent(def source, String message) {
        this.source = source
        this.message = message
    }
}

class SomeOtherTestEvent {
    def source
    String message

    SomeOtherTestEvent(def source, String message) {
        this.source = source
        this.message = message
    }
}

/**
 * These interfaces are all used as variations on producing listener lists.
 */
interface TestListener {
    void eventOccurred(TestEvent event)
}

interface SomeOtherTestListener {
    void event2Occurred(SomeOtherTestEvent event)
}

interface TestObjectListener {
  void eventOccurred(Object event)
}

interface TestListListener {
  void eventOccurred(List event)
}

interface TestMapListener {
  void eventOccurred(Map event)
}

interface TestTwoMethodListener {
  void eventOccurred1(TestEvent event)
  void eventOccurred2(TestEvent event)
}


/**
 * Unit test for ListenerList.
 * @author Alexander Klein
 * @author Hamlet D'Arcy
 */

class ListenerListASTTest extends GroovyTestCase {

    public void testDefaultFireAndName() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
      package b
      import groovy.beans.*

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent)
        def listeners
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
        tc.fireEventOccurred(source, message)
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeTestListener(tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 2
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testCustomFireAndName() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
      package b
      import groovy.beans.*

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent, fire = "eventOccurred2", name = "someOtherTestListener")
        def listeners
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
        assert !tc.class.methods.name.contains('fireEventOccurred')
        assert tc.someOtherTestListeners.size() == 0
        tc.addSomeOtherTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.someOtherTestListeners.size() == 1
        tc.fireEventOccurred2(source, message)
        tc.fireEventOccurred2(new TestEvent(source, message))
        tc.removeSomeOtherTestListener(tc.someOtherTestListeners[0])
        assert tc.someOtherTestListeners.size() == 0
        assert count == 2
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testMultipleMethodListener() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(listener = TestTwoMethodListener, event = TestEvent)
                def listeners
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
        tc.addTestTwoMethodListener ([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestTwoMethodListener)
        tc.fireEventOccurred1 (source1, message1)
        assert count1 == 1
        assert count2 == 0
        assert evt1.source.is (source1)
        assert evt1.message.is (message1)
        tc.fireEventOccurred2 (source2, message2)
        assert count1 == 1
        assert count2 == 1
        assert evt2.source.is (source2)
        assert evt2.message.is (message2)
    }

    public void testMultipleListsOfSameEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(listener = TestListener, event = TestEvent)
                def listeners
                @ListenerList(listener = TestListener, event = TestEvent, fire = "eventOccurred2->eventOccurred", name = "someOtherTestListener")
                def listeners2
              }

              new TestClass ()    """)

        assert tc.listeners == []
        assert tc.listeners2 == []
        int count1 = 0
        int count2 = 0
        String source1 = 'TestSource'
        String source2 = 'TestSource'
        String message1 = 'TestMessage'
        String message2 = 'TestMessage'
        def evt1
        def evt2
        tc.addTestListener ([eventOccurred: { e -> count1++; evt1 = e }] as TestListener)
        tc.fireEventOccurred (source1, message1)
        assert count1 == 1
        assert count2 == 0
        assert evt1.source.is (source1)
        assert evt1.message.is (message1)

        tc.addSomeOtherTestListener ([eventOccurred: { e -> count2++; evt2 = e }] as TestListener)
        tc.fireEventOccurred2 (source2, message2)
        assert count1 == 1
        assert count2 == 1
        assert evt2.source.is (source2)
        assert evt2.message.is (message2)
    }

    public void testMultipleMethodListenerWithCustomFire() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
      package b
      import groovy.beans.*

      class TestClass {
        @ListenerList(listener = TestTwoMethodListener, event = TestEvent, fire = ["eventOccurred->eventOccurred1", "nextEventOccurred->eventOccurred2"])
        def listeners
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
        tc.addTestTwoMethodListener ([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestTwoMethodListener)
        tc.fireEventOccurred (source1, message1)
        assert count1 == 1
        assert count2 == 0
        assert evt1.source.is (source1)
        assert evt1.message.is (message1)
        tc.fireNextEventOccurred (source2, message2)
        assert count1 == 1
        assert count2 == 1
        assert evt2.source.is (source2)
        assert evt2.message.is (message2)
    }

    public void testMapEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(listener = TestMapListener, event = Map)
                def listeners
              }

              new TestClass ()    """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        tc.addTestMapListener ([eventOccurred: { e -> count++; evt = e }] as TestMapListener)
        tc.fireEventOccurred ([source: source, message: message])
        assert count == 1
        assert evt.source.is (source)
        assert evt.message.is (message)
    }

    public void testMapListener() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(event = TestEvent, name = "testListener", fire = "eventOccurred")
                def listeners
              }

              new TestClass () """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        tc.addTestListener ([eventOccurred: { e -> count++; evt = e }])
        tc.fireEventOccurred (source, message)
        tc.fireEventOccurred (new TestEvent (source, message))
        assert count == 2
        assert evt.source.is (source)
        assert evt.message.is (message)
    }

    public void testMultipleMapListener() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
          package b
          import groovy.beans.*

          class TestClass {
            @ListenerList(event = TestEvent, name = "testListener", fire = ["eventOccurred", "eventOccurred2"])
            def listeners
          }

          new TestClass ()    """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        int count2 = 0
        String source2 = 'TestSource'
        String message2 = 'TestMessage'
        def evt2
        tc.addTestListener ([eventOccurred: { e -> count++; evt = e }, eventOccurred2: { e -> count2++; evt2 = e }])
        tc.fireEventOccurred (source, message)
        tc.fireEventOccurred (new TestEvent (source, message))
        tc.fireEventOccurred2 (source2, message2)
        tc.fireEventOccurred2 (new TestEvent (source2, message2))
        assert count == 2
        assert evt.source.is (source)
        assert evt.message.is (message)
        assert count2 == 2
        assert evt.source.is (source2)
        assert evt.message.is (message2)
    }

    public testMapListenerAndEvent = {->
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(name = "testListener", fire = "eventOccurred", event = Map)
                def listeners
              }

              new TestClass ()
            """)
        assert tc.listeners == []
        int count = 0
        String source = "TestSource"
        String message = 'TestMessage'
        def evt
        tc.addTestListener([eventOccurred: { e -> count++; evt = e }])
        tc.fireEventOccurred([source: source, message: message])
        assert count == 1
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testListEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(listener = TestListListener)
                def listeners
              }

              new TestClass ()
            """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        tc.addTestListListener([eventOccurred: { e -> count++; evt = e }] as TestListListener)
        tc.fireEventOccurred ([source, message])
        assert count == 1
        assert evt[0].is (source)
        assert evt[1].is (message)
    }

    public void testObjectEvent() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(listener = TestObjectListener)
                def listeners
              }

              new TestClass ()    """)
        assert tc.listeners == []
        int count = 0
        def evt
        tc.addTestObjectListener ([eventOccurred: { e -> count++; evt = e }] as TestObjectListener)
        def obj = new Object()
        tc.fireEventOccurred (obj)
        assert count == 1
        assert evt.is (obj)
    }

    public void testDefaultForGenericList() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList(event = String)
                List<TestObjectListener> listeners
              }

              new TestClass ()    """)

        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        def evt
        assert tc.testObjectListeners.size() == 0
        tc.addTestObjectListener ([eventOccurred: { e -> count++; evt = e }] as TestObjectListener)
        assert tc.testObjectListeners.size() == 1
        tc.fireEventOccurred (source)
        tc.removeTestObjectListener (tc.testObjectListeners[0])
        assert tc.testObjectListeners.size() == 0
        assert count == 1
        assert evt.is (source)        
    }

    public void testDefaultForGenericListUsingFirstAbstractMethodsParameter() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
              package b
              import groovy.beans.*

              class TestClass {
                @ListenerList
                List<TestListener> listeners
              }

              new TestClass ()     """)
        assert tc.listeners == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1
        tc.fireEventOccurred (source, message)
        tc.fireEventOccurred (new TestEvent (source, message))
        tc.removeTestListener (tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 2
        assert evt.source.is (source)
        assert evt.message.is (message)
    }

    public void testClassAnnotation() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
          package b
          import groovy.beans.*

          @ListenerList(listener = TestListener, event = TestEvent)
          class TestClass {
          }

          new TestClass () """)

        assert tc.testListenerList == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1
        tc.fireEventOccurred (source, message)
        tc.fireEventOccurred (new TestEvent (source, message))
        tc.removeTestListener (tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 2
        assert evt.source.is (source)
        assert evt.message.is (message)
    }

    public void testClassAnnotationWithExistingField() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
          package b
          import groovy.beans.*

          @ListenerList(listener = TestListener, event = TestEvent)
          class TestClass {
            def testListenerList
          }

          new TestClass () """)
        assert tc.testListenerList == []
        int count = 0
        String source = 'TestSource'
        String message = 'TestMessage'
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1
        tc.fireEventOccurred(source, message)
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeTestListener(tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 2
        assert evt.source.is(source)
        assert evt.message.is(message)
    }

    public void testMultipleClassAnnotation() {
        GroovyShell shell = new GroovyShell()
        def tc = shell.evaluate("""
      package b
      import groovy.beans.*

      @ListenerList(listener = TestListener, event = TestEvent)
      @ListenerList(listener = SomeOtherTestListener, event = SomeOtherTestEvent)
      class TestClass {
      }

      new TestClass () """)
        assert tc.testListenerList == []
        assert tc.someOtherTestListenerList == []
        int count = 0
        int count1 = 0
        String source = 'TestSource'
        String source1 = 'TestSource'
        String message = 'TestMessage'
        String message1 = 'TestMessage'
        def evt
        assert tc.testListeners.size() == 0
        tc.addTestListener([eventOccurred: { e -> count++; evt = e }] as TestListener)
        assert tc.testListeners.size() == 1

        assert tc.someOtherTestListeners.size() == 0
        tc.addSomeOtherTestListener([event2Occurred: { e -> count1++; evt = e }] as SomeOtherTestListener)
        assert tc.someOtherTestListeners.size() == 1

        tc.fireEventOccurred(source, message)
        tc.fireEventOccurred(new TestEvent(source, message))
        tc.removeTestListener(tc.testListeners[0])
        assert tc.testListeners.size() == 0
        assert count == 2
        assert evt.source.is(source)
        assert evt.message.is(message)
        tc.fireEvent2Occurred(source, message)
        tc.fireEvent2Occurred(new SomeOtherTestEvent(source, message))
        tc.removeSomeOtherTestListener(tc.someOtherTestListeners[0])
        assert tc.someOtherTestListeners.size() == 0
        assert count1 == 2
        assert evt.source.is(source1)
        assert evt.message.is(message1)
    }
}
