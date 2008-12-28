package groovy.jmx.builder.vm5

import javax.management.ObjectName
import groovy.jmx.builder.JmxBuilder

class JmxListenerFactoryTest extends GroovyTestCase {
  def builder

  void setUp() {
    builder = new JmxBuilder()
  }

  void testRequiredAttributeFrom() {
    builder.timer(name: "test:type=timer")
    def lstr = builder.listener(from: "test:type=timer")
    assert lstr
    assert lstr.type == "eventListener"
    assert lstr.from instanceof ObjectName
    assert lstr.from == new ObjectName("test:type=timer")

    shouldFail {
      lstr = builder.listener(event: "someEvent")
      lstr = builder.listener(from: "test:type=nonExistingObject")
    }
  }

  void testListenerEvent() {
    builder.timer(name: "test:type=timer", period: 300).start()
    def eventCount = 0
    def lstr = builder.listener(from: "test:type=timer", call: {event ->
      eventCount = eventCount + 1
    })
    Thread.currentThread().sleep(700)
    assert eventCount > 1

    shouldFail {
      eventCount = 0
      def lstr2 = builder.listener(from: "test:type=timer", call: {event ->
        eventCount = eventCount + 1
      })
      Thread.currentThread().sleep(700)
      assert eventCount == 0
    }
  }
}