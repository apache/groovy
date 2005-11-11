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
package org.codehaus.groovy.grails.commons.metaclass;
/**
 * <p>An abstract class for implementors of dynamic getters to implement
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public abstract class AbstractDynamicProperty implements DynamicProperty {

	private String propertyName;
	
	
	public AbstractDynamicProperty(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	public boolean isPropertyMatch(String propertyName) {
		return this.propertyName.equals(propertyName);
	}

	
	public String getPropertyName() {
		return propertyName;
	}

	public abstract Object get(Object object);
	public abstract void set(Object object, Object newValue);
}
