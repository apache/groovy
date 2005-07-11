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
package org.codehaus.groovy.grails.orm.hibernate.cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;

/**
 * Creates runtime configuration mappings for the Grails domain classes
 * based on the work done in the Hibernate Annotations project
 * 
 * @author Graeme Rocher
 * @since 06-Jul-2005
 */
public class GrailsDomainConfiguration extends Configuration {

	private GrailsApplication grailsApplication;
	private Set domainClasses;

	/**
	 * 
	 */
	public GrailsDomainConfiguration() {
		super();		
		this.domainClasses = new HashSet();
	}

	/**
	 * Adds a domain class to the configuration
	 * @param domainClass
	 * @return
	 */
	public GrailsDomainConfiguration addDomainClass( GrailsDomainClass domainClass ) {
		this.domainClasses.add(domainClass);
		return this;		
	}
	/**
	 * @param domain The domain to set.
	 */
	public void setGrailsApplication(GrailsApplication application) {
		this.grailsApplication = application;
		
		GrailsDomainClass[] existingDomainClasses = this.grailsApplication.getGrailsDomainClasses();
		for(int i = 0; i < existingDomainClasses.length;i++) {
			addDomainClass(existingDomainClasses[i]);
		}		
	}
	
	/**
	 * 
	 */
	protected void secondPassCompile() throws MappingException {
		// set the class loader to load Groovy classes
		Thread.currentThread().setContextClassLoader( this.grailsApplication.getClassLoader() );
		// do Grails class configuration
		for(Iterator i = this.domainClasses.iterator();i.hasNext();) {
			GrailsDomainClass domainClass = (GrailsDomainClass)i.next();
			
			GrailsDomainBinder.bindClass(domainClass, super.createMappings());
		}
		
		// call super
		super.secondPassCompile();
	}
	
}
