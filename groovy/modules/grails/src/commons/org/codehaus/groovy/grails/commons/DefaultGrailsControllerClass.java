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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class DefaultGrailsControllerClass extends AbstractInjectableGrailsClass
		implements GrailsControllerClass {

    public static final String CONTROLLER = "Controller";

    private static final String SLASH = "/";
    private static final String VIEW = "View";
    private static final String TYPED_VIEWS = "TypedViews";
    private static final String DEFAULT_CLOSURE_PROPERTY = "defaultAction";

	private Map viewNames = null;
	private Map uri2closureMap = null;
	private String[] uris = null;
	
	public DefaultGrailsControllerClass(Class clazz) {
		super(clazz, CONTROLLER);
		String uri = SLASH + (StringUtils.isNotBlank(getPackageName()) ? getPackageName().replace('.', '/')  + SLASH : "" ) + WordUtils.uncapitalize(getName());
		String defaultClosureName = (String)getPropertyValue(DEFAULT_CLOSURE_PROPERTY, String.class);
		Collection closureNames = new ArrayList();
		
		this.viewNames = new HashMap();
		this.uri2closureMap = new HashMap();
		PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			if (propertyDescriptor.getPropertyType().equals(Closure.class)) {
				closureNames.add(propertyDescriptor.getName());
				String viewName = (String)getPropertyValue(propertyDescriptor.getName() + VIEW, String.class);
				// if no explicity view name is specified just use property name
				if(viewName == null) {
					viewName = propertyDescriptor.getName();
				}
				Map typedViews = (Map)getPropertyValue(propertyDescriptor.getName() + TYPED_VIEWS, Map.class);
				String tmpUri = uri + SLASH + propertyDescriptor.getName();
				
				if (StringUtils.isNotBlank(viewName)) {
					this.viewNames.put(tmpUri, uri + SLASH + viewName);
				}
				Closure closure = (Closure)getPropertyValue(propertyDescriptor.getName(), Closure.class);
				if (closure != null) {
					this.uri2closureMap.put(tmpUri, propertyDescriptor.getName());
					if (typedViews != null) {
						for (Iterator iter = typedViews.keySet().iterator(); iter.hasNext();) {
							String viewType = (String)iter.next();
							String typedViewName = (String)typedViews.get(viewType);
							String typedUri = tmpUri + SLASH + viewType;
							this.viewNames.put(typedUri, typedViewName);
							this.uri2closureMap.put(typedUri, propertyDescriptor.getName());
							if (defaultClosureName != null && defaultClosureName.equals(propertyDescriptor.getName())) {
								this.uri2closureMap.put(uri + SLASH + viewType, propertyDescriptor.getName());
								this.viewNames.put(uri + SLASH + viewType, typedViewName);
							}
						}
					}
					if (defaultClosureName != null && defaultClosureName.equals(propertyDescriptor.getName())) {
						this.uri2closureMap.put(uri, propertyDescriptor.getName());
						this.viewNames.put(uri, viewName);
					}
				}
			}
		}
		if (closureNames.size() == 1) {
			String closureName = ((String)closureNames.iterator().next());
			this.uri2closureMap.put(uri, this.uri2closureMap.values().iterator().next());
			if (!this.viewNames.isEmpty()) {
				this.viewNames.put(uri, this.viewNames.values().iterator().next());
			}
			Map typedViews = (Map)getPropertyValue(closureName + TYPED_VIEWS, Map.class);
			if (typedViews != null) {
				for (Iterator iter = typedViews.keySet().iterator(); iter.hasNext();) {
					String viewType = (String)iter.next();
					String typedViewName = (String)typedViews.get(viewType);
					this.uri2closureMap.put(uri + SLASH + viewType, this.uri2closureMap.values().iterator().next());
					this.viewNames.put(uri + SLASH + viewType, typedViewName);
				}
			}
		}
		this.uris = (String[])this.uri2closureMap.keySet().toArray(new String[this.uri2closureMap.size()]); 
	}

	public String[] getURIs() {
		return this.uris;
	}

	public boolean mapsToURI(String uri) {
		for (int i = 0; i < uris.length; i++) {
			if (uris[i].equals(uri)) {
				return true;
			}
		}
		return false;
	}
	
	public String getViewName(String closureName) {
		return (String)this.viewNames.get(closureName);
	}
	
	public String getClosurePropertyName(String uri) {
		return (String)this.uri2closureMap.get(uri);
	}
}
