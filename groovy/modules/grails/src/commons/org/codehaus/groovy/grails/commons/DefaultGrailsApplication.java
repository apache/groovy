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
import groovy.lang.GroovyResourceLoader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.grails.exceptions.MoreThanOneActiveDataSourceException;
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
	private GrailsPageFlowClass[] pageFlows = null;
	private GrailsDomainClass[] domainClasses = null;
	private GrailsDataSource dataSource = null;
	private GrailsServiceClass[] services = null;
	private GrailsBootstrapClass[] bootstrapClasses = null;
	
	private Map controllerMap = null;
	private Map domainMap = null;
	private Map pageFlowMap = null;
	private Map serviceMap = null;
	private Map bootstrapMap = null;
	
	private Class[] allClasses = null;
	
	
	private static Logger log = Logger.getLogger(DefaultGrailsApplication.class);
	
	public DefaultGrailsApplication(final Class[] classes, GroovyClassLoader classLoader) {
		if(classes == null)
			throw new IllegalArgumentException("Constructor argument 'classes' cannot be null");
		
		configureLoadedClasses(classes);
		this.cl = classLoader;
	}
	public DefaultGrailsApplication(final Resource[] resources) throws IOException, ClassNotFoundException {
		super();
		
		log.debug("Loading Grails application.");

		final Collection loadedResources = new ArrayList();
		
		GroovyResourceLoader resourceLoader = new GroovyResourceLoader() {
			public URL loadGroovySource(String resource) {
				String filename = resource.replace('.', '/') + ".groovy";
				Resource foundResource = null;
				for (int i = 0; resources != null && i < resources.length; i++) {
					if (resources[i].getFilename().endsWith(filename)) {
						if (foundResource == null) {
							foundResource = resources[i];
						} else {
							throw new IllegalArgumentException("Resources [" + resources[i].getFilename() + "] and [" + foundResource.getFilename() + "] end with [" + filename + "]. Cannot load because of duplicate match!");
						}
					}
				}
				try {
					if (foundResource != null) {
						loadedResources.add(foundResource);
						return foundResource.getURL();
					} else {
						return null;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		this.cl = new GroovyClassLoader();
		this.cl.setResourceLoader(resourceLoader);
		
		
			for (int i = 0; resources != null && i < resources.length; i++) {
				log.debug("Loading groovy file :[" + resources[i].getFile().getAbsolutePath() + "]");
				if (!loadedResources.contains(resources[i])) {
					try {
						cl.parseClass(resources[i].getFile());
					} catch (CompilationFailedException e) {
						throw new org.codehaus.groovy.grails.exceptions.CompilationFailedException("Compilation error parsing file ["+resources[i].getFilename()+"]: " + e.getMessage(), e);
					}						
				}				
			}			
		
		// get all the classes that were loaded
		if(log.isDebugEnabled())
			log.debug( "loaded classes: ["+Arrays.toString(this.cl.getLoadedClasses())+"]" );
		
		Class[] classes = this.cl.getLoadedClasses();		
		this.allClasses = classes;		
		configureLoadedClasses(classes);
	}

	private void configureLoadedClasses(Class[] classes) {
		
		// first load the domain classes
		this.domainMap = new HashMap();
		log.debug("Going to inspect domain classes.");
		for (int i = 0; i < classes.length; i++) {
			log.debug("Inspecting [" + classes[i].getName() + "]");
			if (Modifier.isAbstract(classes[i].getModifiers())) {
				log.debug("[" + classes[i].getName() + "] is abstract.");
				continue;
			}
			// check that it is a domain class
			if(GrailsClassUtils.isDomainClass(classes[i])) {
				log.debug("[" + classes[i].getName() + "] is a domain class.");
				GrailsDomainClass grailsDomainClass = new DefaultGrailsDomainClass(classes[i]);				
				this.domainMap.put(grailsDomainClass.getFullName(), grailsDomainClass);		
						
			} else {
				log.debug("[" + classes[i].getName() + "] is not a domain class.");
			}
		}		
		
		this.controllerMap = new HashMap();
		this.pageFlowMap = new HashMap();
		this.serviceMap = new HashMap();
		this.bootstrapMap = new HashMap();
		for (int i = 0; i < classes.length; i++) {
			if (Modifier.isAbstract(classes[i].getModifiers())) {
				continue;
			}
			if (GrailsClassUtils.isControllerClass(classes[i])  /* && not ends with FromController */) {
				GrailsControllerClass grailsControllerClass = new DefaultGrailsControllerClass(classes[i]);
				if (grailsControllerClass.getAvailable()) {
					this.controllerMap.put(grailsControllerClass.getFullName(), grailsControllerClass);
				}
			} else if (GrailsClassUtils.isPageFlowClass(classes[i])) {
				GrailsPageFlowClass grailsPageFlowClass = new DefaultGrailsPageFlowClass(classes[i]);
				if (grailsPageFlowClass.getAvailable()) {
					this.pageFlowMap.put(grailsPageFlowClass.getFullName(), grailsPageFlowClass);
				}
			} else if (GrailsClassUtils.isDataSource(classes[i])) {
				GrailsDataSource tmpDataSource = new DefaultGrailsDataSource(classes[i]);
				if (tmpDataSource.getAvailable()) {
					if (dataSource == null) {
						dataSource = tmpDataSource;
					} else {
						throw new MoreThanOneActiveDataSourceException("More than one active data source is configured!");
					}
				}
			} else if (GrailsClassUtils.isService(classes[i])) {
				GrailsServiceClass grailsServiceClass = new DefaultGrailsServiceClass(classes[i]);
				serviceMap.put(grailsServiceClass.getFullName(), grailsServiceClass);
			}
			else if(GrailsClassUtils.isBootstrapClass(classes[i])) {
				GrailsBootstrapClass grailsBootstrapClass = new DefaultGrailsBootstrapClass(classes[i]);
				this.bootstrapMap.put(grailsBootstrapClass.getFullName(),grailsBootstrapClass);
			}
		}
		
		this.controllerClasses = ((GrailsControllerClass[])controllerMap.values().toArray(new GrailsControllerClass[controllerMap.size()]));
		this.pageFlows = ((GrailsPageFlowClass[])pageFlowMap.values().toArray(new GrailsPageFlowClass[pageFlowMap.size()]));
		this.domainClasses = ((GrailsDomainClass[])this.domainMap.values().toArray(new GrailsDomainClass[domainMap.size()]));
		this.services = ((GrailsServiceClass[])this.serviceMap.values().toArray(new GrailsServiceClass[serviceMap.size()]));
		this.bootstrapClasses = ((GrailsBootstrapClass[])this.bootstrapMap.values().toArray(new GrailsBootstrapClass[bootstrapMap.size()]));
		
		configureDomainClassRelationships();	
	}
	/**
	 * Sets up the relationships between the domain classes, this has to be done after
	 * the intial creation to avoid looping
	 *
	 */
	private void configureDomainClassRelationships() {

		for (int i = 0; i < this.domainClasses.length; i++) {
			GrailsDomainClassProperty[] props = this.domainClasses[i].getPersistantProperties();
			
			for (int j = 0; j < props.length; j++) {
				if(props[j].isAssociation()) {
					DefaultGrailsDomainClassProperty prop = (DefaultGrailsDomainClassProperty)props[j];
					GrailsDomainClass referencedGrailsDomainClass = (GrailsDomainClass)this.domainMap.get( props[j].getReferencedPropertyType().getName() );
					prop.setReferencedDomainClass(referencedGrailsDomainClass);
					
				}
			}
						
		}
		
		for (int i = 0; i < this.domainClasses.length; i++) {
			GrailsDomainClassProperty[] props = this.domainClasses[i].getPersistantProperties();
			
			for (int j = 0; j < props.length; j++) {
				if(props[j].isAssociation()) {
					DefaultGrailsDomainClassProperty prop = (DefaultGrailsDomainClassProperty)props[j];
					GrailsDomainClassProperty[] referencedProperties =  prop.getReferencedDomainClass().getPersistantProperties();					
					for (int k = 0; k < referencedProperties.length; k++) {
						if(referencedProperties[k].getReferencedPropertyType().equals( this.domainClasses[i].getClazz())) {
							prop.setOtherSide(referencedProperties[k]);
							break;
						}
					}		
				}
			}
						
		}		
	
			
	}

	public GrailsControllerClass[] getControllers() {
		return this.controllerClasses;
	}

	public GrailsControllerClass getController(String name) {
		return (GrailsControllerClass)this.controllerMap.get(name);
	}
	
	public GrailsControllerClass getControllerByURI(String uri) {
		for (int i = 0; i < controllerClasses.length; i++) {
			if (controllerClasses[i].mapsToURI(uri)) {
				return controllerClasses[i];
			}
		}
		return null;
	}

	public GrailsPageFlowClass getPageFlow(String fullname) {
		return (GrailsPageFlowClass)this.pageFlowMap.get(fullname);
	}
	
	public GrailsPageFlowClass[] getPageFlows() {
		return this.pageFlows;
	}
	
	public GroovyClassLoader getClassLoader() {
		return this.cl;
	}

	public GrailsDomainClass[] getGrailsDomainClasses() {
		return this.domainClasses;
	}

	public GrailsDomainClass getGrailsDomainClass(String name) {
			return (GrailsDomainClass)this.domainMap.get(name);
	}
	

	public GrailsDataSource getGrailsDataSource() {
		return this.dataSource;
	}
	
	public GrailsServiceClass[] getGrailsServiceClasses() {
		return this.services;
	}
	
	public GrailsServiceClass getGrailsServiceClass(String name) {
		return (GrailsServiceClass)this.serviceMap.get(name);
	}
	
	public Class[] getAllClasses() {
		return this.allClasses;
	}

	public GrailsBootstrapClass[] getGrailsBootstrapClasses() {		// 
		return this.bootstrapClasses;
	}
	
}
