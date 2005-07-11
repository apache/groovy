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
package org.codehaus.groovy.grails.commons;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Graeme Rocher
 * @since 05-Jul-2005
 */
public class DefaultGrailsDomainClassProperty implements GrailsDomainClassProperty {

	
	private GrailsDomainClass owner;
	private PropertyDescriptor descriptor;
	private boolean persistant;
	private boolean optional;
	private boolean identity;
	private String name;
	private Class type;
	
		
	public DefaultGrailsDomainClassProperty(DefaultGrailsDomainClass owner, PropertyDescriptor descriptor)  {
		this.owner = owner;
		this.descriptor = descriptor;
		// required by default
		this.optional = false;
		// persistant by default
		this.persistant = true;
		this.name = this.descriptor.getName();
		this.type = this.descriptor.getPropertyType();
		this.identity = this.descriptor.getName().equals( IDENTITY );
		// get the not required descritor from the owner bean
		List optionalProps = null;
		List transientProps = null;

		optionalProps = (List)owner.getPropertyValue( OPTIONAL, List.class );
		transientProps= (List)owner.getPropertyValue( TRANSIENT, List.class );
		
		// Undocumented feature alert! Steve insisted on this :-)
		List evanescent = (List)owner.getPropertyValue( EVANESCENT, List.class );
		if(evanescent != null) {
			if(transientProps == null) 
				transientProps = new ArrayList();
			
			transientProps.addAll(evanescent);
		}
		// establish of property is required
		if(optionalProps != null) {
			for(Iterator i = optionalProps.iterator();i.hasNext();) {
				
				// make sure its a string otherwise ignore. Note: Maybe put a warning here?
				Object currentObj = i.next();
				if(currentObj instanceof String) {
					String propertyName = (String)currentObj;
					// if the property name in the not required list
					// matches this property name set not required
					if(propertyName.equals( this.name )) {
						this.optional = true;
						break;
					}
				}
			}
		}
		// establish if property is persistant
		if(transientProps != null) {
			for(Iterator i = transientProps.iterator();i.hasNext();) {
				
				// make sure its a string otherwise ignore. Note: Again maybe a warning?
				Object currentObj = i.next();
				if(currentObj instanceof String) {
					String propertyName = (String)currentObj;
					// if the property name is on the not persistant list
					// then set persistant to false
					if(propertyName.equals( this.name )) {
						this.persistant = false;
						break;
					}
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#getType()
	 */
	public Class getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isPersistant()
	 */
	public boolean isPersistant() {
		return this.persistant;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isRequired()
	 */
	public boolean isOptional() {
		return this.optional;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isIdentity()
	 */
	public boolean isIdentity() {
		return this.identity;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isOneToMany()
	 */
	public boolean isOneToMany() {
		// TODO: Implement one to many logic
		return false;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isManyToOne()
	 */
	public boolean isManyToOne() {
		// TODO: Many to one logic
		return false;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#getFieldName()
	 */
	public String getFieldName() {
		return getName().toUpperCase();
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClassProperty#isOneToOne()
	 */
	public boolean isOneToOne() {
		// TODO: One to One logic
		return false;
	}


}
