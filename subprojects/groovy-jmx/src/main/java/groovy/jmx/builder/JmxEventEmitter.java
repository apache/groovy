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
package groovy.jmx.builder;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The JmxEventEmitter is a JMX Broadcaster class that is used to send generic events on the MBeanServer's
 * event bus. It is used by the Emitter node () to send event to registered listeners.
 * <p>
 * <pre>
 * def jmx = JmxBuilder()
 * jmx.emitter(name:"Object name"|ObjectName(), event:"event type")
 * ...
 * jmx.emitter.send(object)
 * </pre>
 *
 * @see groovy.jmx.builder.JmxEmitterFactory
 */
public class JmxEventEmitter extends NotificationBroadcasterSupport implements JmxEventEmitterMBean {
    private String event;
    private String message;

    /**
     * Event type getter
     *
     * @return - returns event type string thrown by this emitter
     */
    public String getEvent() {
        return event;
    }

    /**
     * Event type setter
     *
     * @param event - event type set for this emitter.
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Event message getter
     *
     * @return - message that is associated with event.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Event message setter.
     *
     * @param message - message that is associated with event emitted.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Called to broadcast message on MBeanServer event bus.  Internally, it calls
     * NotificationBroadCasterSupport.sendNotification() method to dispatch the event.
     *
     * @param data - a data object sent as part of the event parameter.
     * @return a sequence number associated with the emitted event.
     */
    public long send(Object data) {
        long seq = NumberSequencer.getNextSequence();
        Notification note = new Notification(
                this.getEvent(),
                this,
                seq,
                System.currentTimeMillis(),
                "Event notification " + this.getEvent()
        );
        note.setUserData(data);
        super.sendNotification(note);
        return seq;
    }

    private static class NumberSequencer {
        private static final AtomicLong num = new AtomicLong(0);

        public static long getNextSequence() {
            return num.incrementAndGet();
        }
    }
}
