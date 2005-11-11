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
 * An interface that defines methods for a handling dynamic method, static method and property 
 * invocations
 * 
 * @author Graeme Rocher
 * @since Oct 27, 2005
 */
public interface DynamicMethods {

	/**
	 * Adds a new dynamic method invocation
	 * @param methodInvocation
	 */
	public abstract void addDynamicMethodInvocation(
			DynamicMethodInvocation methodInvocation);

	/**
	 * Adds a new static method invocation
	 * @param methodInvocation
	 */
	public abstract void addStaticMethodInvocation(
			StaticMethodInvocation methodInvocation);

	/**
	 * Adds a new dynamic property
	 * @param property
	 */
	public abstract void addDynamicProperty(DynamicProperty property);

	/**
	 * Retrieves a dynamic property for the specified property name
	 * @param propertyName The name of the property
	 * @return A DynamicProperty instance of null if none exists
	 */
	public abstract DynamicProperty getDynamicProperty(String propertyName);
	
	public abstract Object getProperty(Object object, String propertyName,
			InvocationCallback callback);

	public abstract void setProperty(Object object, String propertyName,
			Object newValue, InvocationCallback callback);

	public abstract Object invokeMethod(Object object, String methodName,
			Object[] arguments, InvocationCallback callback);

	public abstract Object invokeStaticMethod(Object object, String methodName,
			Object[] arguments, InvocationCallback callBack);

}