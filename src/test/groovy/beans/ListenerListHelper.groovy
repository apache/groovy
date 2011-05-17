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
  void eventOccurred(List<? extends Object> event)
}

interface TestMapListener {
  void eventOccurred(Map event)
}

interface TestTwoMethodListener {
  void eventOccurred1(TestEvent event)
  void eventOccurred2(TestEvent event)
}


