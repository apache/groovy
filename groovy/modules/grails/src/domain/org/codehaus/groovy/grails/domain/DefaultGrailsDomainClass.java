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
package org.codehaus.groovy.grails.domain;


import java.beans.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.domain.exceptions.GrailsDomainException;
import org.codehaus.groovy.grails.domain.exceptions.InvalidPropertyException;

/**
 * @author Graeme Rocher
 * @since 05-Jul-2005
 */
public class DefaultGrailsDomainClass extends AbstractGrailsClass  implements GrailsDomainClass {

	private GrailsDomainClassProperty identifier;
	private GrailsDomainClassProperty version;
	private GrailsDomainClassProperty[] properties;
	private GrailsDomainClassProperty[] persistantProperties;
	private Map propertyMap;

	
	public DefaultGrailsDomainClass(Class clazz) {
		super(clazz, "");
		
		PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
		
		this.propertyMap = new HashMap();
		
		// First go through the properties of the class and create domain properties
		// populating into a map
		for(int i = 0; i < propertyDescriptors.length; i++) {
			
			PropertyDescriptor descriptor = propertyDescriptors[i];
				// ignore properties: GroovyObject metaClass property, notPersistant and notRequired
				if(!descriptor.getName().equals( GrailsDomainClassProperty.META_CLASS ) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.CLASS ) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.NOT_PERSISTANT) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.NOT_REQUIRED)  ) {
					
					
					GrailsDomainClassProperty property = new DefaultGrailsDomainClassProperty(this, descriptor);
					this.propertyMap.put(property.getName(), property);
					
					if(property.isIdentity()) {
						this.identifier = property;				
					}
					else if(property.getName().equals( GrailsDomainClassProperty.VERSION )) {
						this.version = property;
					}
			}
			
		}
		// if no identifier property throw exception
		if(this.identifier == null) {
			throw new GrailsDomainException("Identity property not found, but required in domain class ["+getFullName()+"]" );
		}
		// if no version property throw exception
		if(this.version == null) {
			throw new GrailsDomainException("Version property not found, but required in domain class ["+getFullName()+"]" );
		}		
		// set properties from map values
		this.properties = (GrailsDomainClassProperty[])this.propertyMap.values().toArray( new GrailsDomainClassProperty[this.propertyMap.size()] );
		
		// set persistant properties
		Collection tempList = new ArrayList();
		for(Iterator i = this.propertyMap.values().iterator();i.hasNext();) {
			GrailsDomainClassProperty currentProp = (GrailsDomainClassProperty)i.next();
			if(currentProp.isPersistant() && !currentProp.isIdentity() && !currentProp.getName().equals( GrailsDomainClassProperty.VERSION )) {
				tempList.add(currentProp);
			}
		}
		this.persistantProperties = (GrailsDomainClassProperty[])tempList.toArray( new GrailsDomainClassProperty[tempList.size()]);
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getProperties()
	 */
	public GrailsDomainClassProperty[] getProperties() {
		return this.properties;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getIdentifier()
	 */
	public GrailsDomainClassProperty getIdentifier() {
		return this.identifier;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getVersion()
	 */
	public GrailsDomainClassProperty getVersion() {
		return this.version;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getPersistantProperties()
	 */
	public GrailsDomainClassProperty[] getPersistantProperties() {
		return this.persistantProperties;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getPropertyByName(java.lang.String)
	 */
	public GrailsDomainClassProperty getPropertyByName(String name) {
		if(this.propertyMap.containsKey(name)) {
			return (GrailsDomainClassProperty)this.propertyMap.get(name);
		}
		else {
			throw new InvalidPropertyException("No property found for name ["+name+"]");
		}
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getFieldName(java.lang.String)
	 */
	public String getFieldName(String propertyName) {
		return getPropertyByName(propertyName).getFieldName();
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.AbstractGrailsClass#getName()
	 */
	public String getName() {
		return ClassUtils.getShortClassName(super.getName());
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#getTableName()
	 */
	public String getTableName() {
		return ClassUtils.getShortClassName(getName()).toUpperCase();
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#isOneToMany(java.lang.String)
	 */
	public boolean isOneToMany(String propertyName) {
		return getPropertyByName(propertyName).isOneToMany();
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomainClass#isManyToOne(java.lang.String)
	 */
	public boolean isManyToOne(String propertyName) {
		return getPropertyByName(propertyName).isManyToOne();
	}
	
	protected Object getPropertyValue(String name, Class type) {
		return super.getPropertyValue(name,type);
	}
}
