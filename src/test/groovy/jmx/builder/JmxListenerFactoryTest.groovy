/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.jmx.builder

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
    Thread.currentThread().sleep(800)
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