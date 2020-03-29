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

    Object model;
    boolean bound;

    final Map<String, PropertyBinding> propertyBindings = new HashMap<String, PropertyBinding>();
    final List<FullBinding> generatedBindings = new ArrayList<FullBinding>();

    public BindingProxy(Object model) {
        this.model = model;
    }

    public synchronized Object getModel() {
        return model;
    }

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

    public void setProperty(String property, Object value) {
        throw new ReadOnlyPropertyException(property, getModel().getClass());
    }

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

    public void update() {
        synchronized (generatedBindings) {
            for (FullBinding generatedBinding : generatedBindings) {
                (generatedBinding).update();
                // should we trap exceptions and do an each?
            }
        }
    }

    public void reverseUpdate() {
        synchronized (generatedBindings) {
            for (FullBinding generatedBinding : generatedBindings) {
                (generatedBinding).reverseUpdate();
                // should we trap exceptions and do an each?
            }
        }
    }

    class ModelBindingPropertyBinding extends PropertyBinding {
        public ModelBindingPropertyBinding(Object bean, String propertyName) {
            super(bean, propertyName);
        }

        public FullBinding createBinding(SourceBinding source, TargetBinding target) {
            FullBinding fb = super.createBinding(source, target);
            generatedBindings.add(fb);
            return fb;
        }
    }

}
