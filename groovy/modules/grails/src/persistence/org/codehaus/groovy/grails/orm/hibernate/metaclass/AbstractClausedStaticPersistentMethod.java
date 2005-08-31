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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;

/**
 * @author Graeme Rocher
 * @since 31-Aug-2005
 *
 */
public abstract class AbstractClausedStaticPersistentMethod extends
		AbstractStaticPersistentMethod {

	private String clause;
	
	public AbstractClausedStaticPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, Pattern pattern, String clause) {
		super(sessionFactory, classLoader, pattern);
		this.clause = clause;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractStaticPersistentMethod#doInvokeInternal(java.lang.Class, java.lang.String, java.lang.Object[])
	 */
	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		Matcher match = super.getPattern().matcher( methodName );
		String[] clauses;
		
		// get the sequence clauses
		String clauseSequence = match.group(2);
		// if it contains logical ands split on Ands
		if(clauseSequence.matches( "[\\w+]["+this.clause+"][\\w+]" )) {
			// TODO: Bit error prone this, as properties could start 
			// with "and" which would cause a problem. discuss? 
			clauses = clauseSequence.split(this.clause);
			// convert first characters to lower case
			for (int i = 0; i < clauses.length; i++) {
				clauses[i] = clauses[i].substring(0,0).toLowerCase()
				 + clauses[i].substring(1);
			}
		}
		// otherwise there is only one clause
		else {
			clauses = new String[1];
			// convert first char to lower case
			clauses[0] = clauseSequence.substring(0,0).toLowerCase()
						 + clauseSequence.substring(1);
		}
		
		// The number of clauses must equal the number of arguments
		if(clauses.length != arguments.length)
			throw new MissingMethodException(methodName,clazz,arguments);
		
		final Map queryMap = createQueryMap(clauses, arguments);
		
		return doInvokeInternalWithQueryMap(clazz, methodName, arguments, queryMap);
	}
	

	protected abstract Object doInvokeInternalWithQueryMap(Class clazz, String methodName, Object[] arguments, Map queryMap);

	private Map createQueryMap(String[] clauses, Object[] arguments) {
		Map queryMap = new HashMap();
		
		for (int i = 0; i < clauses.length; i++) {
			queryMap.put( clauses[i], arguments[i] );
		}
		return queryMap;
	}
}
