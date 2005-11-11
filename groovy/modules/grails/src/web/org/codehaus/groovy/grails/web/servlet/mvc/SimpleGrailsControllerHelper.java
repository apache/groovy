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

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.commons.metaclass.PropertyAccessProxyMetaClass;
import org.codehaus.groovy.grails.web.metaclass.ChainDynamicMethod;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.web.metaclass.GetParamsDynamicProperty;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletResponse;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.IncompatibleParameterCountException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoClosurePropertyForURIException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoViewNameDefinedException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnknownControllerException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnsupportedReturnValueException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

public class SimpleGrailsControllerHelper implements GrailsControllerHelper {

	private GrailsApplication application;
	private ApplicationContext applicationContext;
	private Map chainModel = Collections.EMPTY_MAP;

	public SimpleGrailsControllerHelper(GrailsApplication application, ApplicationContext context) {
		super();
		this.application = application;
		this.applicationContext = context;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerClassByName(java.lang.String)
	 */
	public GrailsControllerClass getControllerClassByName(String name) {
		return this.application.getController(name);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerClassByURI(java.lang.String)
	 */
	public GrailsControllerClass getControllerClassByURI(String uri) {
		return this.application.getControllerByURI(uri);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerInstance(org.codehaus.groovy.grails.commons.GrailsControllerClass)
	 */
	public GroovyObject getControllerInstance(GrailsControllerClass controllerClass) {
		return (GroovyObject)this.applicationContext.getBean(controllerClass.getFullName());
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleURI(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleURI(String uri, HttpServletRequest request, HttpServletResponse response) {
		return handleURI(uri,request,response,Collections.EMPTY_MAP);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleActionResponse(org.codehaus.groovy.grails.commons.GrailsControllerClass, java.lang.Object, java.lang.String, java.lang.String)
	 */
	public ModelAndView handleActionResponse( GrailsControllerClass controllerClass,Object returnValue,String closurePropertyName, String viewName) {
		boolean viewNameBlank = (viewName == null || viewName.length() == 0);
		
		if (returnValue == null) {
			if (viewNameBlank) {
				return null;
			} else {
				return new ModelAndView(viewName);
			}
		} else if (returnValue instanceof Map) {
			// remove any Proxy wrappers and set the adaptee as the value
			removeProxiesFromModelObjects((Map)returnValue);
			if (viewNameBlank) {
				throw new NoViewNameDefinedException("Map instance returned by and no view name specified for closure on property [" + closurePropertyName + "] in controller [" + controllerClass.getFullName() + "]!");
			} else {
				Map returnMap = (Map)returnValue;
				if(!this.chainModel.isEmpty()) {
					this.chainModel.putAll( returnMap );
					return new ModelAndView(viewName, this.chainModel);
				}
				else {
					return new ModelAndView(viewName, returnMap);
				}
			}
		} else if (returnValue instanceof ModelAndView) {
			ModelAndView modelAndView = (ModelAndView)returnValue;
			
			// remove any Proxy wrappers and set the adaptee as the value
			Map modelMap = modelAndView.getModel();
			removeProxiesFromModelObjects(modelMap);
			if(!this.chainModel.isEmpty()) {
				this.chainModel.putAll(modelMap);
				modelAndView.addAllObjects(this.chainModel);
			}
			
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

	/**
	 * If in Proxy's are used in the Groovy context, unproxy (is that a word?) them by setting
	 * the adaptee as the value in the map so that they can be used in non-groovy view technologies
	 *   
	 * @param model The model as a map
	 */
	private void removeProxiesFromModelObjects(Map model) {
		
		for (Iterator keyIter = model.keySet().iterator(); keyIter.hasNext();) {
			Object current = keyIter.next();
			Object modelObject = model.get(current);
			if(modelObject instanceof Proxy) {
				model.put( current, ((Proxy)modelObject).getAdaptee() );
			}			
		}
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleURI(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
	 */
	public ModelAndView handleURI(String uri, HttpServletRequest request, HttpServletResponse response, Map params) {
		if(uri == null)
			throw new IllegalArgumentException("Controller URI [" + uri + "] cannot be null!");
		
		// step 1: process the uri
		if (uri.indexOf("?") > -1) {
			uri = uri.substring(0, uri.indexOf("?"));
		}		
		if(!uri.startsWith("/"))
			uri = '/' + uri;
		if(uri.endsWith("/"))
			uri = uri.substring(0,uri.length() - 2);
		
		// Step 2: lookup the controller in the application.
		GrailsControllerClass controllerClass = getControllerClassByURI(uri);
		if (controllerClass == null) {
			throw new UnknownControllerException("No controller found for URI [" + uri + "]!");
		}
		//String controllerName = WordUtils.uncapitalize(controllerClass.getName());
		
		// Step 3: load controller from application context.
		GroovyObject controller = getControllerInstance(controllerClass);
		
		// Step 4: get closure property name for URI.
		String closurePropertyName = controllerClass.getClosurePropertyName(uri);
		if (closurePropertyName == null) {
			throw new NoClosurePropertyForURIException("Could not find closure property for URI [" + uri + "] for controller [" + controllerClass.getFullName() + "]!");
		}
		
		// Step 5: get the view name for this URI.
		String viewName = controllerClass.getViewByURI(uri);
		
		
		// Step 6: get closure from closure property
		Closure closure = (Closure)controller.getProperty(closurePropertyName);
		
		// Step 7: process the action
		Object returnValue = handleAction( controller,closure,request,response,params );

		
		// Step 8: determine return value type and handle accordingly
		return handleActionResponse(controllerClass,returnValue,closurePropertyName,viewName);
	}

	public void setChainModel(Map model) {
		this.chainModel  = model;
	}

	public Object handleAction(GroovyObject controller,Closure action, HttpServletRequest request, HttpServletResponse response) {
		return handleAction(controller,action,request,response,Collections.EMPTY_MAP);
	}

	public Object handleAction(GroovyObject controller,Closure action, HttpServletRequest request, HttpServletResponse response, Map params) {
		// Step 3a: Configure a proxy interceptor for controller dynamic methods for this request
		try {
			ControllerDynamicMethodsInterceptor interceptor;
			if(!controller.getMetaClass().getClass().equals( PropertyAccessProxyMetaClass.class )) {				
				interceptor = new ControllerDynamicMethodsInterceptor(controller,this,request,response);
			}
			else {
				ProxyMetaClass pmc = (ProxyMetaClass)controller.getMetaClass(); 
				interceptor = (ControllerDynamicMethodsInterceptor)pmc.getInterceptor(); 
			}
			// if there are additional params add them to the params dynamic property
			if(params != null && !params.isEmpty()) {
				GetParamsDynamicProperty paramsProp = (GetParamsDynamicProperty)interceptor.getDynamicProperty( GetParamsDynamicProperty.PROPERTY_NAME );
				paramsProp.addParams( params );
			}			
			// check the chain model is not empty and add it
			if(!this.chainModel.isEmpty()) {
				// get the "chainModel" property
				GenericDynamicProperty chainProperty = (GenericDynamicProperty)interceptor.getDynamicProperty(ChainDynamicMethod.PROPERTY_CHAIN_MODEL);
				// if it doesn't exist create it
				if(chainProperty == null) {			
					interceptor.addDynamicProperty( new GenericDynamicProperty( ChainDynamicMethod.PROPERTY_CHAIN_MODEL,Map.class,this.chainModel,false ) );
				}
				else {
					// otherwise add to it
					Map chainPropertyModel = (Map)chainProperty.getValue();
					chainPropertyModel.putAll( this.chainModel );
					this.chainModel = chainPropertyModel;
				}
			}

		}
		catch(IntrospectionException ie) {
			throw new ControllerExecutionException("Error creating dynamic controller methods for controller ["+controller.getClass()+"]: " + ie.getMessage(), ie);
		}
		
		// Step 7: determine argument count and execute.
		Object returnValue = null;
		if (action.getParameterTypes().length == 1) {
			// closure may have zero or one parameter, we cannot be sure.
			returnValue = action.call(new GrailsHttpServletRequest(request));
		} else if (action.getParameterTypes().length == 2) {
			returnValue = action.call(new Object[] { new  GrailsHttpServletRequest(request), new GrailsHttpServletResponse(response) });
		} else {
			throw new IncompatibleParameterCountException("Closure on property [" + action + "] in [" + controller.getClass() + "] has an incompatible parameter count [" + action.getParameterTypes().length + "]! Supported values are 0 and 2.");			
		}
		return returnValue;
	}
	
}
