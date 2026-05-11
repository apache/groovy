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

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.management.ObjectName
import java.lang.management.ManagementFactory
import java.util.logging.Level
import java.util.logging.Logger

import static groovy.test.GroovyAssert.shouldFail

class JmxListenerFactoryTest {
    private static final Logger LOGGER = Logger.getLogger(JmxListenerFactoryTest.name)

    static {
        // Prime the code paths that sporadically NPE on first use in containerized
        // CI environments — JDK cgroup-v2 detection (CgroupV2Subsystem / CgroupInfo).
        // JMX MBean registration walks the platform OS MXBean, which on Linux
        // consults cgroup state. Where the trip is a one-shot first-use race,
        // priming here absorbs it. Where the trip is deterministic (the JDK bug
        // fires on every call in this env), the per-test guard below converts it
        // into a skipped assumption instead of a failure.
        try {
            ManagementFactory.getOperatingSystemMXBean().toString()
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, 'Primed OS MXBean init absorbed exception (likely JDK cgroup-v2)', t)
        }
        try {
            new JmxBuilder().timer(name: 'prime:type=timer-prime', period: 60000)
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, 'Primed JmxBuilder.timer absorbed exception (likely JDK cgroup-v2)', t)
        }
    }

    def builder

    @BeforeEach
    void setUp() {
        builder = new JmxBuilder()
    }

    @Test
    void testRequiredAttributeFrom() {
        try {
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
        } catch (Throwable t) {
            skipIfKnownCgroupNpe(t)
            throw t
        }
    }

    @Test
    void testListenerEvent() {
        try {
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
        } catch (Throwable t) {
            skipIfKnownCgroupNpe(t)
            throw t
        }
    }

    /**
     * If the given throwable (or any of its causes) is the known JDK cgroup-v2
     * NPE — {@code CgroupInfo.getMountPoint()} on a null controller — abort the
     * test with an assumption (reported as skipped, not failed). Any other
     * failure propagates unchanged.
     */
    private static void skipIfKnownCgroupNpe(Throwable t) {
        for (Throwable c = t; c != null; c = c.cause) {
            String msg = c.message
            if (c instanceof NullPointerException && msg != null &&
                (msg.contains('CgroupInfo.getMountPoint') || msg.contains('anyController') && msg.contains('null'))) {
                Assumptions.abort("Skipped: known JDK cgroup-v2 NPE in this CI environment: ${msg}")
            }
        }
    }
}
