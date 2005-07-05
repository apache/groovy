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

import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoViewNameDefinedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SimpleGrailsControllerTests extends AbstractDependencyInjectionSpringContextTests {

	public SimpleGrailsControllerTests() {
		super();
		setPopulateProtectedVariables(true);
	}

	protected GrailsApplication grailsApplication = null;
	protected SimpleGrailsController controller = null;
	
	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/web/servlet/mvc/simple-grails-controller-tests.xml" };
	}
	
	private ModelAndView execute(String uri, Properties parameters) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
		if (parameters != null) {
			for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) {
				String paramName = (String)iter.next();
				String paramValue = parameters.getProperty(paramName);
				request.addParameter(paramName, paramValue);
			}
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		return controller.handleRequest(request, response);
	}
	
	public void testTestControllerFail() throws Exception {
		try {
			execute("/test", null);
			fail();
		} catch (NoViewNameDefinedException e) {
			// expected
		}
	}
	
	public void testSimpleControllerSuccess() throws Exception {
		ModelAndView modelAndView = execute("/simple", null);
		assertNull(modelAndView);
	}
	
	public void testReturnModelAndViewControllerWithView() throws Exception {
		ModelAndView modelAndView = execute("/org/codehaus/groovy/grails/web/servlet/mvc/returnModelAndView/withView", null);
		assertNotNull(modelAndView);
	}
	
	public void testReturnModelAndViewControlerWithoutView() throws Exception {
		try {
			execute("/org/codehaus/groovy/grails/web/servlet/mvc/returnModelAndView/withoutView", null);
			fail();
		} catch (NoViewNameDefinedException e) {
			// expected
		}
	}
	
	public void testReturnModelAndViewControllerViewConfigured() throws Exception {
		ModelAndView modelAndView = execute("/org/codehaus/groovy/grails/web/servlet/mvc/returnModelAndView/viewConfigured", null);
		assertNotNull(modelAndView);
		assertEquals("someOtherView", modelAndView.getViewName());
	}
	
	public void testReturnModelAndViewControllerDefaultClosure() throws Exception {
		ModelAndView modelAndView = execute("/org/codehaus/groovy/grails/web/servlet/mvc/returnModelAndView?test1=test2&test3=test4", null);
		assertNotNull(modelAndView);
		assertEquals("someView", modelAndView.getViewName());
	}
	
	public void testParameterControllerTwoParameters() throws Exception {
		ModelAndView modelAndView = execute("/parameter", null);
		assertNotNull(modelAndView);
		assertEquals("someView", modelAndView.getViewName());
		assertNotNull(modelAndView.getModel().get("request"));
		assertTrue(modelAndView.getModel().get("request") instanceof HttpServletRequest);
		assertNotNull(modelAndView.getModel().get("response"));
		assertTrue(modelAndView.getModel().get("response") instanceof HttpServletResponse);
	}

	public void testParameterControllerTwoParametersRss() throws Exception {
		ModelAndView modelAndView = execute("/parameter/rss", null);
		assertNotNull(modelAndView);
		assertEquals("someRssView", modelAndView.getViewName());
		assertNotNull(modelAndView.getModel().get("request"));
		assertTrue(modelAndView.getModel().get("request") instanceof HttpServletRequest);
		assertNotNull(modelAndView.getModel().get("response"));
		assertTrue(modelAndView.getModel().get("response") instanceof HttpServletResponse);
	}

	
	public void testParameterControllerOneParameter() throws Exception {
		ModelAndView modelAndView = execute("/parameter/oneParameter", null);
		assertNotNull(modelAndView);
		assertEquals("someOtherView", modelAndView.getViewName());
		assertNotNull(modelAndView.getModel().get("request"));
		assertTrue(modelAndView.getModel().get("request") instanceof HttpServletRequest);
	}
	
	public void testParameterControllerOneParameterRss() throws Exception {
		ModelAndView modelAndView = execute("/parameter/oneParameter/rss", null);
		assertNotNull(modelAndView);
		assertEquals("someOtherRssView", modelAndView.getViewName());
		assertNotNull(modelAndView.getModel().get("request"));
		assertTrue(modelAndView.getModel().get("request") instanceof HttpServletRequest);
	}

}
