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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The "findBy*" static persistent method. This method allows querying for
 * instances of grails domain classes based on their properties.
 * 
 * eg.
 * Account.findByHolder("Joe Blogs"); // Where class "Account" has a property called "holder"
 * Account.findByHolderAndBranch("Joe Blogs", "London" ); // Where class "Account" has a properties called "holder" and "branch"
 * 
 * @author Graeme Rocher
 * @since 31-Aug-2005
 *
 */
public class FindByPersistentMethod extends AbstractClausedStaticPersistentMethod {
	
	private static final String OPERATOR_OR = "Or";
	private static final String OPERATOR_AND = "And";
	
	private static final String METHOD_PATTERN = "(findBy)(\\w+)";
	private static final String[] OPERATORS = new String[]{ OPERATOR_AND, OPERATOR_OR };

	public FindByPersistentMethod(GrailsApplication application,SessionFactory sessionFactory, ClassLoader classLoader) {
 		super(application,sessionFactory, classLoader, Pattern.compile( METHOD_PATTERN ),OPERATORS);
	}

	protected Object doInvokeInternalWithExpressions(final Class clazz, String methodName, Object[] arguments, final List expressions) {
		return super.getHibernateTemplate().executeFind( new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria crit = session.createCriteria(clazz);
				for (Iterator i = expressions.iterator(); i.hasNext();) {
					GrailsMethodExpression current = (GrailsMethodExpression) i.next();
					crit.add( current.getCriterion() );
					
				}
				return crit.list();
			}
		});
	}
	
}
