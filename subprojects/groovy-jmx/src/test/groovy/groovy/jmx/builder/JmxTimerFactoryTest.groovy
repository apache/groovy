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

import groovy.jmx.GroovyMBean
import groovy.test.GroovyTestCase

import javax.management.MBeanServer
import javax.management.ObjectName

class JmxTimerFactoryTest extends GroovyTestCase {
    def builder
    MBeanServer server

    void setUp() {
        super.setUp()
        builder = new JmxBuilder()
        builder.registerFactory "timer", new JmxTimerFactory()
        server = builder.getMBeanServer()
    }

    void testSimpleTimerSetup() {
        GroovyMBean timer = builder.timer()
        assert timer
        assert server.queryNames(new ObjectName("jmx.builder:type=TimerService,*"), null).size() > 0
        shouldFail {
            builder.timer("This is a timer")
        }
        shouldFail {
            builder.timer(["foo"])
        }
    }

    void testTimerWithName() {
        GroovyMBean timer = builder.timer(name: "jmx.builder:type=TimerService")
        assert timer
        assert timer.name().toString() == "jmx.builder:type=TimerService"
        shouldFail {
            GroovyMBean timer2 = builder.timer()
            assertEqual timer2.name().toString(), "jmx.builder:type=TimerService"
        }
    }

    void testTimerEventName() {
        GroovyMBean timer = builder.timer(event: "timer.event")
        assert timer
        assert timer.getNotificationType(1) == "timer.event"
        shouldFail {
            GroovyMBean timer2 = builder.timer()
            assertEqual timer2.getNotificationType(1), "timer.event"
        }
    }

    void testTimerMessage() {
        GroovyMBean timer = builder.timer(message: "foo is here")
        assert timer
        assert timer.getNotificationMessage(1) == "foo is here"
        shouldFail {
            GroovyMBean timer2 = builder.timer()
            assertEqual timer2.getNotificationMessage(1), "foo bar"
        }
    }

    void testTimerUserData() {
        def today = new Date()
        GroovyMBean timer = builder.timer(data: today)
        assert timer
        assert timer.getNotificationUserData(1) == today
        shouldFail {
            GroovyMBean timer2 = builder.timer(data: new Date().toString())
            assertEqual timer2.getNotificationMessage(1), today
        }
    }

    void testTimerDate() {
        def today = new Date()
        GroovyMBean timer = builder.timer(date: today)
        assert timer
        assert timer.getDate(1) == today

        timer = builder.timer(date: "now")
        assert timer.getDate(1)

        shouldFail {
            GroovyMBean timer2 = builder.timer(date: new Date())
            assertEqual timer2.getDate(1), today
        }
    }

    void testTimerPeriod() {
        GroovyMBean timer = builder.timer(period: 1000)
        assert timer
        assert timer.getPeriod(1) == 1000

        timer = builder.timer(period: "2s")
        assert timer.getPeriod(1) == 2000

        timer = builder.timer(period: "22s")
        assert timer.getPeriod(1) == 22000

        timer = builder.timer(period: "2m")
        assert timer.getPeriod(1) == (2000 * 60)

        timer = builder.timer(period: "22m")
        assert timer.getPeriod(1) == (22000 * 60)

        timer = builder.timer(period: "2h")
        assert timer.getPeriod(1) == (2000 * 60 * 60)

        timer = builder.timer(period: "22h")
        assert timer.getPeriod(1) == (22000 * 60 * 60)

        timer = builder.timer(period: "2d")
        assert timer.getPeriod(1) == (2000 * 60 * 60 * 24)

        timer = builder.timer(period: "22d")
        assert timer.getPeriod(1) == (22000 * 60 * 60 * 24)

        timer = builder.timer(period: "Mood")
        assert timer.getPeriod(1) == 1000

        shouldFail {
            timer = builder.timer(period: "2d")
            assert timer.getPeriod(1) == (22000 * 60 * 60)
        }
    }

    void testNestedTimer() {
        def timers = builder.export {
            timer(event: "event.hi.heartbeat")
            timer(event: "event.low.heartbeat")
        }
        assert timers
        assert timers.size() == 2
        assert timers[0].getNotificationType(1) == "event.hi.heartbeat"
        assert timers[1].getNotificationType(1) == "event.low.heartbeat"
        shouldFail {
            assert timers[2].getNotificationType(1) == "event.low.heartbeat"
        }
    }
}