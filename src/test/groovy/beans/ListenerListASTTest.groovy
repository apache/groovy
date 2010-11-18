package groovy.beans

/**
 * @author Alexander Klein
 */
class ListenerListASTTest extends GroovyTestCase {
  public void testDefaultFireAndName() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
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
    """)
  }

  public void testCustomFireAndName() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent, fire = "eventOccurred2", name = "testListener2")
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      assert tc.class.methods.name.contains("addTestListener") == false
      assert tc.class.methods.name.contains("removeTestListener") == false
      assert tc.class.methods.name.contains("getTestListeners") == false
      assert tc.class.methods.name.contains("fireEventOccurred") == false
      assert tc.testListener2s.size() == 0
      tc.addTestListener2 ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      assert tc.testListener2s.size() == 1
      tc.fireEventOccurred2 (source, message)
      tc.fireEventOccurred2 (new TestEvent (source, message))
      tc.removeTestListener2 (tc.testListener2s[0])
      assert tc.testListener2s.size() == 0
      assert count == 2
      assert evt.source.is (source)
      assert evt.message.is (message)
    """)
  }

  public void testMultipleMethodListener() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred1(TestEvent event)
        void eventOccurred2(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count1 = 0
      int count2 = 0
      String source1 = "TestSource"
      String source2 = "TestSource"
      String message1 = "TestMessage"
      String message2 = "TestMessage"
      def evt1
      def evt2
      tc.addTestListener ([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestListener)
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
    """)
  }

  public void testMultipleListsOfSameEvent() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent)
        def listeners
        @ListenerList(listener = TestListener, event = TestEvent, fire = "eventOccurred2->eventOccurred", name = "testListener2")
        def listeners2
      }

      tc = new TestClass ()
      assert tc.listeners == []
      assert tc.listeners2 == []
      int count1 = 0
      int count2 = 0
      String source1 = "TestSource"
      String source2 = "TestSource"
      String message1 = "TestMessage"
      String message2 = "TestMessage"
      def evt1
      def evt2
      tc.addTestListener ([eventOccurred: { e -> count1++; evt1 = e }] as TestListener)
      tc.fireEventOccurred (source1, message1)
      assert count1 == 1
      assert count2 == 0
      assert evt1.source.is (source1)
      assert evt1.message.is (message1)

      tc.addTestListener2 ([eventOccurred: { e -> count2++; evt2 = e }] as TestListener)
      tc.fireEventOccurred2 (source2, message2)
      assert count1 == 1
      assert count2 == 1
      assert evt2.source.is (source2)
      assert evt2.message.is (message2)
    """)
  }

  public void testMultipleMethodListenerWithCustomFire() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred1(TestEvent event)
        void eventOccurred2(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = TestEvent, fire = ["eventOccurred->eventOccurred1", "nextEventOccurred->eventOccurred2"])
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count1 = 0
      int count2 = 0
      String source1 = "TestSource"
      String source2 = "TestSource"
      String message1 = "TestMessage"
      String message2 = "TestMessage"
      def evt1
      def evt2
      tc.addTestListener ([eventOccurred1: { e -> count1++; evt1 = e }, eventOccurred2: { e -> count2++; evt2 = e }] as TestListener)
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
    """)
  }

  public void testMapEvent() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(Map event)
      }

      class TestClass {
        @ListenerList(listener = TestListener, event = Map)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      tc.fireEventOccurred ([source: source, message: message])
      assert count == 1
      assert evt.source.is (source)
      assert evt.message.is (message)
    """)
  }

  public void testMapListener() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(event = TestEvent, name = "testListener", fire = "eventOccurred")
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }])
      tc.fireEventOccurred (source, message)
      tc.fireEventOccurred (new TestEvent (source, message))
      assert count == 2
      assert evt.source.is (source)
      assert evt.message.is (message)
    """)
  }

  public void testMultipleMapListener() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList(event = TestEvent, name = "testListener", fire = ["eventOccurred", "eventOccurred2"])
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      int count2 = 0
      String source2 = "TestSource"
      String message2 = "TestMessage"
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
    """)
  }

  public void testMapListenerAndEvent() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      class TestClass {
        @ListenerList(name = "testListener", fire = "eventOccurred", event = Map)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }])
      tc.fireEventOccurred ([source: source, message: message])
      assert count == 1
      assert evt.source.is (source)
      assert evt.message.is (message)
    """)
  }

  public void testListEvent() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(List event)
      }

      class TestClass {
        @ListenerList(listener = TestListener)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      tc.fireEventOccurred ([source, message])
      assert count == 1
      assert evt[0].is (source)
      assert evt[1].is (message)
    """)
  }

  public void testObjectEvent() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(Object event)
      }

      class TestClass {
        @ListenerList(listener = TestListener)
        def listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
      def evt
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      def obj = new Object()
      tc.fireEventOccurred (obj)
      assert count == 1
      assert evt.is (obj)
    """)
  }

  public void testDefaultForGenericList() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(Object event)
      }

      class TestClass {
        @ListenerList(event = String)
        List<TestListener> listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      def evt
      assert tc.testListeners.size() == 0
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      assert tc.testListeners.size() == 1
      tc.fireEventOccurred (source)
      tc.removeTestListener (tc.testListeners[0])
      assert tc.testListeners.size() == 0
      assert count == 1
      assert evt.is (source)
    """)
  }

  public void testDefaultForGenericListUsingFirstAbstractMethodsParameter() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      class TestClass {
        @ListenerList
        List<TestListener> listeners
      }

      tc = new TestClass ()
      assert tc.listeners == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
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
    """)
  }

  public void testClassAnnotation() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      @ListenerList(listener = TestListener, event = TestEvent)
      class TestClass {
      }

      tc = new TestClass ()
      assert tc.testListenerList == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
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
    """)
  }

  public void testClassAnnotationWithExistingField() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      @ListenerList(listener = TestListener, event = TestEvent)
      class TestClass {
        def testListenerList
      }

      tc = new TestClass ()
      assert tc.testListenerList == []
      int count = 0
      String source = "TestSource"
      String message = "TestMessage"
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
    """)
  }

  public void testMultipleClassAnnotation() {
    GroovyShell shell = new GroovyShell()
    shell.evaluate("""
      package b
      import groovy.beans.*

      interface TestListener {
        void eventOccurred(TestEvent event)
      }

      class TestEvent {
        def source
        String message

        TestEvent(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      interface Test1Listener {
        void event1Occurred(Test1Event event)
      }

      class Test1Event {
        def source
        String message

        Test1Event(def source, String message) {
          this.source = source
          this.message = message
        }
      }

      @ListenerList(listener = TestListener, event = TestEvent)
      @ListenerList(listener = Test1Listener, event = Test1Event)
      class TestClass {
      }

      tc = new TestClass ()
      assert tc.testListenerList == []
      assert tc.test1ListenerList == []
      int count = 0
      int count1 = 0
      String source = "TestSource"
      String source1 = "TestSource"
      String message = "TestMessage"
      String message1 = "TestMessage"
      def evt
      assert tc.testListeners.size() == 0
      tc.addTestListener ([eventOccurred: { e -> count++; evt = e }] as TestListener)
      assert tc.testListeners.size() == 1

      assert tc.test1Listeners.size() == 0
      tc.addTest1Listener ([event1Occurred: { e -> count1++; evt = e }] as Test1Listener)
      assert tc.test1Listeners.size() == 1

      tc.fireEventOccurred (source, message)
      tc.fireEventOccurred (new TestEvent (source, message))
      tc.removeTestListener (tc.testListeners[0])
      assert tc.testListeners.size() == 0
      assert count == 2
      assert evt.source.is (source)
      assert evt.message.is (message)
          println tc.metaClass.methods
      tc.fireEvent1Occurred (source, message)
      tc.fireEvent1Occurred (new Test1Event (source, message))
      tc.removeTest1Listener (tc.test1Listeners[0])
      assert tc.test1Listeners.size() == 0
      assert count1 == 2
      assert evt.source.is (source1)
      assert evt.message.is (message1)
    """)
  }
}
