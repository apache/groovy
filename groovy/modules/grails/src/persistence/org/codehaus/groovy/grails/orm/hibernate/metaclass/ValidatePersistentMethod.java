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



import groovy.lang.GroovyObject;

import java.beans.IntrospectionException;
import java.util.List;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.metaclass.DefaultGroovyDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.commons.metaclass.DynamicMethods;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
import org.hibernate.SessionFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
/**
 * A method that validates an instance of a domain class against its constraints 
 * 
 * @author Graeme Rocher
 * @since 07-Nov-2005
 */
public class ValidatePersistentMethod extends AbstractDynamicPersistentMethod {

	private static final String METHOD_NAME = "validate";
	private static final String HAS_ERRORS_PROPERTY = "hasErrors";
	private static final String ERRORS_PROPERTY = "errors";
	private GrailsApplication application;
	



	public ValidatePersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, GrailsApplication application) {
		super(METHOD_NAME, sessionFactory, classLoader);
		if(application == null)
			throw new IllegalArgumentException("Constructor argument 'application' cannot be null");
		this.application = application;
	}

	protected Object doInvokeInternal(Object target, Object[] arguments) {
		Errors errors = new BindException(target, target.getClass().getName());
		Validator validator = application.getGrailsDomainClass( target.getClass().getName() ).getValidator();
		
		Boolean valid = new Boolean(true);
		if(validator != null) {
			validator.validate(target,errors);
			
			if(errors.hasErrors()) {
				valid = new Boolean(!errors.hasErrors());			
				GenericDynamicProperty hasErrorsProperty = new GenericDynamicProperty(HAS_ERRORS_PROPERTY,Boolean.class,valid,true);
				GenericDynamicProperty errorsProperty = new GenericDynamicProperty(ERRORS_PROPERTY,List.class,errors.getAllErrors(),true);
				
				DynamicMethods interceptor;
				try {
					interceptor = new DefaultGroovyDynamicMethodsInterceptor((GroovyObject)target);
					interceptor.addDynamicProperty(hasErrorsProperty);
					interceptor.addDynamicProperty( errorsProperty );			
				} catch (IntrospectionException e) {
					throw new GrailsDomainException("Introspection exception validating object ["+target+"] or class ["+target.getClass()+"]: " + e.getMessage(),e);
				}
			}
		}
		return valid;
	}

}
