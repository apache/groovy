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
package org.codehaus.groovy.grails.web.servlet;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springmodules.beans.factory.drivers.xml.XmlWebApplicationContextDriver;

/**
 * <p>Servlet that handles incoming requests for Grails.
 * <p/>
 * <p>This servlet loads the Spring configuration based on the Grails application
 * in the parent application context.
 *
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class GrailsDispatcherServlet extends DispatcherServlet {

    private static final String GRAILS_APPLICATION_ID = "grailsApplication";
    private static final String GRAILS_APPLICATION_CONTEXT = "grailsApplicationContext";

    public GrailsDispatcherServlet() {
        super();
    }

    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) throws BeansException {
        // use config file locations if available
    	ApplicationContext grailsContext = (ApplicationContext)getServletContext().getAttribute(GRAILS_APPLICATION_CONTEXT );
    	GrailsApplication application;
    	WebApplicationContext webContext;
    	
    	if(grailsContext != null) {
    		XmlWebApplicationContext xmlContext = new XmlWebApplicationContext();
    		xmlContext.setParent(grailsContext);
    		webContext = xmlContext;
    		application = (GrailsApplication) webContext.getBean(GRAILS_APPLICATION_ID, GrailsApplication.class);
    		 
    	}
    	else {
	        String[] locations = null;
	        if (null != getContextConfigLocation()) {
	            locations = StringUtils.tokenizeToStringArray(
	                    getContextConfigLocation(),
	                    ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS);
	        }
	        // construct the SpringConfig for the container managed application
	        application = (GrailsApplication) parent.getBean(GRAILS_APPLICATION_ID, GrailsApplication.class);
	        SpringConfig springConfig = new SpringConfig(application);
	        // return a context that obeys grails' settings
	        webContext = new XmlWebApplicationContextDriver().getWebApplicationContext(
	                springConfig.getBeanReferences(),
	                parent,
	                getServletContext(),
	                getNamespace(),
	                locations);
    	}
    	
    	// init the Grails application
    	GrailsBootstrapClass[] bootstraps =  application.getGrailsBootstrapClasses();
    	for (int i = 0; i < bootstraps.length; i++) {
			bootstraps[i].callInit(  getServletContext() );
		}
    	
    	return webContext;
    }

	public void destroy() {
		WebApplicationContext webContext = getWebApplicationContext();
		GrailsApplication application = (GrailsApplication) webContext.getBean(GRAILS_APPLICATION_ID, GrailsApplication.class);
		
    	GrailsBootstrapClass[] bootstraps =  application.getGrailsBootstrapClasses();
    	for (int i = 0; i < bootstraps.length; i++) {
			bootstraps[i].callDestroy();
		}		
    	// call super
		super.destroy();    	
	}

}
