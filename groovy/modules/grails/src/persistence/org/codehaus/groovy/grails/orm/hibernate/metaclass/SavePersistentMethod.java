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

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.metaclass.DelegatingMetaClass;
import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.hibernate.SessionFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 7, 2005
 */
public class SavePersistentMethod extends AbstractDynamicPersistentMethod {
	
	private static final String METHOD_SIGNATURE = "save";
	private GrailsApplication application;
	
	public SavePersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, GrailsApplication application) {
		super(METHOD_SIGNATURE,sessionFactory, classLoader);
		
		if(application == null)
			throw new IllegalArgumentException("Constructor argument 'application' cannot be null");
		this.application = application;		
	}

	protected Object doInvokeInternal(Object target, Object[] arguments) {
		
		Errors errors = new BindException(target, target.getClass().getName());
		Validator validator = application.getGrailsDomainClass( target.getClass().getName() ).getValidator();
		boolean doValidation = true;
		if(arguments.length > 0) {
			if(arguments[0] instanceof Boolean) {
				doValidation = ((Boolean)arguments[0]).booleanValue();
			}
		}
		Boolean success = new Boolean(true);
		if(doValidation) {
			if(validator != null) {
				validator.validate(target,errors);
				
				if(errors.hasErrors()) {
					success = new Boolean(!errors.hasErrors());	
					DelegatingMetaClass metaClass = (DelegatingMetaClass)InvokerHelper.getInstance().getMetaRegistry().getMetaClass(target.getClass());
					metaClass.setProperty(target,DomainClassMethods.HAS_ERRORS_PROPERTY,success);
					metaClass.setProperty(target,DomainClassMethods.ERRORS_PROPERTY,errors.getAllErrors());											
				}
				else {
					getHibernateTemplate().saveOrUpdate(target);
				}
			}
		}
		else {
			getHibernateTemplate().saveOrUpdate(target);			
		}
		
		return success;
	}

}
