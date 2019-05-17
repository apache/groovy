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
package groovy.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A simple ValueModel implementation which is a holder of an object value. 
 * Used to share local variables with closures
 */
public class ValueHolder implements ValueModel {
    private Object value;
    private final Class type;
    private PropertyChangeSupport propertyChangeSupport;
    private boolean editable = true;

    public ValueHolder() {
        this(Object.class);
    }
    
    public ValueHolder(Class type) {
        this.type = type;
    }
    
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
    

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        if ( propertyChangeSupport != null ) {
            propertyChangeSupport.firePropertyChange("value", oldValue, value);
        }
    }

    public Class getType() {
        return type;
    }

    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
