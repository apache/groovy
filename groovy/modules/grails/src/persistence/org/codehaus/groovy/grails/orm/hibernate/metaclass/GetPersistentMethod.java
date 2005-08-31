/* Copyright 2004-2005 the original author or authors.
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

import groovy.lang.MissingMethodException;

import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
/**
 * The "get" static persistent method for Grails domain classes. This method
 * takes an id and returns the instance 
 * 
 * eg. Account.get(2)
 * 
 * @author Graeme Rocher
 *
 */
public class GetPersistentMethod extends AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "^get$";

	public GetPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	protected Object doInvokeInternal(Class clazz, String methodName,
			Object[] arguments) {
		// if no arguments passed throw exception
		if(arguments.length == 0)
			throw new MissingMethodException(METHOD_PATTERN, clazz,arguments);
		// if its not a map throw exception
		Object arg = arguments[0];
		if(!(arg instanceof Integer))
			throw new MissingMethodException(METHOD_PATTERN, clazz,arguments);
		
		return super.getHibernateTemplate().get( clazz, (Integer)arg );
	}

}
