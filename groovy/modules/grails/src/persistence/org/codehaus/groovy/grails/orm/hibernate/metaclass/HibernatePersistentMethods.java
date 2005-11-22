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

import java.beans.IntrospectionException;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicMethods;
import org.codehaus.groovy.grails.metaclass.SetPropertiesDynamicProperty;
import org.hibernate.SessionFactory;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public class HibernatePersistentMethods extends AbstractDynamicMethods {

	public HibernatePersistentMethods(GrailsApplication application, Class theClass, SessionFactory sessionFactory, ClassLoader classLoader)
			throws IntrospectionException {
		super(theClass);
		// dynamic methods
		addDynamicMethodInvocation(new SavePersistentMethod(sessionFactory, classLoader));
		addDynamicMethodInvocation(new DeletePersistentMethod(sessionFactory, classLoader));
		addDynamicMethodInvocation(new RefreshPersistentMethod(sessionFactory, classLoader));		
		addDynamicMethodInvocation(new ValidatePersistentMethod(sessionFactory, classLoader, application ));
		
		// static methods
		addStaticMethodInvocation(new FindAllPersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new FindByPersistentMethod(application,sessionFactory, classLoader));
		addStaticMethodInvocation(new GetByPersistentMethod(application,sessionFactory, classLoader));
		addStaticMethodInvocation(new FindPersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new ListOrderByPersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new ListPersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new FindWherePersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new GetPersistentMethod(sessionFactory, classLoader));
		addStaticMethodInvocation(new CreateCriteriaPersistentMethod(sessionFactory, classLoader));
		
		// add dynamic properties
		addDynamicProperty( new SetPropertiesDynamicProperty() );
		
	}
	
}
