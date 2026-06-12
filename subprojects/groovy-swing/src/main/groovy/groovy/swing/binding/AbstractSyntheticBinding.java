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
package groovy.swing.binding;

import org.apache.groovy.swing.binding.AbstractFullBinding;
import org.apache.groovy.swing.binding.PropertyBinding;
import org.apache.groovy.swing.binding.SourceBinding;
import org.apache.groovy.swing.binding.TargetBinding;

/**
 * Base class for bindings that observe synthetic Swing properties.
 */
public abstract class AbstractSyntheticBinding extends AbstractFullBinding {
    /**
     * Indicates whether the synthetic binding is currently active.
     */
    boolean bound;
    /**
     * The synthetic property name handled by this binding.
     */
    String propertyName;
    /**
     * The Swing component type supported by this binding.
     */
    Class<?> klass;

    /**
     * Creates a synthetic binding for the supplied property and component type.
     *
     * @param source the source property binding
     * @param target the target binding
     * @param klass the supported component type
     * @param propertyName the synthetic property name
     */
    @SuppressWarnings("rawtypes")
    public AbstractSyntheticBinding(PropertyBinding source, TargetBinding target, Class klass, String propertyName) {
        this.propertyName = propertyName;
        this.klass = klass;
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    /**
     * Installs the synthetic listeners if this binding is not already active.
     */
    @Override
    public void bind() {
        if (!bound) {
            try {
                syntheticBind();
                bound = true;
            } catch (RuntimeException re) {
                try {
                    syntheticUnbind();
                } catch (Exception e) {
                    // ignore as we are re-throwing the original cause
                }
                throw re;
            }
        }
    }

    /**
     * Removes the synthetic listeners if this binding is currently active.
     */
    @Override
    public void unbind() {
        if (bound) {
            // fail dirty, no checks
            bound = false;
            syntheticUnbind();
        }
    }

    /**
     * Hooks the synthetic listeners required by this binding.
     */
    protected abstract void syntheticBind();
    /**
     * Removes any synthetic listeners installed by this binding.
     */
    protected abstract void syntheticUnbind();

    /**
     * Reinstalls the synthetic listeners when the binding is already active.
     */
    @Override
    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    /**
     * Validates and stores the source binding for this synthetic property.
     *
     * @param source the source binding to validate
     */
    @Override
    public void setSourceBinding(SourceBinding source) {
        if (!(source instanceof PropertyBinding)) {
            throw new IllegalArgumentException("Only PropertySourceBindings are accepted");
        }

        if (!propertyName.equals(((PropertyBinding) source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be '" + propertyName + "'");
        }
        Object bean = ((PropertyBinding) source).getBean();
        if ((bean == null) || !klass.isAssignableFrom(bean.getClass())) {
            throw new IllegalArgumentException("SourceBean must be a " + klass.getName());
        }
        super.setSourceBinding(source);
    }

    /**
     * Stores the target binding that receives propagated updates.
     *
     * @param target the target binding
     */
    @Override
    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }
}
