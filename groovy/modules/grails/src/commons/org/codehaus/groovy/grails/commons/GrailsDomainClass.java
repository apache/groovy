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


/**
 * <p>Represents a persistable Grails domain class</p>
 * 
 * @author Graeme Rocher
 * @since Jul 5, 2005
 */
public interface GrailsDomainClass extends GrailsClass {


	
	/**
	 * Returns all of the properties of the domain class
	 * @return The domain class properties
	 */
	public GrailsDomainClassProperty[] getProperties();
	/**
	 * Returns all of the persistant properties of the domain class
	 * @return The domain class' persistant properties
	 */
	public GrailsDomainClassProperty[] getPersistantProperties();
	/**
	 * Returns the identifier property
	 * @return The identifier property
	 */
	public GrailsDomainClassProperty getIdentifier();
	/**
	 * Returns the version property
	 * @return The version property
	 */
	public GrailsDomainClassProperty getVersion();
	
	/**
	 * Returns the property for the given name
	 * 
	 * @param name The property for the name
	 * @throws org.codehaus.groovy.grails.exceptions.InvalidPropertyException
	 * @return The domain class property for the given name
	 */
	public GrailsDomainClassProperty getPropertyByName(String name);	
	
	
	/**
	 * Returns the field name for the given property name
	 * @param propertyName
	 * @return
	 */
	public String getFieldName(String propertyName);
	
	/**
	 * Returns the table name for this class
	 */
	public String getTableName();
	
	/**
	 * <p>Returns the default property name of the GrailsClass. For example the property name for 
	 * a class called "User" would be "user"</p>
	 * 
	 * @return
	 */
	public String getPropertyName();
	
	/**
	 * Returns true if the given property is a one to many relationship
	 * @param propertyName The name of the property
	 * @return A boolean value
	 */
	public boolean isOneToMany(String propertyName);
	
	/**
	 * Returns true if the given property is a many to one relationship
	 * @param propertyName The name of the property
	 * @return A boolean value
	 */
	public boolean isManyToOne(String propertyName);
	
	/**
	 * Returns true if the given property is a bi-directional relationship
	 * 
	 * @param propertyName The name of the property
	 * @return A boolean value
	 */
	public boolean isBidirectional(String propertyName);
	
	/**
	 * Returns the type of the related class of the given property
	 * 
	 * @param propertyName The name of the property 
	 * @return The type of the class or null if no relationship exists for the specified property
	 */
	public Class getRelatedClassType(String propertyName);
	
}
