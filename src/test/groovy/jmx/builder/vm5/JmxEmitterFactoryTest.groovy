package groovy.jmx.builder.vm5

import javax.management.MBeanServer
import groovy.jmx.builder.JmxBuilder

public class JmxEmitterFactoryTest extends GroovyTestCase {
  def builder
  MBeanServer server

  void setUp() {
    server = javax.management.MBeanServerFactory.createMBeanServer()
    builder = new JmxBuilder(server)
  }

  void testSimpleEmitterSetup() {
    def emitter = builder.emitter()
    assert emitter
    assert emitter.Event == "jmx.builder.event.emitter"

    def seq = emitter.send("Hello")
    assert seq > 0

    seq = emitter.send("World")
    assert seq > 1
  }

  void testEmitterWithImplicitListeners() {
    def emitter = builder.emitter(name: "jmx.builder:type=Emitter")
    assert emitter

    def eventTrap = 0
    builder.listener(from: "jmx.builder:type=Emitter", call: {event ->
      eventTrap = eventTrap + 1
    })

    def seq = emitter.send("Hello World")
    Thread.currentThread().sleep(300)

    assert eventTrap == 1

    shouldFail {
      emitter.send("Hello World")
      Thread.currentThread().sleep(300)
      assert eventTrap == 1
    }
  }

  void testEmitterWithExplicitListeners() {
    def count = 0
    def data
    def emitter = builder.emitter(name: "jmx.builder:type=Emitter")
    assert emitter

    def beans = builder.export {
      bean(target: new MockManagedObject(), name: "jmx.builder:type=Listner",
              listeners: [
                      "emitter": [
                              from: "jmx.builder:type=Emitter",
                              call: {e ->
                                count = count + 1
                                data = e.data
                              }
                      ]
              ])
    }
    assert beans[0]

    long seq = emitter.send("Hello|World")
    Thread.currentThread().sleep(300)

    assert count == 1
    assert data == "Hello|World"

    emitter.send("World Order")
    shouldFail {
      assert count != 2
    }
    shouldFail {
      assert data == "Hello|World"
    }
  }

}