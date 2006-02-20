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

/**
 *  <p>Exposes all classes for a Grails application.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public interface GrailsApplication {
    /**
     * The id of the grails application within a bean context
     */
    String APPLICATION_ID = "grailsApplication";

    /**
     * <p>Returns all controllers in an application
     *
     * @return controllers in an application
     */
    public GrailsControllerClass[] getControllers();

    /**
     * <p>Returns the controller with the given full name or null if no controller was found with that name.
     *
     * @param fullname the controller full name
     * @return the controller or null if no controller was found.
     */
    public GrailsControllerClass getController(String fullname);

    /**
     * <p>Returns the controllers that maps to the given URI or null if no controller was found with that name.
     *
     * @param uri the uri of the request
     * @return the controller or null if no controller was found
     */
    public GrailsControllerClass getControllerByURI(String uri);

    /**
     * <p>Returns all page flows in an application.
     *
     * @return page flows in an application.
     */
    public GrailsPageFlowClass[] getPageFlows();

    /**
     * <p>Returns the page flow with the given full name or null if no page flow was found with that name.
     *
     * @param fullname the page flow full name
     * @return the page flow or null if no controller was found.
     */
    public GrailsPageFlowClass getPageFlow(String fullname);

    /**
     * <p>Returns an array of all the Grails Domain classes</p>
     *
     * @return The domain classes in the domain
     */
    public GrailsDomainClass[] getGrailsDomainClasses();

    /**
     * Check whether the specified class is a grails domain class
     * @param domainClass The class to check
     * @return True if it is
     */
    public boolean isGrailsDomainClass(Class domainClass);

    /**
     * <p>Retrieves a domain class for the specified name</p>
     *
     * @param name The name of the domain class to retrieve
     * @return The retrieved domain class
     */
    public GrailsDomainClass getGrailsDomainClass(String name);

    /**
     * <p>Returns the active data source for this Grails application or null if not available.
     *
     * @return the active data source or null if not available.
     */
    public GrailsDataSource getGrailsDataSource();

    /**
     * <p>Returns the class loader instance for the Grails application</p>
     *
     * @return The GroovyClassLoader instance
     */
    public GroovyClassLoader getClassLoader();

    /**
     * <p>Returns all service classes for the Grails application.
     *
     * @return service class for Grails application
     */
    public GrailsServiceClass[] getGrailsServiceClasses();

    /**
     * <p>Returns the service with the specified full name.
     *
     * @param name the full name of the service class
     * @return the service class
     */
    public GrailsServiceClass getGrailsServiceClass(String name);

    /**
     * <p>Returns all the bootstrap classes for the Grails application
     *
     * @return An array of BootStrap classes
     */
    public GrailsBootstrapClass[] getGrailsBootstrapClasses();

    /**
     * <p>Returns all the tag lib classes for the Grails application
     *
     * @return An array of TagLib classes
     */
    public GrailsTagLibClass[] getGrailsTabLibClasses();

    /**
     * <p>Returns a tag lib class for the specified name
     *
     * @param tagLibName The name of the taglib class
     * @return A taglib class instance or null if non exists
     */
    public GrailsTagLibClass getGrailsTagLibClass(String tagLibName);

    /**
     * <p>Retrieves the tag lib class for the specified tag
     *
     * @param tagName The name of the tag
     * @return A array of tag lib classes
     */
    public GrailsTagLibClass getTagLibClassForTag(String tagName);

    /**
     * Adds a new Grails controller class to the application
     * @param controllerClass The grails controller class to add
     * @return A GrailsControllerClass instance
     */
    GrailsControllerClass addControllerClass(Class controllerClass);

    /**
     * Adds a new Grails taglib class to the application. If it already exists the old one will be replaced
     * 
     * @param tagLibClass The taglib class to add
     * @return The newly added class
     */
    GrailsTagLibClass addTagLibClass(Class tagLibClass);

    /**
     * Adds a new Grails service class to the application. If it already exists the old one will be replaced
     *
     * @param serviceClass The service class to add
     * @return The newly added class or null if the class is abstract and was not added
     */
    GrailsServiceClass addServiceClass(Class serviceClass);

    /**
     * Adds a new domain class to the grails application
     * @param domainClass The domain class to add
     * @return The GrailsDomainClass instance or null if the class is abstract and was not added
     */
    GrailsDomainClass addDomainClass(Class domainClass);

    /**
     * Adds a new domain class to the grails application
     * @param domainClass The domain class to add
     * @return The GrailsDomainClass instance or null if the class is abstract and was not added
     */
    
    GrailsDomainClass addDomainClass(GrailsDomainClass domainClass);

    /**
     * Retrieves the controller that is scaffolding the specified domain class
     *
     * @param domainClass The domain class to check
     * @return An instance of GrailsControllerClass                                      
     */
    GrailsControllerClass getScaffoldingController(GrailsDomainClass domainClass);


}
