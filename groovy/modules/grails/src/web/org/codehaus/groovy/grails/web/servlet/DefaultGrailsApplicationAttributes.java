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
package org.codehaus.groovy.grails.web.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.validation.Errors;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import groovy.lang.GroovyObject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
public class DefaultGrailsApplicationAttributes implements GrailsApplicationAttributes {

    private UrlPathHelper urlHelper = new UrlPathHelper();
    private ServletContext context;

    public DefaultGrailsApplicationAttributes(ServletContext context) {
        this.context = context;
    }

    public WebApplicationContext getApplicationContext() {
        return (WebApplicationContext)this.context.getAttribute(APPLICATION_CONTEXT);
    }

    public GroovyObject getController(ServletRequest request) {
        return (GroovyObject)request.getAttribute(CONTROLLER);
    }

    public String getControllerUri(ServletRequest request) {
        GroovyObject controller = getController(request);

        return (String)controller.getProperty(ControllerDynamicMethods.CONTROLLER_URI_PROPERTY);
    }

    public String getApplicationUri(ServletRequest request) {
        return this.urlHelper.getContextPath((HttpServletRequest)request);
    }

    public String getControllerActionUri(ServletRequest request) {
        GroovyObject controller = getController(request);

        return (String)controller.getProperty(ControllerDynamicMethods.ACTION_URI_PROPERTY);
    }

    public GroovyObject getTagLib(ServletRequest request) {
        return (GroovyObject)request.getAttribute(TAG_LIB);
    }

    public Errors getErrors(ServletRequest request) {
        return (Errors)request.getAttribute(ERRORS);
    }

    public GroovyPagesTemplateEngine getPagesTemplateEngine() {
       return (GroovyPagesTemplateEngine)this.context.getAttribute(GSP_TEMPLATE_ENGINE);
    }

    public GrailsApplication getGrailsApplication() {
        return (GrailsApplication)getApplicationContext()
                                    .getBean(GrailsApplication.APPLICATION_ID);
    }
}
