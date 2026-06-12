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
import java.io.Serial;
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

    /** Bound property name for map size changes. */
    public static final String SIZE_PROPERTY = "size";
    /** Bound property name for content changes. */
    public static final String CONTENT_PROPERTY = "content";
    /** Bound property name used for clear events. */
    public static final String CLEARED_PROPERTY = "cleared";

    /** Creates an observable map backed by a {@link LinkedHashMap}. */
    public ObservableMap() {
        this(new LinkedHashMap(), null);
    }

    /**
     * Creates an observable map backed by a {@link LinkedHashMap}.
     *
     * @param test optional event filter
     */
    public ObservableMap(Closure test) {
        this(new LinkedHashMap(), test);
    }

    /**
     * Creates an observable map backed by the supplied delegate.
     *
     * @param delegate the backing map
     */
    public ObservableMap(Map delegate) {
        this(delegate, null);
    }

    /**
     * Creates an observable map backed by the supplied delegate.
     *
     * @param delegate the backing map
     * @param test optional event filter
     */
    public ObservableMap(Map delegate, Closure test) {
        this.delegate = delegate;
        this.test = test;
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Returns the mutable backing map.
     *
     * @return the delegate map
     */
    protected Map getMapDelegate() {
        return delegate;
    }

    /**
     * Returns the optional event filter closure.
     *
     * @return the event filter, or {@code null}
     */
    protected Closure getTest() {
        return test;
    }

    /**
     * Returns an unmodifiable snapshot view of the backing map.
     *
     * @return the map content
     */
    public Map getContent() {
        return Collections.unmodifiableMap(delegate);
    }

    /** Fires a map-cleared event. */
    protected void firePropertyClearedEvent(Map values) {
        firePropertyEvent(new PropertyClearedEvent(this, values));
    }

    /** Fires a property-added event. */
    protected void firePropertyAddedEvent(Object key, Object value) {
        firePropertyEvent(new PropertyAddedEvent(this, String.valueOf(key), value));
    }

    /** Fires a property-updated event. */
    protected void firePropertyUpdatedEvent(Object key, Object oldValue, Object newValue) {
        firePropertyEvent(new PropertyUpdatedEvent(this, String.valueOf(key), oldValue, newValue));
    }

    /** Fires a multi-property event from a list of events. */
    protected void fireMultiPropertyEvent(List<PropertyEvent> events) {
        firePropertyEvent(new MultiPropertyEvent(this, (PropertyEvent[]) events.toArray(new PropertyEvent[0])));
    }

    /** Fires a multi-property event from an event array. */
    protected void fireMultiPropertyEvent(PropertyEvent[] events) {
        firePropertyEvent(new MultiPropertyEvent(this, events));
    }

    /** Fires a property-removed event. */
    protected void firePropertyRemovedEvent(Object key, Object value) {
        firePropertyEvent(new PropertyRemovedEvent(this, String.valueOf(key), value));
    }

    /** Publishes a property event to registered listeners. */
    protected void firePropertyEvent(PropertyEvent event) {
        pcs.firePropertyChange(event);
    }

    /** Fires the bound size change event. */
    protected void fireSizeChangedEvent(int oldValue, int newValue) {
        pcs.firePropertyChange(new PropertyChangeEvent(this, SIZE_PROPERTY, oldValue, newValue));
    }

    // Map interface

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public Set entrySet() {
        return delegate.entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Set keySet() {
        return delegate.keySet();
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void putAll(Map map) {
        int oldSize = size();
        if (map != null) {
            List<PropertyEvent> events = new ArrayList<PropertyEvent>();
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

    /** {@inheritDoc} */
    @Override
    public Object remove(Object key) {
        int oldSize = size();
        Object result = delegate.remove(key);
        if (key != null) {
            firePropertyRemovedEvent(key, result);
            fireSizeChangedEvent(oldSize, size());
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Returns the current map size as a bound property value.
     *
     * @return the current map size
     */
    public int getSize() {
        return size();
    }

    /** {@inheritDoc} */
    @Override
    public Collection values() {
        return delegate.values();
    }

    // observable interface

    /**
     * Registers a listener for all observable map events.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Registers a listener for a named bound property.
     *
     * @param propertyName the property to observe
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Returns listeners registered for all properties.
     *
     * @return the registered listeners
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /**
     * Returns listeners registered for a named property.
     *
     * @param propertyName the observed property name
     * @return the registered listeners
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * Removes a listener registered for all properties.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Removes a listener registered for a named property.
     *
     * @param propertyName the observed property name
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Reports whether listeners are registered for a named property.
     *
     * @param propertyName the property name to inspect
     * @return {@code true} if listeners are registered
     */
    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    /** Enumerates the specialized map change events. */
    public enum ChangeType {
        /** A property was added. */
        ADDED,
        /** A property was updated. */
        UPDATED,
        /** A property was removed. */
        REMOVED,
        /** The map was cleared. */
        CLEARED,
        /** Multiple property changes were aggregated. */
        MULTI,
        /** No specialized change type applies. */
        NONE;

        /** Placeholder old value for aggregated events. */
        public static final Object oldValue = new Object[0];
        /** Placeholder new value for aggregated events. */
        public static final Object newValue = new Object[0];

        /**
         * Resolves an enum constant from its ordinal.
         *
         * @param ordinal the serialized ordinal value
         * @return the matching change type
         */
        public static ChangeType resolve(int ordinal) {
            return switch (ordinal) {
                case 0 -> ADDED;
                case 2 -> REMOVED;
                case 3 -> CLEARED;
                case 4 -> MULTI;
                case 5 -> NONE;
                default -> UPDATED;
            };
        }
    }

    /**
     * Base event type for observable map changes.
     */
    public abstract static class PropertyEvent extends PropertyChangeEvent {
        @Serial
        private static final long serialVersionUID = -8328412226044328674L;
        private ChangeType type;

        /**
         * Creates a property event.
         *
         * @param source the event source
         * @param propertyName the affected property name
         * @param oldValue the previous value payload
         * @param newValue the new value payload
         * @param type the specialized change type
         */
        public PropertyEvent(Object source, String propertyName, Object oldValue, Object newValue, ChangeType type) {
            super(source, propertyName, oldValue, newValue);
            this.type = type;
        }

        /**
         * Returns the specialized event type ordinal.
         *
         * @return the change type ordinal
         */
        public int getType() {
            return type.ordinal();
        }

        /**
         * Returns the specialized event type.
         *
         * @return the change type
         */
        public ChangeType getChangeType() {
            return type;
        }

        /**
         * Returns the specialized event type name.
         *
         * @return the change type name
         */
        public String getTypeAsString() {
            return type.name().toUpperCase();
        }
    }

    /** Event fired when a property is added. */
    public static class PropertyAddedEvent extends PropertyEvent {
        @Serial
        private static final long serialVersionUID = -5761685843732329868L;

        /**
         * Creates a property-added event.
         *
         * @param source the event source
         * @param propertyName the added property name
         * @param newValue the added value
         */
        public PropertyAddedEvent(Object source, String propertyName, Object newValue) {
            super(source, propertyName, null, newValue, ChangeType.ADDED);
        }
    }

    /** Event fired when a property value changes. */
    public static class PropertyUpdatedEvent extends PropertyEvent {
        @Serial
        private static final long serialVersionUID = -1104637722950032690L;

        /**
         * Creates a property-updated event.
         *
         * @param source the event source
         * @param propertyName the updated property name
         * @param oldValue the previous value
         * @param newValue the new value
         */
        public PropertyUpdatedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
            super(source, propertyName, oldValue, newValue, ChangeType.UPDATED);
        }
    }

    /** Event fired when a property is removed. */
    public static class PropertyRemovedEvent extends PropertyEvent {
        @Serial
        private static final long serialVersionUID = 1882656655856158470L;

        /**
         * Creates a property-removed event.
         *
         * @param source the event source
         * @param propertyName the removed property name
         * @param oldValue the removed value
         */
        public PropertyRemovedEvent(Object source, String propertyName, Object oldValue) {
            super(source, propertyName, oldValue, null, ChangeType.REMOVED);
        }
    }

    /** Event fired when the map is cleared. */
    public static class PropertyClearedEvent extends PropertyEvent {
        @Serial
        private static final long serialVersionUID = -1472110679547513634L;
        private Map values = new HashMap();

        /**
         * Creates a map-cleared event.
         *
         * @param source the event source
         * @param values the removed entries
         */
        public PropertyClearedEvent(Object source, Map values) {
            super(source, ObservableMap.CLEARED_PROPERTY, values, null, ChangeType.CLEARED);
            if (values != null) {
                this.values.putAll(values);
            }
        }

        /**
         * Returns the removed entries.
         *
         * @return an unmodifiable view of removed entries
         */
        public Map getValues() {
            return Collections.unmodifiableMap(values);
        }
    }

    /** Event fired when multiple property changes are aggregated. */
    public static class MultiPropertyEvent extends PropertyEvent {
        /** Synthetic property name used for aggregated map events. */
        public static final String MULTI_PROPERTY = "groovy_util_ObservableMap_MultiPropertyEvent_MULTI";
        private static final PropertyEvent[] EMPTY_PROPERTY_EVENTS = new PropertyEvent[0];
        @Serial
        private static final long serialVersionUID = 3925136810810084267L;

        private final PropertyEvent[] events;

        /**
         * Creates an aggregated property event.
         *
         * @param source the event source
         * @param events the nested property events
         */
        public MultiPropertyEvent(Object source, PropertyEvent[] events) {
            super(source, MULTI_PROPERTY, ChangeType.oldValue, ChangeType.newValue, ChangeType.MULTI);
            if (events != null && events.length > 0) {
                this.events = new PropertyEvent[events.length];
                System.arraycopy(events, 0, this.events, 0, events.length);
            } else {
            	this.events = EMPTY_PROPERTY_EVENTS;
            }
        }

        /**
         * Returns the nested property events.
         *
         * @return a defensive copy of the nested events
         */
        public PropertyEvent[] getEvents() {
            PropertyEvent[] copy = new PropertyEvent[events.length];
            System.arraycopy(events, 0, copy, 0, events.length);
            return copy;
        }
    }
}
