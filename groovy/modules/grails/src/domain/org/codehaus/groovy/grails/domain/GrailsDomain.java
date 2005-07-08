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

import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * @author Graeme Rocher
 * @since Jul 5, 2005
 * 
 * Interface to represent all the domain class in the application domain
 */
public interface GrailsDomain {

	/**
	 * <p>Returns an array of all the Grails Domain classes</p>
	 * 
	 * @return The domain classes in the domain
	 */
	public GrailsDomainClass[] getGrailsDomainClasses();
	
	/**
	 * <p>Retrieves a domain class for the specified name</p>
	 * 
	 * @param name The name of the domain class to retrieve
	 * @return The retrieved domain class
	 */
	public GrailsDomainClass getGrailsDomainClass(String name);
	
	/**
	 * Returns the GrailsApplication instance for the domain
	 * @return The GrailsApplication instance
	 */
	public GrailsApplication getGrailsApplication();
}
