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

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * InvokerHelper.getInstance().getMetaRegistry().setMetaClass(Foo.class,myFooMetaClass)
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public abstract class AbstractDynamicMethods {

	private Collection dynamicMethodInvocations = null;
	private Collection staticMethodInvocations = null;
	private Collection dynamicProperties = null;
	private Class clazz = null;
	
	public AbstractDynamicMethods(Class theClass)
			throws IntrospectionException {
		super();
		new DelegatingMetaClass(theClass, this);
		this.clazz = theClass;
		this.dynamicMethodInvocations = new ArrayList();
		this.staticMethodInvocations = new ArrayList();
		this.dynamicProperties = new ArrayList();
	}

	public void addDynamicMethodInvocation(DynamicMethodInvocation methodInvocation) {
		this.dynamicMethodInvocations.add(methodInvocation);
	}
	
	public void addStaticMethodInvocation(StaticMethodInvocation methodInvocation) {
		this.staticMethodInvocations.add(methodInvocation);
	}
	
	public void addDynamicProperty(DynamicProperty property) {
		this.dynamicProperties.add(property);
	}
	
	public Object getProperty(Object object, String propertyName, InvocationCallback callback) {
		for (Iterator iter = this.dynamicProperties.iterator(); iter.hasNext();) {
			DynamicProperty getter = (DynamicProperty)iter.next();
			if (getter.isPropertyMatch(propertyName)) {
				callback.markInvoked();
				return getter.get(object);
			}
		}
		return null;		
	}
	public void setProperty(Object object, String propertyName,Object newValue, InvocationCallback callback) {
		for (Iterator iter = this.dynamicProperties.iterator(); iter.hasNext();) {
			DynamicProperty property = (DynamicProperty)iter.next();
			if (property.isPropertyMatch(propertyName)) {
				callback.markInvoked();
				property.set(object,newValue);
			}
		}		
	}	
	public Object invokeMethod(Object object, String methodName,
		Object[] arguments, InvocationCallback callback) {
		for (Iterator iter = this.dynamicMethodInvocations.iterator(); iter.hasNext();) {
			DynamicMethodInvocation methodInvocation = (DynamicMethodInvocation)iter.next();
			if (methodInvocation.isMethodMatch(methodName)) {
				callback.markInvoked();
				return methodInvocation.invoke(object, arguments);
			}
		}
		return null;
	}
	
	public Object invokeStaticMethod(Object object, String methodName,
			Object[] arguments, InvocationCallback callBack) {
		for (Iterator iter = this.staticMethodInvocations.iterator(); iter.hasNext();) {
			StaticMethodInvocation methodInvocation = (StaticMethodInvocation)iter.next();
			if (methodInvocation.isMethodMatch(methodName)) {
				callBack.markInvoked();
				return methodInvocation.invoke(this.clazz, methodName, arguments);
			}
		}
		return null;
	}
}
