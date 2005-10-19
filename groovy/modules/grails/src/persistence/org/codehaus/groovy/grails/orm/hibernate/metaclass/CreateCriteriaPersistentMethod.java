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

import grails.orm.HibernateCriteriaBuilder;
import groovy.lang.MissingMethodException;

import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
/**
 * Creates a HibernateCriteriaBuilder instance for the current class and returns it.
 * 
 * eg. Account.createCriteria()
 * 
 * @author Graeme Rocher
 */
public class CreateCriteriaPersistentMethod extends
		AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "^createCriteria$";
	private static final String METHOD_SIGNATURE = "get";
	
	public CreateCriteriaPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
		
	}

	protected Object doInvokeInternal(Class clazz, String methodName,
			Object[] arguments) {
		
		if(arguments.length > 0) 
			throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);
				
		return new HibernateCriteriaBuilder(clazz,super.getHibernateTemplate().getSessionFactory());
	}

}
