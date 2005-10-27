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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * A dynamic property that adds a "params" map to a controller for accessing request and controller parameters
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class GetParamsDynamicProperty extends AbstractDynamicControllerProperty {
	public static final String PROPERTY_NAME = "params";
	private GrailsParameterMap paramsMap;
	
	public GetParamsDynamicProperty(HttpServletRequest request, HttpServletResponse response) {
		super(PROPERTY_NAME,request, response);
		this.paramsMap = new GrailsParameterMap(request);
	}

	public Object get(Object object) {
		return paramsMap;
	}

	public void set(Object object, Object newValue) {
		throw new UnsupportedOperationException("Property '" + PROPERTY_NAME + "' of class '"+object.getClass()+"' is read-only!" ); 
	}	

	public void addParam(String paramName, Object paramValue) {
		this.paramsMap.put( paramName,paramValue );
	}	
	public void addParams(Map params) {
		this.paramsMap.putAll(params);
	}
}
