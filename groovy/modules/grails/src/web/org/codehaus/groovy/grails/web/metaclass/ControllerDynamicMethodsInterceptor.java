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

import groovy.lang.GroovyObject;

import java.beans.IntrospectionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicGroovyMethodsInterceptor;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
/**
 * Adds dynamic methods and properties for Grails Controllers
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class ControllerDynamicMethodsInterceptor extends
		AbstractDynamicGroovyMethodsInterceptor {

	public ControllerDynamicMethodsInterceptor(GroovyObject controller,GrailsControllerHelper helper,HttpServletRequest request, HttpServletResponse response) throws IntrospectionException {
		super(controller);
			
		addDynamicProperty(new GetParamsDynamicProperty(request,response));
		addDynamicProperty(new GetSessionDynamicProperty(request,response));
		addDynamicMethodInvocation( new RedirectDynamicMethod(helper,request,response) );
		addDynamicMethodInvocation( new ChainDynamicMethod(helper, request, response ) );
	}

}
