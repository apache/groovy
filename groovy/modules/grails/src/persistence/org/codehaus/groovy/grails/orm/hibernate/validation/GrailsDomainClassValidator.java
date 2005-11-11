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
package org.codehaus.groovy.grails.orm.hibernate.validation;

import java.util.Collection;
import java.util.Iterator;

import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
/**
 * A validator that validates a domain class based on the applied constraints 
 * 
 * @author Graeme Rocher
 * @since 07-Nov-2005
 */
public class GrailsDomainClassValidator implements Validator {

	private Collection constrainedProperties;
	private Class targetClass;
	private HibernateTemplate template;

	public GrailsDomainClassValidator(Class targetClass, Collection constrainedProperties, SessionFactory sessionFactory) {
		super();
		if(targetClass == null)
			throw new IllegalArgumentException("Constructor argument 'targetClass' cannot be null");
		if(constrainedProperties == null)
			throw new IllegalArgumentException("Constructor argument 'constrainedProperties' cannot be null");
		if(template == null)
			throw new IllegalArgumentException("Constructor argument 'template' cannot be null");		
		
		
		this.targetClass = targetClass;
		this.constrainedProperties = constrainedProperties;
		this.template = new HibernateTemplate(sessionFactory);
	}

	public boolean supports(Class clazz) {
		return this.targetClass.equals( clazz );
	}

	public void validate(Object obj, Errors errors) {		
		for (Iterator i = constrainedProperties.iterator(); i.hasNext();) {
			ConstrainedProperty c = (ConstrainedProperty)i.next();
			if(c instanceof ConstrainedPersistentProperty) {
				((ConstrainedPersistentProperty)c).setHibernateTemplate(this.template);
			}
			c.validate(obj,errors);
		}

	}

}
