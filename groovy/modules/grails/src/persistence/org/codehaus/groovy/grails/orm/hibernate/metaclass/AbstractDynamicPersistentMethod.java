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

import org.codehaus.groovy.grails.metaclass.AbstractDynamicMethodInvocation;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public abstract class AbstractDynamicPersistentMethod extends
		AbstractDynamicMethodInvocation {

	private SessionFactory sessionFactory = null;
	
	public AbstractDynamicPersistentMethod(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	protected HibernateTemplate getHibernateTemplate() {
		if (this.sessionFactory == null) {
			throw new IllegalStateException("Session factory is required!");
		}
		return new HibernateTemplate(this.sessionFactory);
	}
	
	public abstract Object invoke(Object target);

}
