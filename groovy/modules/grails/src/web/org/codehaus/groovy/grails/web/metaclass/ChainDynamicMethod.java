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
package org.codehaus.groovy.grails.web.metaclass;

import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.ProxyMetaClass;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletResponse;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.IncompatibleParameterCountException;

/**
 * Implements the "chain" Controller method for action chaining
 * 
 * @author Graeme Rocher
 * @since Oct 27, 2005
 */
public class ChainDynamicMethod extends AbstractDynamicControllerMethod {

	private static final String METHOD_SIGNATURE = "chain";
	private static final String ARGUMENT_ACTION = "action";
	private static final String ARGUMENT_PARAMS = "params";
	private static final Object ARGUMENT_MODEL = "model";
	
	static public final String PROPERTY_CHAIN_MODEL = "chainModel";
	
	private GrailsControllerHelper helper;

	public ChainDynamicMethod(GrailsControllerHelper helper, HttpServletRequest request, HttpServletResponse response) {
		super(METHOD_SIGNATURE, request, response);
		if(helper == null)
			throw new IllegalStateException("Constructor argument 'helper' cannot be null");
		
		this.helper = helper;
	}

	public Object invoke(Object target, Object[] arguments) {
		if(arguments.length == 0)
			throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
				
		Object actionRef;
		Map params = null;
		Map model = null;
		GroovyObject targetGo = (GroovyObject)target;
		
		if(arguments[0] instanceof Map) {
			Map argMap = (Map)arguments[0];
			actionRef = argMap.get(ARGUMENT_ACTION);
			if(argMap.get(ARGUMENT_PARAMS) instanceof Map)
				params = (Map)argMap.get(ARGUMENT_PARAMS);
			if(argMap.get(ARGUMENT_MODEL) instanceof Map)
				model = (Map)argMap.get(ARGUMENT_MODEL);
			else
				throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
		}
		else {
			actionRef = arguments[0];
			if(!(arguments[1] instanceof Map))
				throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
			
			model = (Map)arguments[1];
			if(arguments.length > 2) {
				if(arguments[2] instanceof Map) {
					params = (Map)arguments[2];
				}
			}			
		}		

		if(actionRef instanceof String) {
			String uri = (String)actionRef;
			if(params != null ) {
				helper.setChainModel(model);
				return helper.handleURI(uri,this.request,this.response, params);
			}
			else {
				helper.setChainModel(model);
				return helper.handleURI(uri,this.request,this.response);
			}
		}
		else if(actionRef instanceof Closure) {
			Closure c = (Closure)actionRef;
			String closureName = GrailsClassUtils.getPropertyDescriptorForValue(target,c).getName();
			GrailsControllerClass controllerClass = helper.getControllerClassByName( target.getClass().getName() );
			String viewName  = controllerClass.getViewByName(closureName);
			
			// add additional params to params dynamic property
			
			ProxyMetaClass metaClass =  (ProxyMetaClass)targetGo.getMetaClass();
			ControllerDynamicMethodsInterceptor interceptor = (ControllerDynamicMethodsInterceptor)metaClass.getInterceptor();
			// get the "chainModel" property
			GenericDynamicProperty chainProperty = (GenericDynamicProperty)interceptor.getDynamicProperty(PROPERTY_CHAIN_MODEL);
			// if it doesn't exist create it
			if(chainProperty == null) {			
				interceptor.addDynamicProperty( new GenericDynamicProperty( PROPERTY_CHAIN_MODEL,Map.class,model,false ) );
			}
			else {
				// otherwise add to it
				Map chainModel = (Map)chainProperty.getValue();
				chainModel.putAll( model );
				model = chainModel;
			}
				
			if(params != null) {				
				GetParamsDynamicProperty paramsProp = (GetParamsDynamicProperty)interceptor.getDynamicProperty(GetParamsDynamicProperty.PROPERTY_NAME);
				paramsProp.addParams(params);
			}
			
			Object returnValue;
			if (c.getParameterTypes().length == 1) {
				// closure may have zero or one parameter, we cannot be sure.
				returnValue = c.call(new GrailsHttpServletRequest(request));
			} else if (c.getParameterTypes().length == 2) {
				returnValue = c.call(new Object[] { new  GrailsHttpServletRequest(request), new GrailsHttpServletResponse(response) });
			} else {
				throw new IncompatibleParameterCountException("Closure on property [" + c + "] in [" + target.getClass() + "] has an incompatible parameter count [" + c.getParameterTypes().length + "]! Supported values are 0 to 2.");			
			}
			helper.setChainModel(model);
			return helper.handleActionResponse(controllerClass,returnValue,closureName,viewName);
		}

		throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);			
	}

}
