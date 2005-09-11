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

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.springframework.orm.hibernate3.HibernateCallback;
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
	private static final String METHOD_SIGNATURE = "get";

	public GetPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		// if no arguments passed throw exception
		if(arguments.length == 0)
			throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);
		// if its not a map throw exception
		final Object arg = arguments[0];
		// if its a long retrieve by id
		if(arg instanceof Long) {
			return super.getHibernateTemplate().get( clazz, (Long)arg );
		}
		// if its an instance of this class, retrieve by example
		else if(clazz.isInstance( arg.getClass() )) {
			
			return super.getHibernateTemplate().execute( new HibernateCallback() {

				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					
					Example example = Example.create(arg)
							.ignoreCase();
					
					Criteria crit = session.createCriteria(clazz);
					return crit
						.add(example)
						.uniqueResult();
				}
				
			});
			
		}
		// if its a string then its a query
		else if(arg instanceof String){
			final String queryString = (String)arg;
			return super.getHibernateTemplate().execute( new HibernateCallback() {

				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query query = session.createQuery(queryString);
					return query.uniqueResult();					
				}			
			});
			
		}
		
		throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);
	}

}
