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
package groovy.swing.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A mutable {@link ValueModel} implementation that stores a local value and optionally emits change events.
 */
public class ValueHolder implements ValueModel {
    private Object value;
    private final Class type;
    private PropertyChangeSupport propertyChangeSupport;
    private boolean editable = true;

    /**
     * Creates a holder with {@link Object} as its declared type.
     */
    public ValueHolder() {
        this(Object.class);
    }

    /**
     * Creates a holder with the supplied declared type.
     *
     * @param type the declared value type
     */
    public ValueHolder(Class type) {
        this.type = type;
    }

    /**
     * Creates a holder initialized with the supplied value.
     *
     * @param value the initial value
     */
    public ValueHolder(Object value) {
        this.value = value;
        this.type = (value != null) ? value.getClass() : Object.class;
    }

    /** 
     * Add a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if ( propertyChangeSupport == null ) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /** 
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if ( propertyChangeSupport != null ) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }


    /**
     * Returns the current stored value.
     *
     * @return the current value
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Updates the current value and notifies registered listeners of the change.
     *
     * @param value the new value
     */
    @Override
    public void setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        if ( propertyChangeSupport != null ) {
            propertyChangeSupport.firePropertyChange("value", oldValue, value);
        }
    }

    /**
     * Returns the declared value type for this holder.
     *
     * @return the holder type
     */
    @Override
    public Class getType() {
        return type;
    }

    /**
     * Indicates whether callers should treat this holder as writable.
     *
     * @return {@code true} when the holder is editable
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Controls whether callers should treat this holder as writable.
     *
     * @param editable {@code true} to mark the holder as editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
