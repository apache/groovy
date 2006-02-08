/* Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.servlet;

import java.util.*;

/**
 * Grails implementation of Flash scope (@see org.codehaus.groovy.grails.web.servlet.FlashScope)
 *
 * @author Graeme Rocher
 * @since 07-Feb-2006
 */
public class GrailsFlashScope implements FlashScope {
    private HashMap current = new HashMap();
    private HashMap next = new HashMap();

    public void next() {
        current.clear();
        current = (HashMap)next.clone();
        next.clear();
    }

    public int size() {
        return current.size() + next.size();
    }

    public void clear() {
        current.clear();
        next.clear();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(Object key) {
        return (current.containsKey(key) || next.containsKey(key));
    }

    public boolean containsValue(Object value) {
        return (current.containsValue(value) || next.containsValue(value));
    }

    public Collection values() {
        Collection c = new ArrayList();
        c.addAll(current.values());
        c.addAll(next.values());
        return c;
    }

    public void putAll(Map t) {
        for (Iterator i = t.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            put(key,t.get(key));
        }
    }

    public Set entrySet() {
        Set keySet = new HashSet();
        keySet.addAll(current.entrySet());
        keySet.addAll(next.entrySet());
        return keySet;
    }

    public Set keySet() {
        Set keySet = new HashSet();
        keySet.addAll(current.keySet());
        keySet.addAll(next.keySet());
        return keySet;
    }

    public Object get(Object key) {
        if(next.containsKey(key))
            return next.get(key);
        return current.get(key);
    }

    public Object remove(Object key) {
        if(current.containsKey(key))
            return current.remove(key);
        else
            return next.remove(key);
    }

    public Object put(Object key, Object value) {
        if(current.containsKey(key)) {
            current.remove(key);
        }
        return next.put(key,value);
    }
}
