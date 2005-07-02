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
package org.codehaus.groovy.grails.commons;

import groovy.lang.Closure;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class DefaultGrailsControllerClass extends AbstractInjectableGrailsClass
		implements GrailsControllerClass {

	private static final String SLASH = "/";
	private static final String VIEW = "View";
	private static final String DEFAULT_CLOSURE_PROPERTY = "defaultClosure";
	public static final String CONTROLLER = "Controller";
	
	
	private Map viewNames = null;
	private Map uri2closureMap = null;
	private String[] uris = null;
	
	public DefaultGrailsControllerClass(Class clazz) {
		super(clazz, CONTROLLER);
		String uri = SLASH + getName().substring(0, 1).toLowerCase() + getName().substring(1, getName().length());
		String defaultClosureName = (String)getPropertyValue(DEFAULT_CLOSURE_PROPERTY, String.class);
		
		this.viewNames = new HashMap();
		this.uri2closureMap = new HashMap();
		PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			if (propertyDescriptor.getPropertyType().equals(Closure.class)) {
				this.viewNames.put(propertyDescriptor.getName(), getPropertyValue(propertyDescriptor.getName() + VIEW, String.class));
				Closure closure = (Closure)getPropertyValue(propertyDescriptor.getName(), Closure.class);
				if (closure != null) {
					this.uri2closureMap.put(uri + SLASH + propertyDescriptor.getName(), propertyDescriptor.getName());
					if (defaultClosureName != null && defaultClosureName.equals(propertyDescriptor.getName())) {
						this.uri2closureMap.put(uri, propertyDescriptor.getName());
					}
				}
			}
		}
		if (this.uri2closureMap.size() == 1) {
			this.uri2closureMap.put(uri, this.uri2closureMap.values().iterator().next());
		}
		this.uris = (String[])this.uri2closureMap.keySet().toArray(new String[this.uri2closureMap.size()]); 
	}

	public String[] getURIs() {
		return this.uris;
	}
	
	public String getViewName(String closureName) {
		return (String)this.viewNames.get(closureName);
	}
	
	public String getClosurePropertyName(String uri) {
		return (String)this.uri2closureMap.get(uri);
	}
}
