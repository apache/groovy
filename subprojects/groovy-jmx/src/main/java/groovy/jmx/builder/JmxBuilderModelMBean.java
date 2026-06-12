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

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The JmxBuilderModelMBean is the MBean class that proxies exported POGO/POJO inside the MBeanServer.
 * When JmxBuilder exports an object instance, an instance of this class is created and exported inside the
 * MBeanServer.
 */
public class JmxBuilderModelMBean extends RequiredModelMBean implements NotificationListener {
    private final List<String> methodListeners = new ArrayList<String>(0);
    private Object managedObject;

    /**
     * Creates a model MBean for the supplied managed resource.
     *
     * @param objectRef the resource to expose
     * @throws MBeanException if the resource cannot be registered
     * @throws RuntimeOperationsException if the supplied resource is invalid
     * @throws InstanceNotFoundException if the resource cannot be found
     * @throws InvalidTargetObjectTypeException if the resource type is unsupported
     */
    public JmxBuilderModelMBean(Object objectRef) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException {
        super.setManagedResource(objectRef, "ObjectReference");
    }

    /**
     * Creates an empty model MBean.
     *
     * @throws MBeanException if initialization fails
     * @throws RuntimeOperationsException if the MBean cannot be initialized
     */
    public JmxBuilderModelMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }

    /**
     * Creates a model MBean backed by the supplied metadata.
     *
     * @param mbi the model MBean metadata
     * @throws MBeanException if initialization fails
     * @throws RuntimeOperationsException if the MBean cannot be initialized
     */
    public JmxBuilderModelMBean(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
        super(mbi);
    }

    /**
     * Updates the managed resource exposed by this model MBean.
     *
     * @param obj the resource to expose
     */
    public synchronized void setManagedResource(Object obj) {
        managedObject = obj;
        try {
            super.setManagedResource(obj, "ObjectReference");
        } catch (Exception ex) {
            throw new JmxBuilderException(ex);
        }
    }

    /**
     * Registers listeners for operation calls (i.e. method, getter, and setter calls) when
     * invoked on this bean from the MBeanServer.  Descriptor should contain a map with layout
     * {@code item -> [Map[methodListener:[target:"", tpe:"", callback:&Closure], ... ,]]}
     *
     * @param descriptor MetaMap descriptor containing description of operation call listeners
     */
    public void addOperationCallListeners(Map<String, Map<String, Map<String, Object>>> descriptor) {
        if (descriptor == null) return;
        for (Map.Entry<String, Map<String, Map<String, Object>>> item : descriptor.entrySet()) {
            // set up method listeners (such as attributeListener and Operation Listeners)
            // item -> [Map[methodListener:[target:"", tpe:"", callback:&Closure], ... ,]]
            if (item.getValue().containsKey("methodListener")) {
                Map<String, Object> listener = item.getValue().get("methodListener");
                String target = (String) listener.get("target");
                methodListeners.add(target);
                String listenerType = (String) listener.get("type");
                listener.put("managedObject", this.managedObject);
                // register an attribute change notification listener with model mbean
                if ("attributeChangeListener".equals(listenerType)) {
                    try {
                        this.addAttributeChangeNotificationListener(
                                AttributeChangedListener.getListener(), (String) listener.get("attribute"), listener
                        );
                    } catch (MBeanException e) {
                        throw new JmxBuilderException(e);
                    }
                }
                if ("operationCallListener".equals(listenerType)) {
                    String eventType = "jmx.operation.call." + target;
                    NotificationFilterSupport filter = new NotificationFilterSupport();
                    filter.enableType(eventType);
                    this.addNotificationListener(JmxEventListener.getListener(), filter, listener);
                }
            }
        }
    }

    /**
     * Sets up event listeners for this MBean as described in the descriptor.
     * The descriptor contains a map with layout
     * {item -&gt; Map[event:"...", from:ObjectName, callback:&amp;Closure],...,}
     *
     * @param server     the MBeanServer is to be registered.
     * @param descriptor a map containing info about the event
     */
    public void addEventListeners(MBeanServer server, Map<String, Map<String, Object>> descriptor) {
        for (Map.Entry<String, Map<String, Object>> item : descriptor.entrySet()) {
            Map<String, Object> listener = item.getValue();

            // register with server
            ObjectName broadcaster = (ObjectName) listener.get("from");
            try {
                String eventType = (String) listener.get("event");

                if (eventType != null) {
                    NotificationFilterSupport filter = new NotificationFilterSupport();
                    filter.enableType(eventType);
                    server.addNotificationListener(broadcaster, JmxEventListener.getListener(), filter, listener);
                } else {
                    server.addNotificationListener(broadcaster, JmxEventListener.getListener(), null, listener);
                }
            } catch (InstanceNotFoundException e) {
                throw new JmxBuilderException(e);
            }
        }
    }

    /**
     * Invokes the named operation and emits any configured call notifications.
     *
     * @param opName the operation name
     * @param opArgs the invocation arguments
     * @param signature the argument signature
     * @return the invocation result
     * @throws MBeanException if the invocation fails
     * @throws ReflectionException if the method cannot be resolved
     */
    @Override
    public Object invoke(String opName, Object[] opArgs, String[] signature) throws MBeanException, ReflectionException {
        Object result = super.invoke(opName, opArgs, signature);
        if (methodListeners.contains(opName)) {
            this.sendNotification(buildCallListenerNotification(opName));
        }
        return result;
    }

    /**
     * Handles notifications received by this model MBean.
     *
     * @param note the received notification
     * @param handback the handback object supplied at registration time
     */
    @Override
    public void handleNotification(Notification note, Object handback) {
        //System.out.println("Received note!");
    }

    private Notification buildCallListenerNotification(String target) {
        return new Notification(
                "jmx.operation.call." + target,
                this,
                NumberSequencer.getNextSequence(),
                System.currentTimeMillis()
        );
    }


    private static final class NumberSequencer {
        private static final AtomicLong NUM = new AtomicLong(0);

        /**
         * Returns the next notification sequence number.
         *
         * @return the next sequence number
         */
        public static long getNextSequence() {
            return NUM.incrementAndGet();
        }
    }

    /**
     * Internal class AttributeChangedListener provides hooks to handle attribute-change events
     * that occurs on registered MBeans.
     *
     * @see groovy.jmx.builder.JmxBuilderModelMBean
     */
    private static final class AttributeChangedListener implements NotificationListener {
        private static AttributeChangedListener listener;

        /**
         * Returns an instance of the AttributeChangedListener.
         *
         * @return the listener
         */
        public static synchronized AttributeChangedListener getListener() {
            if (listener == null) {
                listener = new AttributeChangedListener();
            }
            return listener;
        }

        private AttributeChangedListener() {
        }

        /**
         * Dispatches an attribute-change notification to the configured callback closure.
         *
         * @param notification the attribute-change notification to process
         * @param handback the listener descriptor containing callback metadata
         */
        @Override
        public void handleNotification(Notification notification, Object handback) {
            AttributeChangeNotification note = (AttributeChangeNotification) notification;
            Map event = (Map) handback;
            if (event != null) {
                Object del = event.get("managedObject");
                Object callback = event.get("callback");
                if (callback != null && callback instanceof Closure closure) {
                    closure.setDelegate(del);
                    if (closure.getMaximumNumberOfParameters() == 1)
                        closure.call(buildAttributeNotificationPacket(note));
                    else closure.call();
                }
            }
        }

        private static Map buildAttributeNotificationPacket(AttributeChangeNotification note) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("oldValue", note.getOldValue());
            result.put("newValue", note.getNewValue());
            result.put("attribute", note.getAttributeName());
            result.put("attributeType", note.getAttributeType());
            result.put("sequenceNumber", note.getSequenceNumber());
            result.put("timeStamp", note.getTimeStamp());
            return result;
        }
    }
}
