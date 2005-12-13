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
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The "list" persistent static method. This method lists of of the persistent
 * instances up the maximum specified amount (if any)
 * 
 * eg.
 * Account.list(); // list all
 * Account.list(max); // list all up to max
 * 
 * @author Graeme Rocher
 *
 */
public class ListPersistentMethod extends AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "^list$";
	private static final String ARGUMENT_MAX = "max";
	private static final String ARGUMENT_SORT = "sort";
	private static final String ARGUMENT_ORDER = "order";
	private static final String ARGUMENT_OFFSET = "offset";
	private static final String ORDER_DESC = "desc";
	private static final String ORDER_ASC = "asc";

	public ListPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		// if there are no arguments list all
		if(arguments == null || arguments.length == 0) {
			return super.getHibernateTemplate().loadAll(clazz);
		}
		// otherwise retrieve the max argument
		else {
			Object arg = arguments[0];
			if(!(arg instanceof Map))
				throw new MissingMethodException(METHOD_PATTERN, clazz, arguments);
			
			Map argMap = (Map)arg;
			
			Integer maxParam = (Integer)argMap.get(ARGUMENT_MAX);
			Integer offsetParam = (Integer)argMap.get(ARGUMENT_OFFSET);
			String orderParam = (String)argMap.get(ARGUMENT_ORDER);
			
			final String sort = (String)argMap.get(ARGUMENT_SORT);
			final String order = ORDER_DESC.equalsIgnoreCase(orderParam) ? ORDER_DESC : ORDER_ASC;
			final int max = maxParam == null ? -1 : maxParam.intValue();
			final int offset = offsetParam == null ? -1 : offsetParam.intValue();
			
			// and list up to the max
			return super.getHibernateTemplate()
				.executeFind( new HibernateCallback() {

					public Object doInHibernate(Session session) throws HibernateException, SQLException {
						Criteria c =  session
							.createCriteria(clazz);
						
						if(max > -1)
							c.setMaxResults(max);
						if(offset > -1)
							c.setFirstResult(offset);
						if(sort != null) {
							if(ORDER_DESC.equals(order)) {
								c.addOrder( Order.desc(sort) );
							}
							else {
								c.addOrder( Order.asc(sort) );
							}
						}
						return c.list();
					}
					
				}
			);
		}		
	}

}
