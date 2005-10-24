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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * A dynamic property that adds a "params" map to a controller for accessing request parameters
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class GetParamsDynamicProperty extends AbstractDynamicControllerProperty {
	private static final String PROPERTY_NAME = "params";
	
	public GetParamsDynamicProperty(HttpServletRequest request, HttpServletResponse response) {
		super(PROPERTY_NAME,request, response);
	}

	public Object get(Object object) {
		return new HttpRequestParameterMap();
	}

	public void set(Object object, Object newValue) {
		throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" ); 
	}	
	
	/**
	 * Wraps the HttpRequest in a map for accessing request parameters
	 * 
	 * @author Graeme Rocher
	 * @since Oct 24, 2005
	 */
	private class HttpRequestParameterMap implements Map {
		Map parameterMap = request.getParameterMap();
		
		public int size() {
			return parameterMap.size();
		}

		public boolean isEmpty() {			
			return parameterMap.isEmpty();
		}

		public boolean containsKey(Object key) {
			return parameterMap.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return parameterMap.containsValue(value);
		}

		public Object get(Object key) {
			if(!(key instanceof String))
					throw new IllegalArgumentException("Request parameter key '"+key+"' must be a string value");
			
			String[] valueArray = (String[])parameterMap.get(key);
			if(valueArray == null)
				return null;
			
			if(valueArray.length == 1)
				return request.getParameter((String)key); 

			return parameterMap.get(key);
		}

		public Object put(Object arg0, Object arg1) {
			throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" );
		}

		public Object remove(Object arg0) {
			throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" );
		}

		public void putAll(Map arg0) {
			throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" );
		}

		public void clear() {
			throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' is read-only!" );
		}

		public Set keySet() {
			return parameterMap.keySet();
		}

		public Collection values() {
			return parameterMap.values();
		}

		public Set entrySet() {
			return parameterMap.entrySet();
		}		
	}
}
