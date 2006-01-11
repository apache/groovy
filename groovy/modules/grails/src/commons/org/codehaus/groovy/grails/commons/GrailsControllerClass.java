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
package org.codehaus.groovy.grails.commons;


/**
 * <p>Represents a controller class in Grails.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public interface GrailsControllerClass extends InjectableGrailsClass {
    /**
     * The name of the controller contained within the request scope
     */
    String REQUEST_CONTROLLER = "grailsController";

    /**
     * <p>Gets the list of all possible URI's available in this controller.
     *
     * @return list of all possible URI's
     */
    public String[] getURIs();
	
	/**
	 * <p>Tests if a controller maps to a given URI.
	 * 
	 * @return true if controller maps to URI
	 */
	public boolean mapsToURI(String uri);
	
	/**
     * Retrieves the view name for the specified URI
	 * 
	 * @param uri the name of URI
	 * @return the view name of null if not found
	 */
	public String getViewByURI(String uri);
	
	/**
	 * Retrieves the view name for the specified closure name
	 * 
	 * @param closureName The name of the closure
	 * @return The view for the specified closure action
	 */
	public String getViewByName(String closureName);

	/**
	 * <p>Returns a closure property name for a specific URI or null if the URI does not map to a closure.
	 * 
	 * @param uri the URI of the request
	 * @return the closure property name mapped to the URI or null is no closure was found
	 */
	public String getClosurePropertyName(String uri);

	/**
	 * @return True of the controller class is scaffolding
	 */
	public boolean isScaffolding();

}
