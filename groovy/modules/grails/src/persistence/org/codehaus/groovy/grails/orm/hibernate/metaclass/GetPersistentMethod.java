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
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * The "get" static persistent method for Grails domain classes. This method
 * takes an id and returns the instance 
 * 
 * eg. Account.get(2)
 * 
 * Or an HQL query and tries to retrieve a unique result (note an exception is thrown if the result is not unique)
 * 
 * eg. Account.get("from Account as a where a.id=2)
 * 
 * @author Graeme Rocher
 *
 */
public class GetPersistentMethod extends AbstractStaticPersistentMethod {

	private static final Pattern METHOD_PATTERN = Pattern.compile("^get$");
	public static final String METHOD_SIGNATURE = "get";
	private GrailsApplication application;
	private Map context = Ognl.createDefaultContext(this);
	private DefaultTypeConverter typeConverter = new DefaultTypeConverter();

	public GetPersistentMethod(GrailsApplication application, SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, METHOD_PATTERN);
		this.application = application;
	}

	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		// if no arguments passed throw exception
		if(arguments.length == 0)
			throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);
		// if its not a map throw exception
		Object arg = arguments[0];
        
        if(arg == null)
            return null;

        GrailsDomainClass domainClass = this.application.getGrailsDomainClass(clazz.getName());
		if(domainClass != null) {
			Class identityType = domainClass.getIdentifier().getType();
			if(!identityType.isAssignableFrom(arg.getClass())) {
				arg = typeConverter.convertValue(context,arg, identityType);
			}
		}

		return super.getHibernateTemplate().get( clazz, (Serializable)arg );		
	}

}
