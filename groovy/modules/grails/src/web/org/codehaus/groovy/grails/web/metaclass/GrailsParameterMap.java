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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
/**
 * A parameter map class that allows mixing of request parameters and controller parameters. If a controller
 * parameter is set with the same name as a request parameter the controller parameter value is retrieved.
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class GrailsParameterMap implements Map {

	private Map parameterMap;
	private Map controllerParamsMap = new HashMap();
	private HttpServletRequest request;
	
	
	public GrailsParameterMap(HttpServletRequest request) {
		super();
		this.request = request;
		this.parameterMap = request.getParameterMap();
	}

	public int size() {
		return parameterMap.size() + controllerParamsMap.size();
	}

	public boolean isEmpty() {			
		return (parameterMap.isEmpty() && controllerParamsMap.isEmpty());
	}

	public boolean containsKey(Object key) {
		return (parameterMap.containsKey(key) || controllerParamsMap.containsKey(key));
	}

	public boolean containsValue(Object value) {
		return (parameterMap.containsValue(value) || controllerParamsMap.containsValue(value));
	}

	public Object get(Object key) {
		if(!(key instanceof String))
				throw new IllegalArgumentException("Parameter key '"+key+"' must be a string value");
		
		if(controllerParamsMap.containsKey(key)) {
			return controllerParamsMap.get(key);
		}
		else {
			String[] valueArray = (String[])parameterMap.get(key);
			if(valueArray == null)
				return null;
			
			if(valueArray.length == 1)
				return request.getParameter((String)key); 
	
			return parameterMap.get(key);
		}
	}

	public Object put(Object key, Object value) {
		if(!(key instanceof String))
			throw new IllegalArgumentException("Parameter key '"+key+"' must be a string value");
		
		controllerParamsMap.put(key,value);
		return value;
	}

	public Object remove(Object key) {
		if(!(key instanceof String))
			throw new IllegalArgumentException("Parameter key '"+key+"' must be a string value");

		return controllerParamsMap.remove(key);
	}

	public void putAll(Map map) {
		controllerParamsMap.putAll(map);
	}

	public void clear() {
		controllerParamsMap.clear();
	}

	public Set keySet() {
		Set keys = new HashSet(controllerParamsMap.keySet());
		keys.addAll( parameterMap.keySet() );
		return keys;
	}

	public Collection values() {
		Set values = new HashSet(parameterMap.values());
		values.addAll( controllerParamsMap.values() );
		return values;
	}

	public Set entrySet() {
		Set entries = new HashSet(parameterMap.entrySet());
		entries.addAll( controllerParamsMap.entrySet() );
		return entries;
	}		

}
