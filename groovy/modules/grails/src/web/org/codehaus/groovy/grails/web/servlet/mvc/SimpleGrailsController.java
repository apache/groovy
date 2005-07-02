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

import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.IncompatibleParameterCountException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoClosurePropertyForURIException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoViewNameDefinedException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnknownControllerException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnsupportedReturnValueException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
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
		// Step 1: determine the name of the controller.
		// This maps to a slash + the name of the controller.
		String uri = this.urlPathHelper.getLookupPathForRequest(request);
		String[] uriParts = StringUtils.tokenizeToStringArray(uri, SLASH, true, true);
		String controllerName = uriParts[0];
		
		// Step 2: lookup the controller in the application.
		GrailsControllerClass controllerClass = this.application.getController(controllerName);
		if (controllerClass == null) {
			String fullControllerName = controllerName.substring(0, 1).toUpperCase() + controllerName.substring(1) + DefaultGrailsControllerClass.CONTROLLER;
			throw new UnknownControllerException("Controller named [" + controllerName + "] could not be found. Make sure [" + fullControllerName + "] is defined and available!");
		}
		
		// Step 3: load controller from application context.
		GroovyObject controller = (GroovyObject)this.applicationContext.getBean(controllerName + DefaultGrailsControllerClass.CONTROLLER);
		
		// Step 4: get closure property name for URI.
		String closurePropertyName = controllerClass.getClosurePropertyName(uri);
		if (closurePropertyName == null) {
			throw new NoClosurePropertyForURIException("Could not find closure property for URI [" + uri + "] for controller [" + controllerClass.getFullName() + "]!");
		}
		
		// Step 5: get the view name for this closure property.
		String viewName = controllerClass.getViewName(closurePropertyName);
		boolean viewNameBlank = (viewName == null || viewName.length() == 0);
		
		// Step 6: get closure from closure property
		Closure closure = (Closure)controller.getProperty(closurePropertyName);
		
		// Step 7: determine argument count and execute.
		Object returnValue = null;
		if (closure.getParameterTypes().length == 1) {
			// closure may have zero or one parameter, we cannot be sure.
			returnValue = closure.call(new Object[] { new GrailsHttpServletRequest(request) });
		} else if (closure.getParameterTypes().length == 2) {
			returnValue = closure.call(new Object[] { new  GrailsHttpServletRequest(request), response });
		} else {
			throw new IncompatibleParameterCountException("Closure on property [" + closurePropertyName + "] in [" + controllerClass.getFullName() + "] has an incompatible parameter count [" + closure.getParameterTypes().length + "]! Supported values are 0 and 2.");
		}
		
		// Step 8: determine return value type and handle accordingly
		if (returnValue == null) {
			if (viewNameBlank) {
				return null;
			} else {
				return new ModelAndView(viewName);
			}
		} else if (returnValue instanceof Map) {
			if (viewNameBlank) {
				throw new NoViewNameDefinedException("Map instance returned by and view name specified for closure on property [" + closurePropertyName + "] in controller [" + controllerClass.getFullName() + "]!");
			} else {
				return new ModelAndView(viewName, (Map)returnValue);
			}
		} else if (returnValue instanceof ModelAndView) {
			ModelAndView modelAndView = (ModelAndView)returnValue;
			if (modelAndView.getView() == null && modelAndView.getViewName() == null) {
				if (viewNameBlank) {
					throw new NoViewNameDefinedException("ModelAndView instance returned by and no view name defined by nor for closure on property [" + closurePropertyName + "] in controller [" + controllerClass.getFullName() + "]!");
				} else {
					modelAndView.setViewName(viewName);
				}
			}
			return modelAndView;
		}
		
		throw new UnsupportedReturnValueException("Return value [" + returnValue + "] is not supported for closure property [" + closurePropertyName + "] in controller [" + controllerClass.getFullName() + "]!");
	}

}
