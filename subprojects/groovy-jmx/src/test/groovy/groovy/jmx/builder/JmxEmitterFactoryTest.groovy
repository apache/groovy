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

import javax.management.MBeanServer

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

        emitter.send("Hello World")
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
            bean(target: new MockManagedObject(), name: "jmx.builder:type=Listener",
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

        emitter.send("Hello|World")
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