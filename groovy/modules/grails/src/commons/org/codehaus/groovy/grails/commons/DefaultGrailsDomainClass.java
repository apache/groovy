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


import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.metaclass.GroovyDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.commons.metaclass.DynamicMethods;
import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException;
import org.codehaus.groovy.grails.validation.metaclass.ConstraintsDynamicProperty;
import org.springframework.validation.Validator;

/**
 * @author Graeme Rocher
 * @since 05-Jul-2005
 */
public class DefaultGrailsDomainClass extends AbstractGrailsClass  implements GrailsDomainClass {

	private static final Log LOG  = LogFactory.getLog(DefaultGrailsDomainClass.class);
	
	private GrailsDomainClassProperty identifier;
	private GrailsDomainClassProperty version;
	private GrailsDomainClassProperty[] properties;
	private GrailsDomainClassProperty[] persistantProperties;
	private Map propertyMap;
	private Map relationshipMap;
	private Map constraints = new HashMap();
	private Validator validator;

	
	public DefaultGrailsDomainClass(Class clazz) {
		super(clazz, "");
		
		PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
		
		this.propertyMap = new HashMap();
		this.relationshipMap = (Map)getPropertyValue( GrailsDomainClassProperty.RELATIONSHIPS, Map.class );
		if(this.relationshipMap == null) {
			this.relationshipMap = new HashMap();
		}
		// process the constraints
		evaluateConstraints();		
		// First go through the properties of the class and create domain properties
		// populating into a map
		for(int i = 0; i < propertyDescriptors.length; i++) {
			
			PropertyDescriptor descriptor = propertyDescriptors[i];
				// ignore certain properties
				if(!descriptor.getName().equals( GrailsDomainClassProperty.META_CLASS ) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.CLASS ) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.TRANSIENT) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.RELATIONSHIPS) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.EVANESCENT) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.OPTIONAL) &&
				   !descriptor.getName().equals( GrailsDomainClassProperty.CONSTRAINTS ))  {
					
					
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
		
		// establish relationships
		establishRelationships();

		
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
	
	/**
	 * Evaluates the constraints closure to build the list of constraints
	 *
	 */
	private void evaluateConstraints() {
		Closure constraintsClosure = (Closure)getPropertyValue( GrailsDomainClassProperty.CONSTRAINTS, Closure.class );
		if(constraintsClosure != null) {
			GroovyObject instance = (GroovyObject)getReference().getWrappedInstance();
			try {
				DynamicMethods interceptor = new GroovyDynamicMethodsInterceptor(instance);
				interceptor.addDynamicProperty( new ConstraintsDynamicProperty() );
				
				this.constraints = (Map)instance.getProperty(GrailsDomainClassProperty.CONSTRAINTS);				
				
			} catch (IntrospectionException e) {
				LOG.error("Introspection error reading domain class ["+getFullName()+"] constraints: " + e.getMessage(), e);
			}
		}		
	}

	/**
	 * Calculates the relationship type based other types referenced
	 *
	 */
	private void establishRelationships() {
		for(Iterator i = this.propertyMap.values().iterator();i.hasNext();  ) {
			DefaultGrailsDomainClassProperty currentProp = (DefaultGrailsDomainClassProperty)i.next();
			Class currentPropType = currentProp.getType();
			// establish if the property is a one-to-many
			// if it is a Set and there are relationships defined
			// and it is defined as persistent
			if(	currentPropType.equals( Set.class ) && 
				currentProp.isPersistant() ) {
				
				establishRelationshipForSet( currentProp);
			}
			// otherwise if the type is a domain class establish relationship
			else if(GrailsClassUtils.isDomainClass(currentPropType) && 
					currentProp.isPersistant()) {
				
				establishDomainClassRelationship( currentProp );
			}			
		}

	}
	
	/**
	 * Establishes a relationship for a java.util.Set
	 * 
	 * @param currentProp
	 * @param currentPropType
	 */
	private void establishRelationshipForSet(DefaultGrailsDomainClassProperty property) {
		// is it a relationship
		Class relatedClassType = getRelatedClassType( property.getName() );
		
		if(relatedClassType != null) {
			// set the referenced type in the property
			property.setReferencedPropertyType(relatedClassType);
			// if the related type is a domain class
			// then figure out what kind of relationship it is
			if(GrailsClassUtils.isDomainClass( relatedClassType )) {
				
				
				// check the relationship defined in the referenced type
				// if it is also a Set/domain class etc.
				Map relatedClassRelationships = (Map)GrailsClassUtils.getPropertyValue( relatedClassType, GrailsDomainClassProperty.RELATIONSHIPS, Map.class );
				Class relatedClassPropertyType = null;
				
				// if the related type has a relationships map it may be a many-to-many
				// figure out if there is a many-to-many relationship defined
				if(	relatedClassRelationships != null && 
					!relatedClassRelationships.isEmpty() ) {
					
					String relatedClassPropertyName = null;
					// retrieve the relationship property
					for(Iterator i = relatedClassRelationships.keySet().iterator();i.hasNext();) {
						String currentKey = (String)i.next();
						Class currentClass = (Class) relatedClassRelationships.get( currentKey );
						if(currentClass.getName().equals(  getClazz().getName() )) {
							relatedClassPropertyName = currentKey;
							break;
						}
					}
															
					// if there is one defined get the type
					if(relatedClassPropertyName != null) {
						relatedClassPropertyType = GrailsClassUtils.getProperyType( relatedClassType, relatedClassPropertyName);								
					}
				}
				// otherwise figure out if there is a one-to-many relationship by retrieving any properties that are of the related type
				// if there is more than one property then (for the moment) ignore the relationship
				if(relatedClassPropertyType == null) {
					PropertyDescriptor[] descriptors = GrailsClassUtils.getPropertiesOfType(relatedClassType, getClazz());
					if(descriptors.length == 1) {
						relatedClassPropertyType = descriptors[0].getPropertyType();
					}					
				}	
				
				establishRelationshipForSetToType(property,relatedClassPropertyType);
			}
			// otherwise set it to not persistent as you can't persist
			// relationships to non-domain classes
			else {
				property.setPersistant(false);
			}								
		}
		else {
			// no relationship defined for set. 
			// set not persistent
			property.setPersistant(false);
		}
		
	}

	/**
	 * Establishes whether the relationship is a bi-directional or uni-directional one-to-many
	 * and applies the appropriate settings to the specified property
	 * 
	 * @param property The property to apply settings to
	 * @param relType The related type
	 */	
	private void establishRelationshipForSetToType(DefaultGrailsDomainClassProperty property, Class relatedClassPropertyType) {
		
		if(relatedClassPropertyType == null) {
			// uni-directional one-to-many
			property.setOneToMany(true);			
			property.setBidirectional(false);
		}		
		else if( relatedClassPropertyType.equals( Set.class ) ){
			// many-to-many
			property.setManyToMany(true);
			property.setBidirectional(true);
		}
		else if( GrailsClassUtils.isDomainClass( relatedClassPropertyType ) ) {
			// bi-directional one-to-many
			property.setOneToMany( true );
			property.setBidirectional( true );
		}
	}

	
	/**
	 * Establish relationship with related domain class
	 * 
	 * @param property
	 */
	private void establishDomainClassRelationship(DefaultGrailsDomainClassProperty property) {
		Class propType = property.getType();
		// establish relationship to type
		Map relatedClassRelationships = (Map)GrailsClassUtils.getPropertyValue( propType, GrailsDomainClassProperty.RELATIONSHIPS, Map.class );		
		Class relatedClassPropertyType = null;
		
		// if there is a relationships map use that to find out 
		// whether it is mapped to a Set
		if(	relatedClassRelationships != null &&	
			!relatedClassRelationships.isEmpty() ) {

									
			String relatedClassPropertyName = null;
			// retrieve the property name
			for(Iterator i = relatedClassRelationships.keySet().iterator();i.hasNext();) {
				String currentKey = (String)i.next();
				Class currentClass = (Class)relatedClassRelationships.get( currentKey );
				if(property.getDomainClass().getClazz().getName().equals(  currentClass.getName()  )) {
					relatedClassPropertyName = currentKey;
					break;
				}					
			}
			// get the type of the property												
			relatedClassPropertyType = GrailsClassUtils.getProperyType( propType, relatedClassPropertyName );									
		}
		// otherwise retrieve all the properties of the type from the associated class
		if(relatedClassPropertyType == null) {
			PropertyDescriptor[] descriptors = GrailsClassUtils.getPropertiesOfType(propType, getClazz());
			
			// if there is only one then the association is established
			if(descriptors.length == 1) {
				relatedClassPropertyType = descriptors[0].getPropertyType();
			}
		}
		
		
		//	establish relationship based on this type
		establishDomainClassRelationshipToType( property, relatedClassPropertyType );		
	}

	/**
	 * @param property
	 * @param relatedClassPropertyType
	 */
	private void establishDomainClassRelationshipToType(DefaultGrailsDomainClassProperty property, Class relatedClassPropertyType) {
		// uni-directional one-to-one
		if(relatedClassPropertyType == null) {
			property.setOneToOne(true);
			property.setBidirectional(false);
		}
		// bi-directional many-to-one
		else if(relatedClassPropertyType.equals( Set.class )) {
			property.setManyToOne(true);
			property.setBidirectional(true);
		}
		// bi-directional one-to-one
		else if( GrailsClassUtils.isDomainClass( relatedClassPropertyType ) ) {
			property.setOneToOne(true);
			property.setBidirectional(true);
		}
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
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#getRelationshipType(java.lang.String)
	 */
	public Class getRelatedClassType(String propertyName) {		
		return (Class)this.relationshipMap.get(propertyName);
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#getPropertyName()
	 */
	public String getPropertyName() {
		String shortTypeName = ClassUtils.getShortClassName( getName() );
		return shortTypeName.substring(0,0).toLowerCase() + shortTypeName.substring(1);
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#isBidirectional()
	 */
	public boolean isBidirectional(String propertyName) {
		return getPropertyByName(propertyName).isBidirectional();
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#getConstraints()
	 */
	public Map getConstrainedProperties() {
		return Collections.unmodifiableMap(this.constraints);
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#getValidator()
	 */
	public Validator getValidator() {
		return this.validator;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.commons.GrailsDomainClass#setValidator(Validator validator)
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}	
}
