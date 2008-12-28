/*
 * Copyright 2008 the original author or authors.
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

import javax.management.MBeanServer
import javax.management.NotificationFilter
import javax.management.NotificationFilterSupport
import javax.management.ObjectName


/**
* The JmxTimerFactory class generates a timer() node for JmxBuilder.  This node crates a standard JMX Timer object
 * that can be used to provide timing signals to registered JMX listeners.
 *
 * <p>Syntax</p>
 *
 * <pre>
 * timer(
 *     name:"object name"|ObjectName(),
 *     event:"...",
 *     message:"...",
 *     data:dataValue
 *     startDate:"now"|dateValue
 *     period:"99d"|"99h"|"99m"|"99s"|99
 *     occurences:long
 * )
 * </pre>
 * 
 * @author Vladimir Vivien
 *
 */
class JmxTimerFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (nodeParam) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }
        JmxBuilder fsb = (JmxBuilder) builder
        javax.management.timer.Timer timer = new javax.management.timer.Timer()
        def metaMap = [:]
        metaMap.server = fsb.getMBeanServer()
        metaMap.timer = timer
        metaMap.name = nodeAttribs.remove("name")
        metaMap.event = nodeAttribs.remove("event") ?: nodeAttribs.remove("type") ?: "jmx.builder.event"
        metaMap.message = nodeAttribs.remove("message") ?: nodeAttribs.remove("msg")
        metaMap.data = nodeAttribs.remove("data") ?: nodeAttribs.remove("userData")
        metaMap.date = nodeAttribs.remove("date") ?: nodeAttribs.remove("startDate")
        metaMap.period = nodeAttribs.remove("period") ?: nodeAttribs.remove("frequency")
        metaMap.occurences = nodeAttribs.remove("occurs") ?: nodeAttribs.remove("occurences") ?: 0

        // normalize data
        metaMap.name = getNormalizedName(fsb, timer, metaMap.name)
        metaMap.date = getNormalizedDate(metaMap.date)
        metaMap.period = getNormalizedPeriod(metaMap.period)
        metaMap.listeners = getNormalizedRecipientList(metaMap.listeners)

        def result = registerTimer(metaMap)
        return result
    }

    private def getNormalizedName(fsb, timer, name) {
        def result
        if (!name) {
            result = getDefaultName(fsb, timer)
        } else {
            if (name instanceof String) {
                result = new ObjectName(name)
            } else if (name instanceof ObjectName) {
                result = name
            } else {
                result = getDefaultName(fsb, time)
            }
        }
        return result
    }

    private def getDefaultName(fsb, timer) {
        def name = "${fsb.getDefaultJmxNameDomain()}:type=TimerService,name=Timer@${timer.hashCode()}"
        return new ObjectName(name)
    }

    private def getNormalizedDate(date) {
        if (!date) return new Date()
        if (date instanceof Date) {
            return date
        }
        def startDate
        switch (date) {
            case null:
            case "now":
            default:
                startDate = new Date()
        }
        return startDate
    }

    private def getNormalizedPeriod(period) {
        if (!period) return 1000L
        if (period instanceof Number) {
            return period
        }
        def result = 1000L
        if (period instanceof String) {
            def multiplier = period[-1]
            def value
            try {
                value = period[0..-2].toLong()
            } catch (e) {
                multiplier = "x"
            }
            switch (multiplier) {
                case "s":
                    result = value * 1000
                    break
                case "m":
                    result = value * 60 * 1000
                    break
                case "h":
                    result = value * 60 * 60 * 1000
                    break
                case "d":
                    result = value * 24 * 60 * 60 * 1000
                    break
                default:
                    result = 1000L
            }
        }
        return result
    }

    private def getNormalizedRecipientList(list) {
        if (!list) return null
        def result = []

        list.each {name ->
            def on
            if (name instanceof String) {
                on = new ObjectName(name)
            }
            if (name instanceof ObjectName) {
                on = name
            }
            result.add(on)
        }

        return result
    }

    private def registerTimer(metaMap) {
        def server = (MBeanServer) metaMap.server
        def timer = metaMap.timer
        timer.addNotification(
                metaMap.event,
                metaMap.message,
                metaMap.data,
                metaMap.date,
                metaMap.period,
                metaMap.occurences)

        if (server.isRegistered(metaMap.name)) {
            server.unregisterMBean metaMap.name
        }
        server.registerMBean(timer, metaMap.name)
        return new GroovyMBean(metaMap.server, metaMap.name)
    }

    private NotificationFilter getEventFilter(type) {
        def noteFilter = new NotificationFilterSupport()
        noteFilter.enableType type
        return noteFilter
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        if (parentNode != null) {
            parentNode.add(thisNode)
        }
    }
}