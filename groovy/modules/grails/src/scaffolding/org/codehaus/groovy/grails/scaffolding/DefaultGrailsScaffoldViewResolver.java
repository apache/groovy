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

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
/**
 * A view resolver that uses a GrailsApplication instance to resolve view names from uris
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public class DefaultGrailsScaffoldViewResolver implements ScaffoldViewResolver {

	private GrailsApplication application;

	public DefaultGrailsScaffoldViewResolver(GrailsApplication application) {
		this.application = application;
	}

	public String resolveViewForUri(String uri) {
		GrailsControllerClass controllerClass = application.getControllerByURI(uri);
		
		if(controllerClass != null) {
			return controllerClass.getViewByURI(uri);
		}
		return null;
	}

	public String resolveViewForUriAndAction(String uri, String action) {
		if(StringUtils.isBlank(action)) {
			return resolveViewForUri(uri);
		}
		GrailsControllerClass controllerClass = application.getControllerByURI(uri);
		
		if(controllerClass != null) {
			return controllerClass.getViewByName(action);
		}
		return null;
	}

}
