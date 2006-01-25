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

import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.codehaus.groovy.grails.validation.Constraint;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.validation.Errors;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
/**
 * Extends ConstrainedProperty to provide additional validation against database specific constraints
 * 
 * @author Graeme Rocher
 * @since 10-Nov-2005
 */
public class ConstrainedPersistentProperty extends ConstrainedProperty {

	public static final String UNIQUE_CONSTRAINT = "unique";

	
	private static final String DEFAULT_NOT_UNIQUE_MESSAGE = bundle.getString("default.not.unique.message");
	private HibernateTemplate hibernateTemplate;
	
	static {		
		constraints.put( UNIQUE_CONSTRAINT, UniqueConstraint.class );
	}
	
	static protected abstract class AbstractPersistentConstraint extends AbstractConstraint implements PersistentConstraint {
		
		protected HibernateTemplate constraintHibernateTemplate;

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.orm.hibernate.validation.PersistentConstraint#setHibernateTemplate(org.springframework.orm.hibernate3.HibernateTemplate)
		 */
		public void setHibernateTemplate(HibernateTemplate template) {
			this.constraintHibernateTemplate = template;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#validate(java.lang.Object, org.springframework.validation.Errors)
		 */
		public void validate(Object propertyValue, Errors errors) {
			if(constraintHibernateTemplate == null)
				throw new IllegalStateException("PersistentConstraint requires an instance of HibernateTemplate.");
			
			super.validate(propertyValue, errors);
		}				
	}
	
	/**
	 * 
     * A constraint that validates the uniqueness of a property
	 */
	static private class UniqueConstraint extends AbstractPersistentConstraint {
		
		private boolean unique;

		/**
		 * @return Returns the unique.
		 */
		public boolean isUnique() {
			return unique;
		}

		/* (non-Javadoc)
		 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
		 */
		public void setParameter(Object constraintParameter) {
			if(!(constraintParameter instanceof Boolean)) 
				throw new IllegalArgumentException("Parameter for constraint ["+UNIQUE_CONSTRAINT+"] of property ["+constraintPropertyName+"] of class ["+constraintOwningClass+"] must be a boolean value");
			
			this.unique = ((Boolean)constraintParameter).booleanValue();
			super.setParameter(constraintParameter);
		}

        public String getName() {
            return UNIQUE_CONSTRAINT;
        }

        protected void processValidate(final Object propertyValue, Errors errors) {

            if(unique) {
                List results = this.constraintHibernateTemplate.executeFind( new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        return session.createCriteria( constraintOwningClass )
                                            .add( Restrictions.eq( constraintPropertyName, propertyValue ) )
                                            .list();

                    }

                });
                if(results.size() > 0) {
                    Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue };
                    super.rejectValue(errors,UNIQUE_CONSTRAINT,args,MessageFormat.format( DEFAULT_NOT_UNIQUE_MESSAGE, args ));
                }
            }
        }

		public boolean supports(Class type) {
			return true;
		}
		
	}
	

	public ConstrainedPersistentProperty(Class clazz, String propertyName, Class propertyType) {
		super(clazz, propertyName, propertyType);
	}
	
	
	/**
	 * @return Returns the hibernateTemplate.
	 */
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	/**
	 * @param hibernateTemplate The hibernateTemplate to set.
	 */
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	/**
	 * @return Returns the unique.
	 */
	public boolean isUnique() {
		UniqueConstraint c = (UniqueConstraint)this.appliedConstraints.get( UNIQUE_CONSTRAINT );
		if(c == null)
			return false;
		
		return c.isUnique();
	}
	/**
	 * @param unique The unique to set.
	 */
	public void setUnique(boolean unique) {

		Constraint c = (Constraint)this.appliedConstraints.get( UNIQUE_CONSTRAINT );
			
		if(c != null) {
			c.setParameter( new Boolean(unique) );				
		}
		else {
			c = new UniqueConstraint();
			c.setOwningClass(this.owningClass);
			c.setPropertyName(this.propertyName);
			c.setParameter(new Boolean(unique));					
			this.appliedConstraints.put( UNIQUE_CONSTRAINT,c );				
		}	
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.validation.ConstrainedProperty#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object propertyValue, Errors errors) {
	
		for (Iterator i = this.appliedConstraints.values().iterator(); i.hasNext();) {
			Constraint c = (Constraint) i.next();
			if(c instanceof PersistentConstraint) {
				PersistentConstraint pc = (PersistentConstraint)c;
				pc.setHibernateTemplate(this.hibernateTemplate);
				pc.validate( propertyValue, errors );
			}
			else {
				c.validate( propertyValue, errors);
			}
		}
	}

	
}
