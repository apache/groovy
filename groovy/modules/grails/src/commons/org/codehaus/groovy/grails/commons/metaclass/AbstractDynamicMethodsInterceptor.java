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

import groovy.lang.Interceptor;
/**
 * Implements an the Interceptor interface to add support for using ProxyMetaClass to define 
 * dynamic methods
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public abstract class AbstractDynamicMethodsInterceptor extends AbstractDynamicMethods
		implements Interceptor,PropertyAccessInterceptor {

	protected boolean doInvoke = true;
	protected boolean doGet = true;
	protected boolean doSet = true;
	private Object returnValue;
	

	public Object beforeInvoke(Object target, String methodName,
			Object[] arguments) {
		InvocationCallback callback = new InvocationCallback();
		this.returnValue = super.invokeMethod(target, methodName, arguments, callback);
		// if the method was invoked as dynamic 
		// don't invoke true target
		if (callback.isInvoked()) {
			doInvoke = false;
			return returnValue;
		} else {
			doInvoke = true;
			return null;
		}
	}

	public Object afterInvoke(Object object, String methodName,
			Object[] arguments, Object result) {
		return this.returnValue;
	}

	public boolean doInvoke() {
		return doInvoke;
	}

	public Object beforeGet(Object object, String property) {
		InvocationCallback callback = new InvocationCallback();
		Object returnValue = super.getProperty(object,property,callback);
		// if the method was invoked as dynamic 
		// don't invoke true target
		if (callback.isInvoked()) {
			doGet = false;
			return returnValue;
		} else {
			doGet = true;
			return null;
		}
	}

	public void beforeSet(Object object, String property, Object newValue) {
		InvocationCallback callback = new InvocationCallback();
		super.setProperty(object,property,newValue,callback);
		// if the method was invoked as dynamic 
		// don't invoke true target
		if (callback.isInvoked()) {
			doSet = false;
		} else {
			doSet = true;
		}
	}

	public boolean doGet() {
		return doGet;
	}

	public boolean doSet() {
		return doSet;
	}

}
