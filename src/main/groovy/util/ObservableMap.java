/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import groovy.lang.Closure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Map decorator that will trigger PropertyChangeEvents when a value changes.<br>
 * An optional Closure may be specified and will work as a filter, if it returns
 * true the property will trigger an event (if the value indeed changed),
 * otherwise it won't. The Closure may receive 1 or 2 parameters, the single one
 * being the value, the other one both the key and value, for example:
 * <pre>
 * // skip all properties whose value is a closure
 * def map = new ObservableMap( {!(it instanceof Closure)} )
 * <p/>
 * // skip all properties whose name matches a regex
 * def map = new ObservableMap( { name, value -&gt; !(name =~ /[A-Z+]/) } )
 * </pre>
 *
 * <p>The current implementation will trigger specialized events in the following scenarios,
 * you need not register a different listener as those events extend from PropertyChangeEvent
 * <ul>
 * <li>ObservableMap.PropertyAddedEvent - a new property is added to the map</li>
 * <li>ObservableMap.PropertyRemovedEvent - a property is removed from the map</li>
 * <li>ObservableMap.PropertyUpdatedEvent - a property changes value (same as regular PropertyChangeEvent)</li>
 * <li>ObservableMap.PropertyClearedEvent - all properties have been removed from the map</li>
 * <li>ObservableMap.MultiPropertyEvent - triggered by calling map.putAll(), contains Added|Updated events</li>
 * </ul></p>
 *
 * @author <a href="mailto:aalmiray@users.sourceforge.net">Andres Almiray</a>
 */
public class ObservableMap implements Map {
    private Map delegate;
    private PropertyChangeSupport pcs;
    private Closure test;

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

    // Map interface

    public void clear() {
        Map values = new HashMap();
        if( !delegate.isEmpty() ) {
            values.putAll( delegate );
        }
        delegate.clear();
        if( values != null ) {
            pcs.firePropertyChange( new PropertyClearedEvent(this,values) );
        }
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
        Object oldValue = null;
        boolean newKey = !delegate.containsKey( key );
        if( test != null ) {
            oldValue = delegate.put(key, value);
            Object result = null;
            if( test.getMaximumNumberOfParameters() == 2 ) {
                result = test.call(new Object[] {key, value});
            } else {
                result = test.call(value);
            }
            if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
                if( newKey ) {
                    pcs.firePropertyChange( new PropertyAddedEvent(this, String.valueOf(key), value ) );
                }else if (oldValue != value) {
                    pcs.firePropertyChange( new PropertyUpdatedEvent(this, String.valueOf(key), oldValue, value ) );
                }
            }
        } else {
            oldValue = delegate.put(key, value);
            if( newKey ) {
                pcs.firePropertyChange( new PropertyAddedEvent(this, String.valueOf(key), value ) );
            }else if (oldValue != value) {
                pcs.firePropertyChange( new PropertyUpdatedEvent(this, String.valueOf(key), oldValue, value ) );
            }
        }
        return oldValue;
    }

    public void putAll(Map map) {
        if( map != null ) {
            List events = new ArrayList();
            for (Iterator entries = map.entrySet()
                    .iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();

                String key = String.valueOf(entry.getKey());
                Object newValue = entry.getValue();
                Object oldValue = null;

                boolean newKey = !delegate.containsKey( key );
                if( test != null ) {
                    oldValue = delegate.put(key, newValue);
                    Object result = null;
                    if( test.getMaximumNumberOfParameters() == 2 ) {
                        result = test.call(new Object[] {key, newValue});
                    } else {
                        result = test.call(newValue);
                    }
                    if( result != null && result instanceof Boolean && ((Boolean) result).booleanValue() ) {
                        if( newKey ) {
                            events.add( new PropertyAddedEvent(this, key, newValue ) );
                        }else if (oldValue != newValue) {
                            events.add( new PropertyUpdatedEvent(this, key, oldValue, newValue) );
                        }
                    }
                } else {
                    oldValue = delegate.put(key, newValue);
                    if( newKey ) {
                        events.add( new PropertyAddedEvent(this, key, newValue ) );
                    }else if (oldValue != newValue) {
                        events.add( new PropertyUpdatedEvent(this, key, oldValue, newValue) );
                    }
                }
            }
            if( events.size() > 0 ) {
                pcs.firePropertyChange( new MultiPropertyEvent(this, (PropertyEvent[]) events.toArray(new PropertyEvent[events.size()]) ) );
            }
        }
    }

    public Object remove(Object key) {
        Object result =  delegate.remove(key);
        if( key != null ) {
            pcs.firePropertyChange( new PropertyRemovedEvent(this, String.valueOf(key), result ) );
        }
        return result;
    }

    public int size() {
        return delegate.size();
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

    public abstract static class PropertyEvent extends PropertyChangeEvent {
        public static final int ADDED = 0;
        public static final int UPDATED = 1;
        public static final int REMOVED = 2;
        public static final int CLEARED = 3;
        public static final int MULTI = 4;

        protected static final Object OLDVALUE = new Object();
        protected static final Object NEWVALUE = new Object();

        private int type;

        public PropertyEvent( Object source, String propertyName, Object oldValue, Object newValue, int type ) {
            super( source, propertyName, oldValue, newValue );
            switch( type ){
                case ADDED:
                case UPDATED:
                case REMOVED:
                case CLEARED:
                case MULTI:
                   this.type = type;
                   break;
                default:
                   this.type = UPDATED;
                   break;
            }
        }

        public int getType() {
            return type;
        }
        
        public String getTypeAsString() {
         switch( type ) {
            case ADDED:
               return "ADDED";
            case UPDATED:
               return "UPDATED";
            case REMOVED:
               return "REMOVED";
            case CLEARED:
               return "CLEARED";
            case MULTI:
               return "MULTI";
            default:
               return "UPDATED";
         }
      }
    }

    public static class PropertyAddedEvent extends PropertyEvent {
        public PropertyAddedEvent( Object source, String propertyName, Object newValue ) {
            super( source, propertyName, null, newValue, PropertyEvent.ADDED );
        }
    }

    public static class PropertyUpdatedEvent extends PropertyEvent {
        public PropertyUpdatedEvent( Object source, String propertyName, Object oldValue, Object newValue ) {
            super( source, propertyName, oldValue, newValue, PropertyEvent.UPDATED );
        }
    }

    public static class PropertyRemovedEvent extends PropertyEvent {
        public PropertyRemovedEvent( Object source, String propertyName, Object oldValue ) {
            super( source, propertyName, oldValue, null, PropertyEvent.REMOVED );
        }
    }

    public static class PropertyClearedEvent extends PropertyEvent {
        public static final String CLEAR_PROPERTY = "groovy_util_ObservableMap_PropertyClearedEvent_CLEAR";
        private Map values = new HashMap();

        public PropertyClearedEvent( Object source, Map values ) {
            super( source, CLEAR_PROPERTY, OLDVALUE, NEWVALUE, PropertyEvent.CLEARED );
            if( values != null ) {
                this.values.putAll( values );
            }
        }

        public Map getValues() {
            return Collections.unmodifiableMap( values );
        }
    }

    public static class MultiPropertyEvent extends PropertyEvent {
        public static final String MULTI_PROPERTY = "groovy_util_ObservableMap_MultiPropertyEvent_MULTI";
        private PropertyEvent[] events = new PropertyEvent[0];

        public MultiPropertyEvent( Object source, PropertyEvent[] events ) {
            super( source, MULTI_PROPERTY, OLDVALUE, NEWVALUE, PropertyEvent.MULTI );
            if( events != null && events.length > 0 ) {
                this.events = new PropertyEvent[events.length];
                System.arraycopy(events, 0, this.events, 0, events.length );
            }
        }

        public PropertyEvent[] getEvents() {
            PropertyEvent[] copy = new PropertyEvent[events.length];
            System.arraycopy(events, 0, copy, 0, events.length );
            return copy;
        }
    }
}
