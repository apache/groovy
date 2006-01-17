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
package org.codehaus.groovy.grails.web.servlet.mvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

/**
 * <p>Base class for Grails controllers.
 *
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SimpleGrailsController implements Controller, ServletContextAware, ApplicationContextAware {


    private UrlPathHelper urlPathHelper = new UrlPathHelper();
    private GrailsApplication application = null;
    private ApplicationContext applicationContext = null;
    private ServletContext servletContext;
    private GrailsControllerHelper helper;

    private static final Log LOG = LogFactory.getLog(SimpleGrailsController.class);


    public SimpleGrailsController() {
        super();
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setGrailsApplication(GrailsApplication application) {
        this.application = application;
    }

    /**
     * <p>This method wraps regular request and response objects into Grails request and response objects.
     *
     * <p>It can handle maps as model types next to ModelAndView instances.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @return the model
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        // Step 1: determine the correct URI of the request.
        String uri = this.urlPathHelper.getPathWithinApplication(request);
        if(LOG.isDebugEnabled()) {
            LOG.debug("[SimpleGrailsController] Processing request for uri ["+uri+"]");
        }


        this.helper = new SimpleGrailsControllerHelper(this.application,this.applicationContext,this.servletContext);

        return helper.handleURI(uri,request,response);
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }
}
