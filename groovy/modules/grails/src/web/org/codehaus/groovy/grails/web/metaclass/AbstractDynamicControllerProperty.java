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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicProperty;
/**
 * An abstract class for dynamic controller properties to implement
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public abstract class AbstractDynamicControllerProperty
	extends AbstractDynamicProperty
	{

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public AbstractDynamicControllerProperty(String propertyName,HttpServletRequest request, HttpServletResponse response) {
		super(propertyName);
		this.request = request;
		this.response = response;
	}

	public abstract Object get(Object object);

}
