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

import groovy.lang.Closure;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The JmxEventListener class is used by the builder to listen to events on the event bus.  It is used internally
 * by JmxBuilder to handle attribute-change and operation-invoke events on the bean() node.
 *
 * @see groovy.jmx.builder.JmxBeanFactory
 */
public class JmxEventListener implements NotificationListener {
    private static JmxEventListener listener;

    /**
     * Factory method that returns an instance of the listener.
     *
     * @return - JmxEventListener instance.
     */
    public static synchronized JmxEventListener getListener() {
        if (listener == null) {
            listener = new JmxEventListener();
        }
        return listener;
    }

    /**
     * This is the implemented method for NotificationListener.  It is called by an event emitter to dispatch
     * JMX events to listeners.  Here it handles internal JmxBuilder events.
     *
     * @param notification the notification object passed to closure used to handle JmxBuilder events.
     * @param handback     - In this case, the handback is the closure to execute when the event is handled.
     */
    @Override
    public void handleNotification(Notification notification, Object handback) {
        Map event = (Map) handback;
        if (event != null) {
            Object del = event.get("managedObject");
            Object callback = event.get("callback");
            if (callback != null && callback instanceof Closure) {
                Closure closure = (Closure) callback;
                closure.setDelegate(del);
                if (closure.getMaximumNumberOfParameters() == 1)
                    closure.call(buildOperationNotificationPacket(notification));
                else closure.call();
            }
        }
    }

    private static Map buildOperationNotificationPacket(Notification note) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("event", note.getType());
        result.put("source", note.getSource());
        result.put("sequenceNumber", note.getSequenceNumber());
        result.put("timeStamp", note.getTimeStamp());
        result.put("data", note.getUserData());
        return result;
    }
}
