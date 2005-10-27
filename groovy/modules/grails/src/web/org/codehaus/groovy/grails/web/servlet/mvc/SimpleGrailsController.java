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

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.ProxyMetaClass;
import groovy.util.Proxy;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.metaclass.PropertyAccessProxyMetaClass;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletResponse;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.IncompatibleParameterCountException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoClosurePropertyForURIException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoViewNameDefinedException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnknownControllerException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnsupportedReturnValueException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

/**
 * <p>Base class for Grails controllers.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SimpleGrailsController implements Controller, ApplicationContextAware {

	private static final String SLASH = "/";
	
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	private GrailsApplication application = null;
	private ApplicationContext applicationContext = null;
	
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
		String uri = this.urlPathHelper.getLookupPathForRequest(request);
		
		GrailsControllerHelper helper = new SimpleGrailsControllerHelper(this.application,this.applicationContext);
		return helper.handleURI(uri,request,response);
	}

}
