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
package groovy.util;

import groovy.lang.Closure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map decorator that will trigger PropertyChangeEvents when a value changes.<br>
 * An optional Closure may be specified and will work as a filter, if it returns
 * true the property will trigger an event (if the value indeed changed),
 * otherwise it won't. The Closure may receive 1 or 2 parameters, the single one
 * being the value, the other one both the key and value, for example:
 * <pre>
 * // skip all properties whose value is a closure
 * def map = new ObservableMap( {!(it instanceof Closure)} )
 *
 * // skip all properties whose name matches a regex
 * def map = new ObservableMap( { name, value -&gt; !(name =~ /[A-Z+]/) } )
 * </pre>
 * The current implementation will trigger specialized events in the following scenarios,
 * you need not register a different listener as those events extend from PropertyChangeEvent
 * <ul>
 * <li>ObservableMap.PropertyAddedEvent - a new property is added to the map</li>
 * <li>ObservableMap.PropertyRemovedEvent - a property is removed from the map</li>
 * <li>ObservableMap.PropertyUpdatedEvent - a property changes value (same as regular PropertyChangeEvent)</li>
 * <li>ObservableMap.PropertyClearedEvent - all properties have been removed from the map</li>
 * <li>ObservableMap.MultiPropertyEvent - triggered by calling map.putAll(), contains Added|Updated events</li>
 * </ul>
 * <p>
 * <strong>Bound properties</strong>
 * <ul>
 * <li><tt>content</tt> - read-only.</li>
 * <li><tt>size</tt> - read-only.</li>
 * </ul>
 */
public class ObservableMap implements Map {
    private final Map delegate;
    private final PropertyChangeSupport pcs;
    private final Closure test;

    public static final String SIZE_PROPERTY = "size";
    public static final String CONTENT_PROPERTY = "content";
    public static final String CLEARED_PROPERTY = "cleared";

    public ObservableMap() {
        this(new LinkedHashMap(), null);
    }

    public ObservableMap(Closure test) {
        this(new LinkedHashMap(), test);
    }

    public ObservableMap(Map delegate) {
        this(delegate, null);
    }

    public ObservableMap(Map delegate, Closure test) {
        this.delegate = delegate;
        this.test = test;
        pcs = new PropertyChangeSupport(this);
    }

    protected Map getMapDelegate() {
        return delegate;
    }

    protected Closure getTest() {
        return test;
    }

    public Map getContent() {
        return Collections.unmodifiableMap(delegate);
    }

    protected void firePropertyClearedEvent(Map values) {
        firePropertyEvent(new PropertyClearedEvent(this, values));
    }

    protected void firePropertyAddedEvent(Object key, Object value) {
        firePropertyEvent(new PropertyAddedEvent(this, String.valueOf(key), value));
    }

    protected void firePropertyUpdatedEvent(Object key, Object oldValue, Object newValue) {
        firePropertyEvent(new PropertyUpdatedEvent(this, String.valueOf(key), oldValue, newValue));
    }

    protected void fireMultiPropertyEvent(List<PropertyEvent> events) {
        firePropertyEvent(new MultiPropertyEvent(this, (PropertyEvent[]) events.toArray(new PropertyEvent[0])));
    }

    protected void fireMultiPropertyEvent(PropertyEvent[] events) {
        firePropertyEvent(new MultiPropertyEvent(this, events));
    }

    protected void firePropertyRemovedEvent(Object key, Object value) {
        firePropertyEvent(new PropertyRemovedEvent(this, String.valueOf(key), value));
    }

    protected void firePropertyEvent(PropertyEvent event) {
        pcs.firePropertyChange(event);
    }

    protected void fireSizeChangedEvent(int oldValue, int newValue) {
        pcs.firePropertyChange(new PropertyChangeEvent(this, SIZE_PROPERTY, oldValue, newValue));
    }

    // Map interface

    public void clear() {
        int oldSize = size();
        Map values = new HashMap();
        if (!delegate.isEmpty()) {
            values.putAll(delegate);
        }
        delegate.clear();
        firePropertyClearedEvent(values);
        fireSizeChangedEvent(oldSize, size());
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public Set entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Object get(Object key) {
        return delegate.get(key);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set keySet() {
        return delegate.keySet();
    }

    public Object put(Object key, Object value) {
        int oldSize = size();
        Object oldValue = null;
        boolean newKey = !delegate.containsKey(key);
        if (test != null) {
            oldValue = delegate.put(key, value);
            Object result = null;
            if (test.getMaximumNumberOfParameters() == 2) {
                result = test.call(key, value);
            } else {
                result = test.call(value);
            }
            if (result instanceof Boolean && (Boolean) result) {
                if (newKey) {
                    firePropertyAddedEvent(key, value);
                    fireSizeChangedEvent(oldSize, size());
                } else if (oldValue != value) {
                    firePropertyUpdatedEvent(key, oldValue, value);
                }
            }
        } else {
            oldValue = delegate.put(key, value);
            if (newKey) {
                firePropertyAddedEvent(key, value);
                fireSizeChangedEvent(oldSize, size());
            } else if (oldValue != value) {
                firePropertyUpdatedEvent(key, oldValue, value);
            }
        }
        return oldValue;
    }

    public void putAll(Map map) {
        int oldSize = size();
        if (map != null) {
            List<PropertyEvent> events = new ArrayList<>();
            for (Object o : map.entrySet()) {
                Entry entry = (Entry) o;

                String key = String.valueOf(entry.getKey());
                Object newValue = entry.getValue();
                Object oldValue = null;

                boolean newKey = !delegate.containsKey(key);
                if (test != null) {
                    oldValue = delegate.put(key, newValue);
                    Object result = null;
                    if (test.getMaximumNumberOfParameters() == 2) {
                        result = test.call(key, newValue);
                    } else {
                        result = test.call(newValue);
                    }
                    if (result instanceof Boolean && (Boolean) result) {
                        if (newKey) {
                            events.add(new PropertyAddedEvent(this, key, newValue));
                        } else if (oldValue != newValue) {
                            events.add(new PropertyUpdatedEvent(this, key, oldValue, newValue));
                        }
                    }
                } else {
                    oldValue = delegate.put(key, newValue);
                    if (newKey) {
                        events.add(new PropertyAddedEvent(this, key, newValue));
                    } else if (oldValue != newValue) {
                        events.add(new PropertyUpdatedEvent(this, key, oldValue, newValue));
                    }
                }
            }
            if (!events.isEmpty()) {
                fireMultiPropertyEvent(events);
                fireSizeChangedEvent(oldSize, size());
            }
        }
    }

    public Object remove(Object key) {
        int oldSize = size();
        Object result = delegate.remove(key);
        if (key != null) {
            firePropertyRemovedEvent(key, result);
            fireSizeChangedEvent(oldSize, size());
        }
        return result;
    }

    public int size() {
        return delegate.size();
    }

    public int getSize() {
        return size();
    }

    public Collection values() {
        return delegate.values();
    }

    // observable interface

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public enum ChangeType {
        ADDED, UPDATED, REMOVED, CLEARED, MULTI, NONE;

        public static final Object oldValue = new Object[0];
        public static final Object newValue = new Object[0];

        public static ChangeType resolve(int ordinal) {
            switch (ordinal) {
                case 0:
                    return ADDED;
                case 2:
                    return REMOVED;
                case 3:
                    return CLEARED;
                case 4:
                    return MULTI;
                case 5:
                    return NONE;
                case 1:
                default:
                    return UPDATED;
            }
        }
    }

    public abstract static class PropertyEvent extends PropertyChangeEvent {
        private static final long serialVersionUID = -8328412226044328674L;
        private ChangeType type;

        public PropertyEvent(Object source, String propertyName, Object oldValue, Object newValue, ChangeType type) {
            super(source, propertyName, oldValue, newValue);
            this.type = type;
        }

        public int getType() {
            return type.ordinal();
        }

        public ChangeType getChangeType() {
            return type;
        }

        public String getTypeAsString() {
            return type.name().toUpperCase();
        }
    }

    public static class PropertyAddedEvent extends PropertyEvent {
        private static final long serialVersionUID = -5761685843732329868L;

        public PropertyAddedEvent(Object source, String propertyName, Object newValue) {
            super(source, propertyName, null, newValue, ChangeType.ADDED);
        }
    }

    public static class PropertyUpdatedEvent extends PropertyEvent {
        private static final long serialVersionUID = -1104637722950032690L;

        public PropertyUpdatedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
            super(source, propertyName, oldValue, newValue, ChangeType.UPDATED);
        }
    }

    public static class PropertyRemovedEvent extends PropertyEvent {
        private static final long serialVersionUID = 1882656655856158470L;

        public PropertyRemovedEvent(Object source, String propertyName, Object oldValue) {
            super(source, propertyName, oldValue, null, ChangeType.REMOVED);
        }
    }

    public static class PropertyClearedEvent extends PropertyEvent {
        private static final long serialVersionUID = -1472110679547513634L;
        private Map values = new HashMap();

        public PropertyClearedEvent(Object source, Map values) {
            super(source, ObservableMap.CLEARED_PROPERTY, values, null, ChangeType.CLEARED);
            if (values != null) {
                this.values.putAll(values);
            }
        }

        public Map getValues() {
            return Collections.unmodifiableMap(values);
        }
    }

    public static class MultiPropertyEvent extends PropertyEvent {
        public static final String MULTI_PROPERTY = "groovy_util_ObservableMap_MultiPropertyEvent_MULTI";
        private static final PropertyEvent[] EMPTY_PROPERTY_EVENTS = new PropertyEvent[0];
        private static final long serialVersionUID = 3925136810810084267L;

        private final PropertyEvent[] events;

        public MultiPropertyEvent(Object source, PropertyEvent[] events) {
            super(source, MULTI_PROPERTY, ChangeType.oldValue, ChangeType.newValue, ChangeType.MULTI);
            if (events != null && events.length > 0) {
                this.events = new PropertyEvent[events.length];
                System.arraycopy(events, 0, this.events, 0, events.length);
            } else {
            	this.events = EMPTY_PROPERTY_EVENTS;
            }
        }

        public PropertyEvent[] getEvents() {
            PropertyEvent[] copy = new PropertyEvent[events.length];
            System.arraycopy(events, 0, copy, 0, events.length);
            return copy;
        }
    }
}
