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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The "findWhere" persistent static method. This method takes a map with the
 * keys being the property names and the values being the values of those properties
 * and locates an instance that matches the criteria.
 * 
 * eg. Account.findWhere( [ "holder":"Joe Bloggs", branch:"London" ] );
 * 
 * @author Graeme Rocher
 *
 */
public class FindWherePersistentMethod extends AbstractStaticPersistentMethod {

	
	private static final String METHOD_PATTERN = "^findWhere$";

	public FindWherePersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractStaticPersistentMethod#doInvokeInternal(java.lang.Class, java.lang.String, java.lang.Object[])
	 */
	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		
		// if no arguments passed throw exception
		if(arguments.length == 0)
			throw new MissingMethodException(METHOD_PATTERN, clazz,arguments);
		// if its not a map throw exception
		Object arg = arguments[0];
		if(!(arg instanceof Map))
			throw new MissingMethodException(METHOD_PATTERN, clazz,arguments);
		
		final Map queryMap = (Map)arg;
		
		// TODO: We may want to validate the query map to make sure each named property
		// is a valid property of the GrailsDomainClass thus hiding the Hibernate
		// exception that will be thrown if it is not
		
		return super.getHibernateTemplate().execute( new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				Criteria crit = session.createCriteria(clazz);
				
				crit.add( Expression.allEq( queryMap ) );
				
				List results = crit.list();
				if(results.size() > 0)
					return results.get(0);
				return null;					
				
			}
			
		});
				
	}

}
