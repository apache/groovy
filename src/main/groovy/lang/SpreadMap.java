/*
 * Copyright 2003-2011 the original author or authors.
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
package groovy.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a spreadable map which extends java.util.HashMap.
 * 
 * @author Pilho Kim
 */
public class SpreadMap extends HashMap {

    private Map mapData;
    private int hashCode;

    public SpreadMap(Object[] values) {
        mapData = new HashMap(values.length / 2);
        int i = 0;
        while (i < values.length) {
           mapData.put(values[i++], values[i++]);
        }
    }

    public SpreadMap(Map map) {
        this.mapData = map;
    }

    /**
     * @since 1.8.0
     * @param list the list to make spreadable
     */
    public SpreadMap(List list) {
        this(list.toArray());
    }

    public Object get(Object obj) {
        return mapData.get(obj);
    }

    public Object put(Object key, Object value) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so ("
                                   + key + ": " + value + ") cannot be added.");
    }

    public Object remove(Object key) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the key ("
                                   + key + ") cannot be deleted.");
    }

    public void putAll(Map t) {
        throw new RuntimeException("SpreadMap: " + this + " is an immutable map, and so the map ("
                                   + t + ") cannot be put in this spreadMap.");
    }

    public int size() {
        return mapData.keySet().size();
    }

    public boolean equals(Object that) {
        if (that instanceof SpreadMap) {
            return equals((SpreadMap) that);
        }
        return false;
    }

    public boolean equals(SpreadMap that) {
        if (that == null) return false;        

        if (size() == that.size()) {
            Iterator iter = mapData.keySet().iterator();
            for (; iter.hasNext(); ) {
                Object key = iter.next();
                if (! DefaultTypeTransformation.compareEqual(get(key), that.get(key)) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public int hashCode() {
        if (hashCode == 0) {
            Iterator iter = mapData.keySet().iterator();
            for (; iter.hasNext(); ) {
                Object key = iter.next();
                int hash = (key != null) ? key.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
        }
        return hashCode;
    }

    /**
     * @return the string expression of <code>this</code>
     */
    public String toString() {
        if (mapData.isEmpty()) {
            return "*:[:]";
        }
        StringBuffer buff = new StringBuffer("*:[");
        Iterator iter = mapData.keySet().iterator();
        for (; iter.hasNext(); ) {
            Object key = iter.next();
            buff.append(key + ":" + mapData.get(key));
            if (iter.hasNext())
                buff.append(", ");
        }
        buff.append("]");
        return buff.toString();
    }
}
