/*
 * Copyright 2004-2005 the original author or authors.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * <p>Wrapper for HttpServletRequest instance that also implements
 * java.util.Map. Read-only map methods are delegated to the getParameterMap method.
 *
 * @author Steven Devijver
 * @since Jul 2, 2005
 * @see javax.servlet.http.HttpServletRequest
 * @see java.util.Map
 */
public class GrailsHttpServletRequest extends HttpServletRequestWrapper implements Map {

    public GrailsHttpServletRequest(HttpServletRequest delegate) {
        super(delegate);
    }

    public int size() {
        return getRequest().getParameterMap().size();
    }

    public boolean isEmpty() {
        return getRequest().getParameterMap().isEmpty();
    }

    public boolean containsKey(Object key) {
        return getRequest().getParameterMap().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return getRequest().getParameterMap().containsValue(value);
    }

    public Object get(Object key) {
        return getRequest().getParameterMap().get(key);
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map arg0) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set keySet() {
        return getRequest().getParameterMap().keySet();
    }

    public Collection values() {
        return getRequest().getParameterMap().values();
    }

    public Set entrySet() {
        return getRequest().getParameterMap().entrySet();
    }

}
