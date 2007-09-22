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
 * def map = new ObservableMap( { it =~ /[A-Z+]/ } )
 * </pre>
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
        delegate.clear();
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
        if (test != null) {
            Object result = null;
            if (test.getMaximumNumberOfParameters() == 2) {
                result = test.call(new Object[] {key, value});
            } else {
                result = test.call(value);
            }
            if (result != null && result instanceof Boolean && ((Boolean) result).booleanValue()) {
                oldValue = delegate.put(key, value);
                if (oldValue != value) {
                    pcs.firePropertyChange(String.valueOf(key), oldValue, value);
                }
            }
        } else {
            oldValue = delegate.put(key, value);
            if (oldValue != value) {
                pcs.firePropertyChange(String.valueOf(key), oldValue, value);
            }
        }
        return oldValue;
    }

    public void putAll(Map map) {
        if (map != null) {
            for (Iterator entries = map.entrySet()
                    .iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();

                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Object remove(Object key) {
        return delegate.remove(key);
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
}