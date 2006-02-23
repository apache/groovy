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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * An interface defining methods to handle CRUD operations involved in scaffolding
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public interface ScaffoldRequestHandler {

	/**
	 * Sets the domain to use during scaffolding. A scaffold domain provides methods to manipulate
	 * the scaffolded persistent class
	 * @param domain
	 */
	void setScaffoldDomain(ScaffoldDomain domain);

    /**
     * @return The domain being scaffolded
     */
    ScaffoldDomain getScaffoldDomain();

    /**
	 * Handles a request to list all scaffolded instances
	 * @param request 
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */
	Map handleList(HttpServletRequest request, HttpServletResponse reponse);
	
	/**
	 * Handles a request to show a scaffolded instance
	 * 
	 * @param request
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */
	Map handleShow(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback);
	
	/**
	 * Handles a request to delete a scaffolded instance
	 * 
	 * @param request
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */	
	Map handleDelete(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback);

    /**
     * Handles a create request
     *
     * @param request
     * @param reponse
     * @param callback
     * @return Optionally a map which reperesents the generated model
     */
    Map handleCreate(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback);
    /**
	 * Handles a request to save a scaffolded instance
	 * 
	 * @param request
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */	
	Map handleSave(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback);
	
	/**
	 * Handles a request to update a scaffolded instance
	 * 
	 * @param request
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */	
	Map handleUpdate(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback);	
	
	/**
	 * Handles a request to find a scaffolded instance
	 * 
	 * @param request
	 * @param reponse
	 * @return Optionally a map which represents the generated model
	 */	
	Map handleFind(HttpServletRequest request, HttpServletResponse reponse);
		
}
