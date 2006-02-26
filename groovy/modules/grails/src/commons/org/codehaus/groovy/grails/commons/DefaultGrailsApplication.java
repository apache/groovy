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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException;
import org.codehaus.groovy.grails.exceptions.MoreThanOneActiveDataSourceException;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfigurationUtil;
import org.codehaus.groovy.grails.commons.spring.GrailsResourceHolder;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of the GrailsApplication interface that manages application loading,
 * state, and artifact instances.
 * 
 * @author Steven Devijver
 * @author Graeme Rocher
 *
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
    private GrailsTagLibClass[] taglibClasses = null;

    private Map controllerMap = null;
    private Map domainMap = null;
    private Map pageFlowMap = null;
    private Map serviceMap = null;
    private Map bootstrapMap = null;
    private Map taglibMap = null;

    private Class[] allClasses = null;


    private static Log log = LogFactory.getLog(DefaultGrailsApplication.class);
    private Map tag2libMap;


    public DefaultGrailsApplication(final Class[] classes, GroovyClassLoader classLoader) {
        if(classes == null)
            throw new IllegalArgumentException("Constructor argument 'classes' cannot be null");

        configureLoadedClasses(classes);
        this.cl = classLoader;
    }
    public DefaultGrailsApplication(final Resource[] resources) throws IOException {
        super();

        log.debug("Loading Grails application.");

        GrailsResourceLoader resourceLoader = new GrailsResourceLoader(resources);
        GrailsResourceHolder resourceHolder = new GrailsResourceHolder();

        this.cl = new GroovyClassLoader();
        this.cl.setResourceLoader(resourceLoader);
           Collection loadedResources = new ArrayList();

            for (int i = 0; resources != null && i < resources.length; i++) {
                log.debug("Loading groovy file :[" + resources[i].getFile().getAbsolutePath() + "]");
                if (!loadedResources.contains(resources[i])) {
                    try {
                        String className = resourceHolder.getClassName(resources[i]);
                        if(!StringUtils.isBlank(className)) {
                            cl.loadClass(className,true,false);
                            loadedResources = resourceLoader.getLoadedResources();
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

    public GrailsDomainClass addDomainClass(GrailsDomainClass domainClass) {
        if(domainClass != null) {
            this.domainMap.put(domainClass.getFullName(),domainClass);

            // reset domain class list
            this.domainClasses = ((GrailsDomainClass[])this.domainMap.values().toArray(new GrailsDomainClass[domainMap.size()]));
           // if(!(domainClass instanceof ExternalGrailsDomainClass)) {
                // reconfigure relationships
                configureDomainClassRelationships();
           // }
        }
        return domainClass;
    }

    public GrailsControllerClass getScaffoldingController(GrailsDomainClass domainClass) {
        if(domainClass == null)
            return null;

        for (int i = 0; i < controllerClasses.length; i++) {
            GrailsControllerClass controllerClass = controllerClasses[i];
            if(controllerClass.isScaffolding()) {
                Class scaffoldedClass = controllerClass.getScaffoldedClass();
                if(scaffoldedClass == null && domainClass.getName().equals(controllerClass.getName())) {
                    return controllerClass;
                }
                else if(domainClass.getClazz().equals(scaffoldedClass)) {
                    return controllerClass;
                }

            }
        }
        return null;
    }

    /**
     * Creates a map of tags to tag libraries
     */
    private void configureTagLibraries() {
        this.tag2libMap = new HashMap();
        for (int i = 0; i < taglibClasses.length; i++) {
            GrailsTagLibClass taglibClass = taglibClasses[i];
            for (Iterator j = taglibClass.getTagNames().iterator(); j.hasNext();) {
                String tagName = (String) j.next();
                if(!this.taglibMap.containsKey(tagName)) {
                    this.tag2libMap.put(tagName,taglibClass);
                }
                else {
                    GrailsTagLibClass current = (GrailsTagLibClass)this.taglibMap.get(tagName);
                    if(!taglibClass.equals(current)) {
                        this.tag2libMap.put(tagName,taglibClass);
                    }
                    else {
                        throw new GrailsConfigurationException("Cannot configure tag library ["+taglibClass.getName()+"]. Library ["+current.getName()+"] already contains a tag called ["+tagName+"]");
                    }
                }
            }
        }
    }

    /**
     * Sets up the relationships between the domain classes, this has to be done after
     * the intial creation to avoid looping
     *
     */
    private void configureDomainClassRelationships() {
       GrailsDomainConfigurationUtil.configureDomainClassRelationships(this.domainClasses,this.domainMap);
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

    public boolean isGrailsDomainClass(Class domainClass) {
        if(domainClass == null)
            return false;

        if(domainMap.containsKey(domainClass.getName())) {
            return true;
        }
        return false;  
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

    public GrailsTagLibClass getTagLibClassForTag(String tagName) {
        return (GrailsTagLibClass)this.tag2libMap.get(tagName);
    }




}
