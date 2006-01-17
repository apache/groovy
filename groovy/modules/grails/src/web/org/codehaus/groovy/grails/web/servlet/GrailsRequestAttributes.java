package org.codehaus.groovy.grails.web.servlet;

import org.springframework.web.context.WebApplicationContext;
import groovy.lang.GroovyObject;

/**
 * An interface defining the names of and methods to retrieve Grails specific request attributes
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
public interface GrailsRequestAttributes {

    String APPLICATION_CONTEXT = "org.codehaus.groovy.grails.APPLICATION_CONTEXT";
    String CONTROLLER = "org.codehaus.groovy.grails.CONTROLLER";
    String TAG_LIB = "org.codehaus.groovy.grails.TAG_LIB";

    /**
     * @return The application context for servlet
     */
    WebApplicationContext getApplicationContext();

    /**
     * @return The controller for the request
     */
    GroovyObject getController();

    /**
     * @return The tag library for the request
     */
    GroovyObject getTagLib();
}
