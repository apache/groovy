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

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * <p>Class that handles URL mapping for Grails
 * 
 * @author Graeme Rocher
 * @since Dec 15, 2005
 */
public class GrailsUrlHandlerMapping extends SimpleUrlHandlerMapping {

	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#getHandlerInternal(javax.servlet.http.HttpServletRequest)
	 */
	protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
		String appPath = urlPathHelper.getPathWithinApplication(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up handler for [" + appPath + "]");
		}
		return lookupHandler(appPath);		
	}
}
