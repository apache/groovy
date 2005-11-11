package org.codehaus.groovy.grails.orm.hibernate.metaclass;



import groovy.lang.GroovyObject;

import java.beans.IntrospectionException;
import java.util.List;

import org.codehaus.groovy.grails.commons.metaclass.DefaultGroovyDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.commons.metaclass.DynamicMethods;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
import org.codehaus.groovy.grails.orm.hibernate.validation.GrailsDomainClassValidator;
import org.codehaus.groovy.grails.validation.metaclass.ConstraintsDynamicProperty;
import org.hibernate.SessionFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class ValidatePersistentMethod extends AbstractDynamicPersistentMethod {

	private static final String METHOD_NAME = "validate";
	private static final String HAS_ERRORS_PROPERTY = "hasErrors";
	private static final String ERRORS_PROPERTY = "errors";
	
	private GrailsDomainClassValidator validator;


	public ValidatePersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, GrailsDomainClassValidator validator) {
		super(METHOD_NAME, sessionFactory, classLoader);
		if(validator == null)
			throw new IllegalArgumentException("Constructor argument 'validator' cannot be null");
		
		this.validator = validator;
	}

	protected Object doInvokeInternal(Object target, Object[] arguments) {
		Errors errors = new BindException(target, target.getClass().getName());
		validator.validate(target,errors);
		
		Boolean hasErrors = new Boolean(errors.hasErrors());
		GenericDynamicProperty hasErrorsProperty = new GenericDynamicProperty(HAS_ERRORS_PROPERTY,Boolean.class,hasErrors,true);
		GenericDynamicProperty errorsProperty = new GenericDynamicProperty(ERRORS_PROPERTY,List.class,errors.getAllErrors(),true);
		
		DynamicMethods interceptor;
		try {
			interceptor = new DefaultGroovyDynamicMethodsInterceptor((GroovyObject)target);
			interceptor.addDynamicProperty(hasErrorsProperty);
			interceptor.addDynamicProperty( errorsProperty );			
		} catch (IntrospectionException e) {
			throw new GrailsDomainException("Introspection exception validating object ["+target+"] or class ["+target.getClass()+"]: " + e.getMessage(),e);
		}
		return hasErrors;
	}

}
