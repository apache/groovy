/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This represents a 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class ListHashMap<K,V> implements Map<K,V> {
    private int listFill = 0;
    private Object[] listKeys;
    private Object[] listValues;
    private int size = 0;
    private Map<K,V> innerMap;
    private final int maxListFill;
    
    public ListHashMap() {
        this(3);
    }
    
    public ListHashMap(int listSize){
        this.listKeys = new Object[listSize];
        this.listValues = new Object[listSize];
        maxListFill = listSize;
    }
    
    public void clear() {
        listFill = 0;
        innerMap = null;
        for (int i=0; i<maxListFill; i++) {
            listValues[i] = null;
            listKeys[i] = null;
        }
    }

    public boolean containsKey(Object key) {
        if (size<maxListFill) {
            for (int i=0; i<listFill; i++) {
                if (listKeys[i].equals(key)) return true;
            }
            return false;
        } else {
            return innerMap.containsKey(key);
        }
    }

    public boolean containsValue(Object value) {
        if (size<maxListFill) {
            for (int i=0; i<listFill; i++) {
                if (listValues[i].equals(value)) return true;
            }
            return false;
        } else {
            return innerMap.containsValue(value);
        }
    }

    private Map<K,V> makeMap() {
        Map<K,V> m = new HashMap();
        for (int i=0; i<size; i++) {
            m.put((K) listKeys[i], (V) listValues[i]);
        }
        return m;
    }
    
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Map m;
        if (size>maxListFill) {
            m = innerMap;
        } else {
            m = makeMap();
        }
        return m.entrySet();
    }

    public V get(Object key) {
        if(size==0) return null;
        if (size<maxListFill) {
            for (int i=0; i<size; i++) {
                if (listKeys[i].equals(key)) return (V) listValues[i];
            }
            return null;
        } else {
            return innerMap.get(key);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Set<K> keySet() {
        Map m;
        if (size>=maxListFill) {
            m = innerMap;
        } else {
            m = makeMap();
        }
        return m.keySet();
    }

    public V put(K key, V value) {
        if (size<maxListFill) {
            for (int i=0; i<listFill; i++) {
                if (listKeys[i].equals(key)) {
                    V old = (V) listValues[i];
                    listValues[i] = value;
                    return old;
                } 
            }
            if (size<maxListFill-1) {
                size++;
                listKeys[listFill] = key;
                listValues[listFill] = value;
                listFill++;
                return null;
            } else {
                innerMap = makeMap();
                size++;
            }
        } 
        return innerMap.put(key,value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
       if (size+m.size()<maxListFill) {
           for (Entry<? extends K,? extends V> entry : m.entrySet()) {
               listKeys[listFill] = entry.getKey();
               listValues[listFill] = entry.getValue();
               listFill++;
           }
           size += m.size();
           return;
       } 
       if (size<maxListFill) innerMap = makeMap();
       innerMap.putAll(m);
       size += m.size();
    }

    public V remove(Object key) {
        if (size<maxListFill) {
            for (int i=0; i<listFill; i++) {
                if (listKeys[i].equals(key)) {
                    V old = (V) listValues[i];
                    listFill--; size--;
                    listValues[i] = listValues[listFill];
                    listKeys[i] = listValues[listFill];
                    return old;
                }
            }
            return null;
        } else {
            V old = innerMap.remove(key);
            if (old!=null) size--;
            if (size<maxListFill) {
                mapToList();
            }
            return old;
        }
    }
    
    private void mapToList() {
        int i = 0;
        for (Entry<? extends K,? extends V> entry : innerMap.entrySet()) {
            listKeys[i] = entry.getKey();
            listValues[i] = entry.getValue();
            i++;
        }
        listFill = innerMap.size();
        innerMap = null;
    }

    public int size() {
        return size;
    }

    public Collection<V> values() {
        if (size<maxListFill) {
            ArrayList<V> list = new ArrayList(size);
            for (int i=0; i<listFill; i++) {
                list.add((V) listValues[i]);
            }
            return list;
        } else {
            return innerMap.values();
        }
    }
    
}
