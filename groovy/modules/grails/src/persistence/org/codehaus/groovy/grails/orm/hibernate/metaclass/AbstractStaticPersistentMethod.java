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

import java.util.regex.Pattern;

import org.codehaus.groovy.grails.commons.metaclass.AbstractStaticMethodInvocation;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 8, 2005
 */
public abstract class AbstractStaticPersistentMethod extends
		AbstractStaticMethodInvocation {

	private SessionFactory sessionFactory = null;
	private ClassLoader classLoader = null;
	
	public AbstractStaticPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, Pattern pattern) {
		super();
		setPattern(pattern);
		this.sessionFactory = sessionFactory;
		this.classLoader = classLoader;
	}

	protected HibernateTemplate getHibernateTemplate() {
		Assert.notNull(sessionFactory, "Session factory is required!");
		return new HibernateTemplate(this.sessionFactory);
	}
	
	public Object invoke(Class clazz, String methodName, Object[] arguments) {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.classLoader);
		Object returnValue = doInvokeInternal(clazz, methodName, arguments);
		Thread.currentThread().setContextClassLoader(originalClassLoader);
		return returnValue;
	}

	protected abstract Object doInvokeInternal(Class clazz, String methodName, Object[] arguments);
}
