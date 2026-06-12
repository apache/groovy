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

import groovy.lang.GroovyObjectSupport;
import groovy.lang.ReadOnlyPropertyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class returns half bound {@link org.apache.groovy.swing.binding.FullBinding}s on the source half to the model
 * object for every property reference (and I do mean every, valid or not, queried before or not). These returned
 * half bindings are stored strongly in a list when generated.
 *
 * Changing the model will keep all existing bindings but change the source on all of the bindings.
 *
 * Formerly Known as Model Binding.
 *
 * @since Groovy 1.5
 */
public class BindingProxy extends GroovyObjectSupport implements BindingUpdatable {

    /**
     * The current model object exposed through generated bindings.
     */
    Object model;
    /**
     * Indicates whether generated bindings are currently active.
     */
    boolean bound;

    /**
     * Cached property bindings keyed by property name.
     */
    final Map<String, PropertyBinding> propertyBindings = new HashMap<String, PropertyBinding>();
    /**
     * All generated full bindings created through this proxy.
     */
    final List<FullBinding> generatedBindings = new ArrayList<FullBinding>();

    /**
     * Creates a proxy for the supplied model object.
     *
     * @param model the model object to expose
     */
    public BindingProxy(Object model) {
        this.model = model;
    }

    /**
     * Returns the current model object.
     *
     * @return the current model
     */
    public synchronized Object getModel() {
        return model;
    }

    /**
     * Replaces the proxied model and retargets all existing property bindings.
     *
     * @param model the new model object
     */
    public synchronized void setModel(Object model) {
        // should we use a finer grained lock than this?

        boolean bindAgain = bound;
        this.model = model;

        unbind();
        for (PropertyBinding propertyBinding : propertyBindings.values()) {
            (propertyBinding).setBean(model);
        }

        if (bindAgain) {
            bind();
            update();
        }
    }

    /**
     * Lazily creates and returns a half binding for the requested model property.
     *
     * @param property the property name to bind
     * @return a generated half binding rooted at the requested property
     */
    @Override
    public Object getProperty(String property) {
        PropertyBinding pb;
        final Object model = getModel();
        synchronized (propertyBindings) {
            // should we verify the property is valid?
            pb = propertyBindings.get(property);
            if (pb == null) {
                pb = new ModelBindingPropertyBinding(model, property);
                propertyBindings.put(property, pb);
            }
        }
        FullBinding fb = pb.createBinding(pb, null);
        if (bound) {
            fb.bind();
        }
        return fb;
    }

    /**
     * Prevents direct writes to the proxy.
     *
     * @param property the property name being written
     * @param value the attempted value
     */
    @Override
    public void setProperty(String property, Object value) {
        throw new ReadOnlyPropertyException(property, getModel().getClass());
    }

    /**
     * Binds every generated full binding managed by this proxy.
     */
    @Override
    public void bind() {
        synchronized (generatedBindings) {
            if (!bound) {
                bound = true;
                for (FullBinding generatedBinding : generatedBindings) {
                    (generatedBinding).bind();
                    // should we trap exceptions and do an each?
                }
            }
        }
    }

    /**
     * Unbinds every generated full binding managed by this proxy.
     */
    @Override
    public void unbind() {
        synchronized (generatedBindings) {
            if (bound) {
                bound = false;
                for (FullBinding generatedBinding : generatedBindings) {
                    (generatedBinding).unbind();
                    // should we trap exceptions and do an each?
                }
            }
        }
    }

    /**
     * Rebinds every generated full binding while preserving the current bound state.
     */
    @Override
    public void rebind() {
        synchronized (generatedBindings) {
            if (bound) {
                for (FullBinding generatedBinding : generatedBindings) {
                    (generatedBinding).rebind();
                    // should we trap exceptions and do an each?
                }
            }
        }
    }

    /**
     * Pushes updates through every generated full binding.
     */
    @Override
    public void update() {
        synchronized (generatedBindings) {
            for (FullBinding generatedBinding : generatedBindings) {
                (generatedBinding).update();
                // should we trap exceptions and do an each?
            }
        }
    }

    /**
     * Pushes reverse updates through every generated full binding.
     */
    @Override
    public void reverseUpdate() {
        synchronized (generatedBindings) {
            for (FullBinding generatedBinding : generatedBindings) {
                (generatedBinding).reverseUpdate();
                // should we trap exceptions and do an each?
            }
        }
    }

    /**
     * Tracks generated bindings created for individual model properties.
     */
    class ModelBindingPropertyBinding extends PropertyBinding {
        /**
         * Creates a property binding for the current model and property.
         *
         * @param bean the current model object
         * @param propertyName the property to expose
         */
        ModelBindingPropertyBinding(Object bean, String propertyName) {
            super(bean, propertyName);
        }

        /**
         * Creates a full binding and records it for later lifecycle management.
         *
         * @param source the binding source
         * @param target the binding target
         * @return the created full binding
         */
        @Override
        public FullBinding createBinding(SourceBinding source, TargetBinding target) {
            FullBinding fb = super.createBinding(source, target);
            generatedBindings.add(fb);
            return fb;
        }
    }

}
