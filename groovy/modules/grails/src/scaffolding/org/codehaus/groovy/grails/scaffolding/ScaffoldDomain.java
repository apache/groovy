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
package org.codehaus.groovy.grails.scaffolding;

import org.springframework.validation.Validator;

import java.io.Serializable;
import java.util.List;

/**
 * The main interface used by scaffolded controllers to access instances of persistent classes
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public interface ScaffoldDomain {

    /**
     * Sets the name of the identity property so that type conversion can be performed
     * @param identityPropertyName
     */
    void setIdentityPropertyName(String identityPropertyName);

    /**
     * Sets the validator to use
     * @param validator
     */
    void setValidator(Validator validator);
    /**
	 * Retrieves the scaffolded persistent class
	 */
	Class getPersistentClass();
		
	/** 
	 * @return All persistent instances of the scaffolded class 
	 */
	List list();
	/**
	 * 
	 * @param max The maximum number of instances to return
	 * @return All persistent instances up to the maximum specified value
	 */
	List list(int max);
	/**
	 * 
	 * @param max The maximum number to return
	 * @param offset The offset position
	 * @return All persistent instances up to the maximum, offset by the specified value
	 */
	List list(int max, int offset);
	
	/**
	 * 
	 * @param max The maximum number to return (Set to -1 for no maximum)
	 * @param offset The offset position (Set to -1 for no offset)
	 * @param order either "ASC" for ascending or "DESC" for descending
	 * 
	 * @return All persistent instances up to the maximum, offset by the specified value
	 */
	List list(int max, int offset, String sort);	
	/**
	 * 
	 * @param max The maximum number to return (Set to -1 for no maximum)
	 * @param offset The offset position (Set to -1 for no offset)
	 * @param order either "ASC" for ascending or "DESC" for descending
	 * @param sort The property name to sort by 
	 * 
	 * @return All persistent instances up to the maximum, offset by the specified value
	 */
	List list(int max, int offset, String sort, String order);	
	/**
	 * Finds all persistent instances for the specified property name and value
	 * 
	 * @param by The property to find by
	 * @param q The query criteria
	 * @return A list of found persistent instances
	 */
	List find(String by, Object q);
	
	/**
	 * @see #find(String, Object) for description
	 * 
	 * @param max The maximum number of results to return
	 */
	List find(String by, Object q, int max);
	
	/**
	 * @see #find(String, Object) for description
	 * 
	 * @param max The maximum number of results to return
	 * @param offset The offset position
	 */	
	List find(String by,Object q, int max, int offset);
	
	/**
	 * Finds all the persistent instances for the specified properties and query values
	 * 
	 * @param by The properties to find by
	 * @param q The query values
	 * @return A list of persistent instances
	 */
	List find(String[] by, Object[] q);
	
	/**
	 * @see #find(String[], Object[]) for description
	 * 
	 * @param max The maximum number of results to return
	 */
	List find(String[] by, Object q[], int max);
	
	/**
	 * @see #find(String[], Object[]) for description
	 * 
	 * @param max The maximum number of results to return
	 * @param offset The offset position
	 */	
	List find(String[] by,Object[] q, int max, int offset);	
	
	/**
	 * Saves an instance of the persistent class using the specified properties
	 * 
	 * @param domainObject The domain object to save
	 * @param callback Any validation errors that may have occured during saving
	 * 
	 * @return The saved instance or null if save was unsuccessful
	 */
	boolean save(Object domainObject, ScaffoldCallback callback);
	
	/**
	 * Updates an existing instance
	 * 
	 * @param id
	 * @param domainObject
	 * @return The updated instance
	 */
	boolean update(Object domainObject, ScaffoldCallback callback);
	
	/**
	 * 	
	 * @param id
	 * @return
	 */
	Object delete(Serializable id);
	
	/**
	 * Retrieves an persistent class instance for the specified id
	 * @param id
	 * @return
	 */
	Object get(Serializable id);
	
	/**
	 * Returns the plural name of the domain
	 * @return
	 */
	String getPluralName();

	/**
	 * Returns the singular name of the domain
	 * @return
	 */
	String getSingularName();

	/**
	 * 
	 * @return The identity property name
	 */
	String getIdentityPropertyName();

	/**
	 * 
	 * @return The full name of the domain class
	 */
	String getName();

	/**
	 * 
	 * @return A new instance of the domain class
	 */
	Object newInstance();
}
