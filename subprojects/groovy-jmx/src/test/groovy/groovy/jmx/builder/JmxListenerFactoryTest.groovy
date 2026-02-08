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
package groovy.jmx.builder

import groovy.test.GroovyTestCase

import javax.management.ObjectName

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
        def eventCount = 0
        builder.timer(name: "test:type=timer", period: 200).start()
        builder.listener(from: "test:type=timer", call: {event ->
            eventCount = eventCount + 1
        })
        sleep 1300
        assert eventCount > 1

        shouldFail {
            eventCount = 0
            builder.listener(from: "test:type=timer", call: {event ->
                eventCount = eventCount + 1
            })
            sleep 700
            assert eventCount == 0
        }
    }
}