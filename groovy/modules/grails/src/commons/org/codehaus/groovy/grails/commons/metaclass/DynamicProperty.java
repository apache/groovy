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
 * <p>A Dynamic class property getter interface
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public interface DynamicProperty {

	/**
	 * Whether the target class contains the specified property
	 * @param propertyName The name of the property
	 * @return True if the class has the property
	 */
	boolean isPropertyMatch(String propertyName);

	/**
	 * Call the getter on the given object
	 * @param object The target object
	 * @return The result of the getter
	 */
	Object get(Object object);
	
	/**
	 * Call the setter on the given object
	 * @param object The target object
	 * @param newValue The new value of the property
	 */
	void set(Object object, Object newValue);

	/**
	 * @return The name of the property
	 */
	String getPropertyName();	

}
