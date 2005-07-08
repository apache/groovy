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

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.springframework.core.io.Resource;

/**
 * @author Graeme Rocher
 * @since Jul 5, 2005
 */
public class DefaultGrailsDomain implements GrailsDomain {

	private GroovyClassLoader cl;
	private Map domainMap;
	private GrailsDomainClass[] grailsDomainClasses;
	private GrailsApplication grailsApplication;
	
	public DefaultGrailsDomain(GrailsApplication app,Resource[] resources)
	 throws IOException, ClassNotFoundException {
		this.grailsApplication = app;
		if(app == null) {
			this.cl = new GroovyClassLoader();
		}
		else {
			this.cl = app.getClassLoader();
		}
		for (int i = 0; resources != null && i < resources.length; i++) {
			try {
				cl.parseClass(resources[i].getFile());
			} catch (CompilationFailedException e) {
				throw new org.codehaus.groovy.grails.exceptions.CompilationFailedException("Compilation error in file [" + resources[i].getFilename() + "]: " + e.getMessage(), e);
			}
		}
		
		Class[] classes = cl.getLoadedClasses();
		this.domainMap = new HashMap();
		for (int i = 0; i < classes.length; i++) {
			// check that it is not a controller or a closure
			if(!GrailsClassUtils.isController(this.grailsApplication, classes[i]) &&
			   !Closure.class.isAssignableFrom(classes[i])	) {
				
				GrailsDomainClass grailsDomainClass = new DefaultGrailsDomainClass(classes[i]);				
				this.domainMap.put(grailsDomainClass.getName().substring(0, 1).toLowerCase() + grailsDomainClass.getName().substring(1), grailsDomainClass);						
			}
		}
		
		this.grailsDomainClasses = ((GrailsDomainClass[])domainMap.values().toArray(new GrailsDomainClass[domainMap.size()]));		
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomain#getGrailsDomainClasses()
	 */
	public GrailsDomainClass[] getGrailsDomainClasses() {
		return this.grailsDomainClasses;
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomain#getGrailsDomainClass(java.lang.String)
	 */
	public GrailsDomainClass getGrailsDomainClass(String name) {
			return (GrailsDomainClass)this.domainMap.get(name);
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.domain.GrailsDomain#getGrailsApplication()
	 */
	public GrailsApplication getGrailsApplication() {
		return this.grailsApplication;
	}

}
