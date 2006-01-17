package org.codehaus.groovy.grails.web.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.validation.Errors;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import groovy.lang.GroovyObject;

import javax.servlet.ServletRequest;

/**
 * An interface defining the names of and methods to retrieve Grails specific request and servlet attributes
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
public interface GrailsRequestAttributes {

    String GSP_TEMPLATE_ENGINE = "org.codehaus.groovy.grails.GSP_TEMPLATE_ENGINE";
    String APPLICATION_CONTEXT = "org.codehaus.groovy.grails.APPLICATION_CONTEXT";
    String CONTROLLER = "org.codehaus.groovy.grails.CONTROLLER";
    String TAG_LIB = "org.codehaus.groovy.grails.TAG_LIB";
    String ERRORS =  "org.codehaus.groovy.grails.ERRORS";

    /**
     * @return The application context for servlet
     */
    WebApplicationContext getApplicationContext();

    /**
     * @return The controller for the request
     */
    GroovyObject getController(ServletRequest request);

    /**
     * @return The tag library for the request
     */
    GroovyObject getTagLib(ServletRequest request);

    /**
     *
     * @param request
     * @return The errors instance contained within the request
     */
    Errors getErrors(ServletRequest request);

    /**
     *
     * @return Retrieves the shared GSP template engine
     */
    GroovyPagesTemplateEngine getPagesTemplateEngine();
}
