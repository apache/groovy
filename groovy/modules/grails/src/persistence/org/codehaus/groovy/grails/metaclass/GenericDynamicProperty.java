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
package org.codehaus.groovy.grails.metaclass;

import groovy.lang.MissingPropertyException;

/**
 * A generic dyanmic property for any type
 * 
 * @author Graeme Rocher
 * @since Oct 27, 2005
 */
public class GenericDynamicProperty extends AbstractDynamicProperty {

	private Class type;
	private boolean readyOnly;
	private Object value;

	/**
	 * 
	 * @param propertyName The name of the property
	 * @param type The type of the property
	 * @param value The initial value of the property
	 * @param readOnly True for read-only property
	 */
	public GenericDynamicProperty(String propertyName, Class type,Object value, boolean readOnly) {
		super(propertyName);
		if(type == null)
			throw new IllegalArgumentException("Constructor argument 'type' cannot be null");
		this.readyOnly = readOnly;
		this.type = type;;
		this.value = value;
	}

	public Object get(Object object) {		
		return this.value;
	}

	public void set(Object object, Object newValue) {		
		if(!readyOnly) {
			if(newValue.getClass().isAssignableFrom(this.type))
				this.value = newValue;
			else
				throw new MissingPropertyException("Property '"+this.getPropertyName()+"' for object '"+object.getClass()+"' cannot be set with value '"+newValue+"'. Incorrect type.",object.getClass());	
		}
		else {
			throw new MissingPropertyException("Property '"+this.getPropertyName()+"' for object '"+object.getClass()+"' is read-only!",object.getClass());
		}
	}

	public void setValue(Object value) {
		if(!value.getClass().isAssignableFrom(this.type))
			throw new IllegalArgumentException("Cannot set value ["+this.value+"] to value ["+value+"] for type ["+this.type+"]" );
		
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

}
