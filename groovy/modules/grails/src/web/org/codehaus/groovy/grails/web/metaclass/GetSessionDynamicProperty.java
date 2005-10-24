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
package org.codehaus.groovy.grails.web.metaclass;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
/**
 * A dynamic property that adds a "session" map to a controller for accessing the Http Session
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class GetSessionDynamicProperty extends AbstractDynamicControllerProperty {

	private static final String PROPERTY_NAME = "session";

	public GetSessionDynamicProperty(HttpServletRequest request, HttpServletResponse response) {
		super(PROPERTY_NAME, request, response);
	}

	public Object get(Object object) {
		return new HttpSessionMap();
	}

	public void set(Object object, Object newValue) {
		throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" );
	}
	
	/**
	 * Wraps the HttpSessoin in a map
	 * 
	 * @author Graeme Rocher
	 * @since Oct 24, 2005
	 */
	private class HttpSessionMap implements Map {
				
		HttpSession session = null;
		
		public int size() {
			if(session == null || session.isNew())
				return 0;			
			// count as there is no way to access size from the session directly
			int count = 0;
			for(Enumeration e = session.getAttributeNames();e.hasMoreElements();count++) {}
			return count;
		}

		public boolean isEmpty() {
			if(session == null || session.isNew())
				return true;
			else
				return false;			
		}

		public boolean containsKey(Object key) {
			if(!(key instanceof String))
				throw new IllegalArgumentException("The 'session' property key '"+key+"' must be a string value");
			
			if(session == null || session.isNew())
				return false;
			
			Object attr = session.getAttribute((String)key);
			if(attr == null)
				return false;
			else
				return true;
		}

		public boolean containsValue(Object value) {
			if(session == null || session.isNew())
				return false;
			
			for(Enumeration e = session.getAttributeNames();e.hasMoreElements();) {
				String currentKey = (String)e.nextElement();
				Object current = session.getAttribute(currentKey);
				if(current.equals(value))
					return true;
			}			
			return false;
		}

		public Object get(Object key) {
			if(!(key instanceof String))
				throw new IllegalArgumentException("The 'session' property key '"+key+"' must be a string value");
			
			if(session == null || session.isNew())
				return null;
			
			return session.getAttribute((String)key);
		}

		public Object put(Object key, Object value) {
			if(!(key instanceof String))
				throw new IllegalArgumentException("The 'session' property key '"+key+"' must be a string value");
			
			if(session == null || session.isNew())
				session = request.getSession();
			
			session.setAttribute((String)key, value);
			return value;
		}

		public Object remove(Object key) {
			if(!(key instanceof String))
				throw new IllegalArgumentException("The 'session' property key '"+key+"' must be a string value");

			if(session == null || session.isNew())
				return null;
			
			Object value = session.getAttribute((String)key);
			if(value != null)
				session.removeAttribute((String)key);
			
			return value;
		}

		public void putAll(Map sessionMap) {
			for (Iterator i = sessionMap.keySet().iterator(); i.hasNext();) {
				String currentKey = (String) i.next();
				put(currentKey,sessionMap.get(currentKey));			
			}
		}

		public void clear() {
			if(session == null || session.isNew())
				return;
			
			for(Enumeration e = session.getAttributeNames();e.hasMoreElements();) {
				String current = (String)e.nextElement();
				session.removeAttribute(current);
			}
		}

		public Set keySet() {
			if(session == null || session.isNew())
				return Collections.EMPTY_SET;
			
			Set keySet = new HashSet();
			for(Enumeration e = session.getAttributeNames();e.hasMoreElements();) {
				String current = (String)e.nextElement();
				keySet.add(current);
			}			
			return keySet;
		}

		public Collection values() {
			throw new UnsupportedOperationException("Method 'values()' is not support by session Map." );
		}

		public Set entrySet() {
			throw new UnsupportedOperationException("Method 'entrySet()' is not support by session Map." );
		}		
	}
}
