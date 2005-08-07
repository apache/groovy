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

import groovy.lang.MetaClass;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * InvokerHelper.getInstance().getMetaRegistry().setMetaClass(Foo.class,myFooMetaClass)
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public abstract class AbstractPersistentMethods extends MetaClass {

	private Collection dynamicMethodInvocations = null;
	private Collection staticMethodInvocations = null;
	
	public AbstractPersistentMethods(Class theClass)
			throws IntrospectionException {
		super(InvokerHelper.getInstance().getMetaRegistry(), theClass);
		this.dynamicMethodInvocations = new ArrayList();
		this.staticMethodInvocations = new ArrayList();
		registry.setMetaClass(theClass, this);
		registerMethodInvocations();
	}

	protected abstract void registerMethodInvocations();
	
	public void addDynamicMethodInvocation(DynamicMethodInvocation methodInvocation) {
		this.dynamicMethodInvocations.add(methodInvocation);
	}
	
	public void setStaticMethodInvocation(StaticMethodInvocation methodInvocation) {
		this.staticMethodInvocations.add(methodInvocation);
	}
	
	public Object invokeMethod(Object object, String methodName,
			Object[] arguments) {
		if (arguments != null && arguments.length == 0) {
			for (Iterator iter = this.dynamicMethodInvocations.iterator(); iter.hasNext();) {
				DynamicMethodInvocation methodInvocation = (DynamicMethodInvocation)iter.next();
				if (methodInvocation.getMethodName().equals(methodName)) {
					return methodInvocation.invoke(object);
				}
			}
		}
		return super.invokeMethod(object, methodName, arguments);
	}
	
	public Object invokeStaticMethod(Object object, String methodName,
			Object[] arguments) {
		for (Iterator iter = this.staticMethodInvocations.iterator(); iter.hasNext();) {
			StaticMethodInvocation methodInvocation = (StaticMethodInvocation)iter.next();
			if (methodInvocation.isMethodMatch(methodName)) {
				return methodInvocation.invoke(methodName, arguments);
			}
		}
		return super.invokeStaticMethod(object, methodName, arguments);
	}
}
