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
package org.apache.groovy.swing.binding;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Creates bindings that update whenever a named event handler property fires.
 *
 * @since Groovy 1.1
 */
public class EventTriggerBinding implements TriggerBinding {

    /**
     * The bean that exposes the event handler property.
     */
    Object triggerBean;
    /**
     * The event handler property name to assign.
     */
    String eventName;

    /**
     * Creates a trigger binding for the supplied bean event property.
     *
     * @param triggerBean the bean that exposes the event property
     * @param eventName the event property name
     */
    public EventTriggerBinding(Object triggerBean, String eventName) {
        this.triggerBean = triggerBean;
        this.eventName = eventName;
    }

    /**
     * Creates a full binding that reacts when the event handler is invoked.
     *
     * @param sourceBinding the source binding to read from
     * @param targetBinding the target binding to update
     * @return the created event-triggered binding
     */
    @Override
    public FullBinding createBinding(final SourceBinding sourceBinding, final TargetBinding targetBinding) {
        return new EventTriggerFullBinding(sourceBinding, targetBinding);
    }

    /**
     * Returns the bean that exposes the event handler property.
     *
     * @return the trigger bean
     */
    public Object getTriggerBean() {
        return triggerBean;
    }

    /**
     * Replaces the bean that exposes the event handler property.
     *
     * @param triggerBean the new trigger bean
     */
    public void setTriggerBean(Object triggerBean) {
        this.triggerBean = triggerBean;
    }

    /**
     * Returns the event handler property name.
     *
     * @return the event property name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Replaces the event handler property name.
     *
     * @param eventName the new event property name
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    private class EventTriggerFullBinding extends AbstractFullBinding {

        Closure handler;

        EventTriggerFullBinding(final SourceBinding sourceBinding, TargetBinding targetBinding) {
            setSourceBinding(sourceBinding);
            setTargetBinding(targetBinding);
            handler = new Closure(triggerBean) {
                @Override
                public Object call(Object... params) {
                    if (sourceBinding instanceof ClosureSourceBinding) {
                        ((ClosureSourceBinding)sourceBinding).setClosureArguments(params);
                    }
                    update();
                    return null;
                }
            };
        }

        @Override
        public void bind() {
            InvokerHelper.setProperty(triggerBean, eventName, handler);
        }

        @Override
        public void unbind() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rebind() {
            throw new UnsupportedOperationException();
        }
    }
}
