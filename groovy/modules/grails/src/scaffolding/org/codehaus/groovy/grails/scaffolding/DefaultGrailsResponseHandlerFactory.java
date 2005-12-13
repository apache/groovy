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
package org.codehaus.groovy.grails.scaffolding;

import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * The default implementation of a ScaffoldResponseHandlerFactory that uses the uri extension to look-up
 * an appropriate response handler. If none exists for the uri suffix it returns the default response handler
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */

public class DefaultGrailsResponseHandlerFactory implements
		ScaffoldResponseHandlerFactory {

	private GrailsApplication application;
	private ScaffoldResponseHandler defaultResponseHandler;
	
	
	public DefaultGrailsResponseHandlerFactory(GrailsApplication application, ScaffoldResponseHandler defaultResponseHandler) {
		super();
		if(defaultResponseHandler == null)
			throw new IllegalStateException("Argument 'defaultResponseHandler' is required");
		if(application == null)
			throw new IllegalStateException("Argument 'application' is required");		
		
		this.application = application;
		this.defaultResponseHandler = defaultResponseHandler;
	}

	public ScaffoldResponseHandler getScaffoldResponseHandler(String uri) {
		// for now just return the default
		return defaultResponseHandler;
	}
}
