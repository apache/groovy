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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
/**
 * An interface defining methods to handle responses. Implementors of this interface are responsible 
 * for transforming the model into an appropriate ModelAndView instance or writing directly to the
 * response
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public interface ScaffoldResponseHandler {

	/**
	 * Creates the response for a Scaffolded model and optionally returns a ModelAndView instance
	 * 
	 * @param request 
	 * @param response
	 * @param model
	 * @return
	 */
	ModelAndView handleResponse(HttpServletRequest request,HttpServletResponse response, String actionName, Map model);
	
	/**
	 * The view resolver to use for resovling views for the response
	 * @param resolver
	 */
	void setScaffoldViewResolver(ScaffoldViewResolver resolver);

}
