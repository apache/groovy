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

/**
 * This is the management interface for JmxEventEmitter.  This MBean is used internally by
 * JmxBuilder to broadcast events to registered listeners of the underlying MBeanServer.
 *
 * @see groovy.jmx.builder.JmxEventEmitter
 */
public interface JmxEventEmitterMBean {
    /**
     * Getter - returns event thrown by emitter.
     *
     * @return event type string
     */
    String getEvent();

    /**
     * Setter - sets event thrown by Emitter.
     *
     * @param event - event type string
     */
    void setEvent(String event);

    /**
     * Method called to dispatch event on event bus.
     *
     * @param data - data sent to listeners.
     * @return - a sequence id generated for event.
     */
    long send(Object data);
}
