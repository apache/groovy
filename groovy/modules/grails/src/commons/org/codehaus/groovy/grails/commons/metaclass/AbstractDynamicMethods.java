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

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * InvokerHelper.getInstance().getMetaRegistry().setMetaClass(Foo.class,myFooMetaClass)
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public abstract class AbstractDynamicMethods implements DynamicMethods {

	private Collection dynamicMethodInvocations = null;
	private Collection staticMethodInvocations = null;
	private Map dynamicProperties = null;
	private Class clazz = null;
	
	private static final Log LOG = LogFactory.getLog(AbstractDynamicMethods.class);
	
	public AbstractDynamicMethods(Class theClass)
			throws IntrospectionException {
		super();
		new DelegatingMetaClass(theClass, this);
		this.clazz = theClass;
		this.dynamicMethodInvocations = new ArrayList();
		this.staticMethodInvocations = new ArrayList();
		this.dynamicProperties = new HashMap();
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#addDynamicMethodInvocation(org.codehaus.groovy.grails.metaclass.DynamicMethodInvocation)
	 */
	public void addDynamicMethodInvocation(DynamicMethodInvocation methodInvocation) {		
		this.dynamicMethodInvocations.add(methodInvocation);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#addStaticMethodInvocation(org.codehaus.groovy.grails.metaclass.StaticMethodInvocation)
	 */
	public void addStaticMethodInvocation(StaticMethodInvocation methodInvocation) {
		this.staticMethodInvocations.add(methodInvocation);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#addDynamicProperty(org.codehaus.groovy.grails.metaclass.DynamicProperty)
	 */
	public void addDynamicProperty(DynamicProperty property) {
		this.dynamicProperties.put(property.getPropertyName(), property);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#getProperty(java.lang.Object, java.lang.String, org.codehaus.groovy.grails.metaclass.InvocationCallback)
	 */
	public Object getProperty(Object object, String propertyName, InvocationCallback callback) {		
		DynamicProperty getter = (DynamicProperty)this.dynamicProperties.get(propertyName);
		if (getter != null && getter.isPropertyMatch(propertyName)) {
			callback.markInvoked();
			return getter.get(object);
		}
		return null;		
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#setProperty(java.lang.Object, java.lang.String, java.lang.Object, org.codehaus.groovy.grails.metaclass.InvocationCallback)
	 */
	public void setProperty(Object object, String propertyName,Object newValue, InvocationCallback callback) {
		DynamicProperty setter = (DynamicProperty)this.dynamicProperties.get(propertyName);
		if (setter != null && setter.isPropertyMatch(propertyName)) {
			callback.markInvoked();
			setter.set(object,newValue);
		}		
	}	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object[], org.codehaus.groovy.grails.metaclass.InvocationCallback)
	 */
	public Object invokeMethod(Object object, String methodName,
		Object[] arguments, InvocationCallback callback) {
		if(LOG.isTraceEnabled()) {
			LOG.trace("[DynamicMethods] Attempting invocation of dynamic method ["+methodName+"] on target ["+object+"] with arguments ["+Arrays.toString( arguments )+"]");			
		}		
		for (Iterator iter = this.dynamicMethodInvocations.iterator(); iter.hasNext();) {
			DynamicMethodInvocation methodInvocation = (DynamicMethodInvocation)iter.next();
			if (methodInvocation.isMethodMatch(methodName)) {
				if(LOG.isTraceEnabled()) {
					LOG.trace("[DynamicMethods] Dynamic method matched, marking and invoking");			
				}				
				callback.markInvoked();
				return methodInvocation.invoke(object, arguments);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.metaclass.DynamicMethods#invokeStaticMethod(java.lang.Object, java.lang.String, java.lang.Object[], org.codehaus.groovy.grails.metaclass.InvocationCallback)
	 */
	public Object invokeStaticMethod(Object object, String methodName,
			Object[] arguments, InvocationCallback callBack) {
		if(LOG.isTraceEnabled()) {
			LOG.trace("[DynamicMethods] Attempting invocation of dynamic static method ["+methodName+"] on target ["+object+"] with arguments ["+Arrays.toString( arguments )+"]");
			LOG.trace("[DynamicMethods] Registered dynamic static methods: ["+this.staticMethodInvocations+"]");
		}
		for (Iterator iter = this.staticMethodInvocations.iterator(); iter.hasNext();) {
			StaticMethodInvocation methodInvocation = (StaticMethodInvocation)iter.next();
			if (methodInvocation.isMethodMatch(methodName)) {
				if(LOG.isTraceEnabled()) {
					LOG.trace("[DynamicMethods] Static method matched, marking and invoking");			
				}				
				callBack.markInvoked();
				return methodInvocation.invoke(this.clazz, methodName, arguments);
			}
		}
		return null;
	}

	public DynamicProperty getDynamicProperty(String propertyName) {
		return (DynamicProperty)this.dynamicProperties.get(propertyName);
	}
}
