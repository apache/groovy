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

import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.grails.exceptions.NewInstanceCreationException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import groovy.lang.GroovyObject;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public abstract class AbstractGrailsClass implements GrailsClass {

	private Class clazz = null;
	private String fullName = null;
	private String name = null;
	private BeanWrapper reference = null;
	
	/**
	 * <p>Contructor to be used by all child classes to create a
	 * new instance and get the name right.
	 * 
	 * @param clazz the Grails class
	 * @param trailingName the trailing part of the name for this class type
	 */
	public AbstractGrailsClass(Class clazz, String trailingName) {
		super();
		setClazz(clazz);
		
		this.reference = new BeanWrapperImpl(newInstance());
		this.fullName = getShortClassname(clazz);
		if (trailingName == null || trailingName.length() == 0) {
			this.name = fullName;
		} else {
			this.name = this.fullName.substring(0, this.fullName.length() - trailingName.length());
		}
	}

	private void setClazz(Class clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Clazz parameter should not be null");
		}
		this.clazz = clazz;
	}
	
	private Class getClazz() {
		return this.clazz;
	}
	
	public GroovyObject newInstance() {
		try {
			return ((GroovyObject)getClazz().newInstance());
		} catch (Exception e) {
			Throwable targetException = null;
			if (e instanceof InvocationTargetException) {
				targetException = ((InvocationTargetException)e).getTargetException();
			} else {
				targetException = e;
			}
			throw new NewInstanceCreationException("Could not create a new instance of class [" + getClazz().getName() + "]!", targetException);
		}
	}

	public String getName() {
		return this.name;
	}

	public String getFullName() {
		return this.fullName;
	}

	private static String getShortClassname(Class clazz) {
		if (clazz.getName().indexOf(".") > -1) {
			return clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1, clazz.getName().length());
		} else {
			return clazz.getName();
		}
	}
	
	/**
	 * <p>The reference instance is used to get configured property values.
	 * 
	 * @return BeanWrapper instance that holds reference
	 */
	protected BeanWrapper getReference() {
		return this.reference;
	}
	
	/**
	 * <p>Looks for a property of the reference instance with a given name and type. If found
	 * its value is returned, otherwise null.
	 * 
	 * @return property value or null if no property was found
	 */
	protected Object getPropertyValue(String name, Class type) {
		if (getReference().isReadableProperty(name) && getReference().getPropertyType(name).equals(type)) {
			return getReference().getPropertyValue(name);
		} else {
			return null;
		}
	}
}
