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

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The "list" persistent static method. This method lists of of the persistent
 * instances up the maximum specified amount (if any)
 * 
 * eg.
 * Account.list(); // list all
 * Account.list(max:10,offset:50,sort:"holder",order:"desc"); // list up to 10, offset by 50, sorted by holder and in descending order 
 * 
 * @author Graeme Rocher
 *
 */
public class ListPersistentMethod extends AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "^list$";

	public ListPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	protected Object doInvokeInternal(final Class clazz, String methodName,
			final Object[] arguments) {
		// if there are no arguments list all
		if(arguments == null || arguments.length == 0) {
			return super.getHibernateTemplate().loadAll(clazz);
		}
		// otherwise retrieve the max argument
		else {
		
			// and list up to the max
			return super.getHibernateTemplate()
				.executeFind( new HibernateCallback() {

					public Object doInHibernate(Session session) throws HibernateException, SQLException {
						Criteria c =  session
							.createCriteria(clazz);
						
						if(arguments.length > 0) {
							if(arguments[0] instanceof Map) {
								Map argMap = (Map)arguments[0];
								populateArgumentsForCriteria(c,argMap);										
							}
						}	
						return c.list();
					}
					
				}
			);
		}		
	}

}
