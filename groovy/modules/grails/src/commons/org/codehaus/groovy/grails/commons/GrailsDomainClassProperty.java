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
 * A property of a GrailsDomainClass instance
 * 
 * @author Graeme Rocher
 * @since Jul 5, 2005
 */
public interface GrailsDomainClassProperty {
	
	String IDENTITY = "id";
	String VERSION = "version";
	String OPTIONAL = "optional";
	String TRANSIENT = "transients";
	String EVANESCENT = "evanescent";
	Object META_CLASS = "metaClass";
	String CLASS = "class";	
	
	/**
	 * Returns the name of the property
	 * @return The property name
	 */
	public String getName();
	/**
	 * Returns the type for the domain class
	 * @return
	 */
	public Class getType();
	/**
	 * Returns true if the domain class property is a persistant property
	 * @return 
	 */
	public boolean isPersistant();
	/**
	 * Returns true if the property is required
	 * @return
	 */
	public boolean isOptional();
	/**
	 * Returns true of the property is an identifier
	 * @return
	 */
	public boolean isIdentity();
	
	/**
	 * Returns true if the property is a one-to-many relationship
	 * @return
	 */
	public boolean isOneToMany();
	
	/**
	 * Returns true if the property is a many-to-one relationship
	 * @return
	 */
	public boolean isManyToOne();
	/**
	 * Returns the domain field name for this property
	 */
	public String getFieldName();
	/**
	 * Returns true if the property is a one-to-one relationship 
	 * @return
	 */
	public boolean isOneToOne();
}
