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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.springframework.web.servlet.ModelAndView;
/**
 * An interface for a helper class that processes Grails controller requests and responses 
 * 
 * @author Graeme Rocher
 * @since Oct 27, 2005
 */
public interface GrailsControllerHelper {

	/**
	 * Retrieves a controller class for the specified class name
	 * @param name
	 * @return
	 */
	public abstract GrailsControllerClass getControllerClassByName(String className);

	/**
	 * Retrieves a GrailsControllerClass by URI
	 * 
	 * @param uri The URI to lookup
	 * @return A GrailsControllerClass 
	 */
	public abstract GrailsControllerClass getControllerClassByURI(String uri);
	/**
	 * Creates a new controller instance for the specified GrailsControllerClass
	 * @param controllerClass The GrailsControllerClass
	 * @return A new controller instance
	 */
	public abstract GroovyObject getControllerInstance(
			GrailsControllerClass controllerClass);
	/**
	 * Handles a Grails URI
	 * @param uri The URI to processs
	 * @param request The request instance
	 * @param response The response instance
	 * @return A ModelAndView instance
	 */
	public abstract ModelAndView handleURI(String uri,
			HttpServletRequest request, HttpServletResponse response);

	/**
	 * Handles a Controller action
	 * 
	 * @param action An action Closure instance
	 * @param request The request object
	 * @param response The response
	 * 
	 * @return
	 */
	public abstract Object handleAction(GroovyObject controller,Closure action,HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * Handles a Controller action
	 * 
	 * @param action An action Closure instance
	 * @param request The request object
	 * @param response The response
	 * @param params A Map of controller parameters
	 * 
	 * @return
	 */
	public abstract Object handleAction(GroovyObject controller,Closure action,HttpServletRequest request, HttpServletResponse response, Map params);
	
	/**
	 * Processes an action response for the specified arguments
	 * 
	 * @param controllerClass The controller class of the closure
	 * @param returnValue The response from the closure
	 * @param closurePropertyName The property name of the closure
	 * @param viewName The name of the view
	 * 
	 * @return A ModelAndView object
	 */
	public abstract ModelAndView handleActionResponse(
			GrailsControllerClass controllerClass, Object returnValue,
			String closurePropertyName, String viewName);

	/**
	 * Handles a Grails URI
	 * @param uri The URI to processs
	 * @param request The request instance
	 * @param response The response instance
	 * @param params A map of controller parameters
	 * @return A ModelAndView instance
	 */
	public abstract ModelAndView handleURI(String uri,
			HttpServletRequest request, HttpServletResponse response, Map params);

	/**
	 * Sets the chain model on the helper
	 * @param model
	 */
	public abstract void setChainModel(Map model);

}