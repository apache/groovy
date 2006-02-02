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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.exceptions.MoreThanOneActiveDataSourceException;
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Steven Devijver
 * @author Graeme Rocher
 *
 * @since Jul 2, 2005
 */
public class DefaultGrailsApplication implements GrailsApplication {

    private static Pattern GRAILS_RESOURCE_PATTERN = Pattern.compile(".+\\\\grails-app\\\\\\w+\\\\(.+)\\.groovy");
    private GroovyClassLoader cl = null;
    private GrailsControllerClass[] controllerClasses = null;
    private GrailsPageFlowClass[] pageFlows = null;
    private GrailsDomainClass[] domainClasses = null;
    private GrailsDataSource dataSource = null;
    private GrailsServiceClass[] services = null;
    private GrailsBootstrapClass[] bootstrapClasses = null;
    private GrailsTagLibClass[] taglibClasses = null;

    private Map controllerMap = null;
    private Map domainMap = null;
    private Map pageFlowMap = null;
    private Map serviceMap = null;
    private Map bootstrapMap = null;
    private Map taglibMap = null;

    private Class[] allClasses = null;


    private static Log log = LogFactory.getLog(DefaultGrailsApplication.class);
    private Map controller2TagMap;


    public DefaultGrailsApplication(final Class[] classes, GroovyClassLoader classLoader) {
        if(classes == null)
            throw new IllegalArgumentException("Constructor argument 'classes' cannot be null");

        configureLoadedClasses(classes);
        this.cl = classLoader;
    }
    public DefaultGrailsApplication(final Resource[] resources) throws IOException {
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
                        Matcher m = GRAILS_RESOURCE_PATTERN.matcher(resources[i].getFile().getAbsolutePath());
                        if(m.find()) {
                            cl.loadClass(m.group(1),true,false);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new org.codehaus.groovy.grails.exceptions.CompilationFailedException("Compilation error parsing file ["+resources[i].getFilename()+"]: " + e.getMessage(), e);
                    }
                }
            }

        // get all the classes that were loaded
        if(log.isDebugEnabled())
            log.debug( "loaded classes: ["+ArrayUtils.toString(this.cl.getLoadedClasses())+"]" );

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
        this.taglibMap = new HashMap();
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
            else if(GrailsClassUtils.isTagLibClass(classes[i])) {
                GrailsTagLibClass grailsTagLibClass = new DefaultGrailsTagLibClass(classes[i]);
                this.taglibMap.put(grailsTagLibClass.getFullName(),grailsTagLibClass);
            }
        }

        this.controllerClasses = ((GrailsControllerClass[])controllerMap.values().toArray(new GrailsControllerClass[controllerMap.size()]));
        this.pageFlows = ((GrailsPageFlowClass[])pageFlowMap.values().toArray(new GrailsPageFlowClass[pageFlowMap.size()]));
        this.domainClasses = ((GrailsDomainClass[])this.domainMap.values().toArray(new GrailsDomainClass[domainMap.size()]));
        this.services = ((GrailsServiceClass[])this.serviceMap.values().toArray(new GrailsServiceClass[serviceMap.size()]));
        this.bootstrapClasses = ((GrailsBootstrapClass[])this.bootstrapMap.values().toArray(new GrailsBootstrapClass[bootstrapMap.size()]));
        this.taglibClasses = ((GrailsTagLibClass[])this.taglibMap.values().toArray(new GrailsTagLibClass[taglibMap.size()]));

        configureDomainClassRelationships();
        configureTagLibraries();
    }

    public GrailsControllerClass addControllerClass(Class controllerClass) {
        if (Modifier.isAbstract(controllerClass.getModifiers())) {
            return null;
        }
        if (GrailsClassUtils.isControllerClass(controllerClass)) {
            GrailsControllerClass grailsControllerClass = new DefaultGrailsControllerClass(controllerClass);
            if (grailsControllerClass.getAvailable()) {
                this.controllerMap.put(grailsControllerClass.getFullName(), grailsControllerClass);
            }

            // reset controller list
            this.controllerClasses = ((GrailsControllerClass[])controllerMap.values().toArray(new GrailsControllerClass[controllerMap.size()]));
            return grailsControllerClass;
        }
        else {
            throw new GrailsConfigurationException("Cannot load controller class ["+controllerClass+"]. It is not a controller!");
        }
    }

    public GrailsTagLibClass addTagLibClass(Class tagLibClass) {
        if (Modifier.isAbstract(tagLibClass.getModifiers())) {
            return null;
        }
        if (GrailsClassUtils.isTagLibClass(tagLibClass)) {
            GrailsTagLibClass grailsTagLibClass = new DefaultGrailsTagLibClass(tagLibClass);
            if (grailsTagLibClass.getAvailable()) {
                this.taglibMap.put(grailsTagLibClass.getFullName(), grailsTagLibClass);
            }

            // reset taglib list
            this.taglibClasses = ((GrailsTagLibClass[])this.taglibMap.values().toArray(new GrailsTagLibClass[taglibMap.size()]));
            // reconfigure controller mappings
            configureTagLibraries();
            
            return grailsTagLibClass;
        }
        else {
            throw new GrailsConfigurationException("Cannot load taglib class ["+tagLibClass+"]. It is not a taglib!");
        }
    }

    public GrailsServiceClass addServiceClass(Class serviceClass) {
        if (Modifier.isAbstract(serviceClass.getModifiers())) {
            return null;
        }
        if (GrailsClassUtils.isService(serviceClass)) {
            GrailsServiceClass grailsServiceClass = new DefaultGrailsServiceClass(serviceClass);
            if (grailsServiceClass.getAvailable()) {
                this.serviceMap.put(grailsServiceClass.getFullName(), grailsServiceClass);
            }

            // reset services list
            this.services = ((GrailsServiceClass[])this.serviceMap.values().toArray(new GrailsServiceClass[serviceMap.size()]));
            return grailsServiceClass;
        }
        else {
            throw new GrailsConfigurationException("Cannot load service class ["+serviceClass+"]. It is not a valid service class!");
        }
    }

    public GrailsDomainClass addDomainClass(Class domainClass) {
      if (Modifier.isAbstract(domainClass.getModifiers())) {
            return null;
        }
        if (GrailsClassUtils.isDomainClass(domainClass)) {
            GrailsDomainClass grailsDomainClass = new DefaultGrailsDomainClass(domainClass);

            this.domainMap.put(grailsDomainClass.getFullName(), grailsDomainClass);


            // reset domain class list
            this.domainClasses = ((GrailsDomainClass[])this.domainMap.values().toArray(new GrailsDomainClass[domainMap.size()]));
            // reconfigure relationships
            configureDomainClassRelationships();
            
            return grailsDomainClass;
        }
        else {
            throw new GrailsConfigurationException("Cannot load domain class ["+domainClass+"]. It is not a valid domain class!");
        }
    }

    /**
     * Maps the controller to the appropriate tag library
     */
    private void configureTagLibraries() {
        this.controller2TagMap = new HashMap();
        for (int i = 0; i < controllerClasses.length; i++) {
            GrailsControllerClass controllerClass = controllerClasses[i];
            boolean found = false;
            for (int j = 0; j < taglibClasses.length; j++) {
                GrailsTagLibClass taglibClass = taglibClasses[j];
                if(controllerClass.getName().equals(taglibClass.getName())) {
                    this.controller2TagMap.put(controllerClass.getFullName(),taglibClass);
                    found = true;
                    break;
                }
            }
            if(!found) {
                this.controller2TagMap.put(controllerClass.getFullName(),taglibMap.get(GrailsTagLibClass.APPLICATION_TAG_LIB));
            }
        }
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

    public GrailsTagLibClass[] getGrailsTabLibClasses() {
        return this.taglibClasses;
    }

    public GrailsTagLibClass getGrailsTagLibClass(String tagLibName) {
        return (GrailsTagLibClass)this.taglibMap.get(tagLibName);
    }

    public GrailsTagLibClass getTagLibClassForController(String controllerName) {
        return (GrailsTagLibClass)this.controller2TagMap.get(controllerName);
    }




}
