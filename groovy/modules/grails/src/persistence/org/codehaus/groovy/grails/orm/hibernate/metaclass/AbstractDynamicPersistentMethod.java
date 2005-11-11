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
package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicMethodInvocation;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public abstract class AbstractDynamicPersistentMethod extends
		AbstractDynamicMethodInvocation {

	private SessionFactory sessionFactory = null;
	private ClassLoader classLoader = null;
	
	public AbstractDynamicPersistentMethod(String methodName, SessionFactory sessionFactory, ClassLoader classLoader) {
		super(methodName);
		this.sessionFactory = sessionFactory;
		this.classLoader = classLoader;
	}

	protected HibernateTemplate getHibernateTemplate() {
		Assert.notNull(sessionFactory, "Session factory is required!");
		return new HibernateTemplate(this.sessionFactory);
	}
	
	public Object invoke(Object target, Object[] arguments) {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.classLoader);
		Object returnValue = doInvokeInternal(target, arguments);
		Thread.currentThread().setContextClassLoader(originalClassLoader);
		return returnValue;
	}
	
	protected abstract Object doInvokeInternal(Object target, Object[] arguments);

}
