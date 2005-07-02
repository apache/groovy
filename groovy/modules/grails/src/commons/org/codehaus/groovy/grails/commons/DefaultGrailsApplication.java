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

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.Resource;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class DefaultGrailsApplication implements GrailsApplication {

	private GroovyClassLoader cl = null;
	private GrailsControllerClass[] controllerClasses = null;
	private Map controllerMap = null;
	
	public DefaultGrailsApplication(Resource[] resources) throws IOException, ClassNotFoundException {
		super();
		
		this.cl = new GroovyClassLoader();
		for (int i = 0; resources != null && i < resources.length; i++) {
			try {
				cl.parseClass(resources[i].getFile());
			} catch (CompilationFailedException e) {
				throw new org.codehaus.groovy.grails.exceptions.CompilationFailedException("Compilation error in file [" + resources[i].getFilename() + "]: " + e.getMessage(), e);
			}
		}
		
		Class[] classes = cl.getLoadedClasses();
		this.controllerMap = new HashMap();
		for (int i = 0; i < classes.length; i++) {
			if (classes[i].getName().endsWith(DefaultGrailsControllerClass.CONTROLLER) /* && not ends with FromController */) {
				GrailsControllerClass grailsControllerClass = new DefaultGrailsControllerClass(classes[i]);
				if (grailsControllerClass.getAvailable()) {
					this.controllerMap.put(grailsControllerClass.getName().substring(0, 1).toLowerCase() + grailsControllerClass.getName().substring(1), grailsControllerClass);
				}
			}
		}
		
		this.controllerClasses = ((GrailsControllerClass[])controllerMap.values().toArray(new GrailsControllerClass[controllerMap.size()]));
	}

	public GrailsControllerClass[] getControllers() {
		return this.controllerClasses;
	}

	public GrailsControllerClass getController(String name) {
		return (GrailsControllerClass)this.controllerMap.get(name);
	}
}
