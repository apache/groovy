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

import groovy.lang.GroovyClassLoader;

import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SimpleGrailsControllerTests extends TestCase {

	public SimpleGrailsControllerTests() {
		super();
	}

	protected GrailsApplication grailsApplication = null;
	protected SimpleGrailsController controller = null;
	
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		
		Class c1 = cl.parseClass("class TestController {\n"+
							"@Property Closure test = {\n"+
								"return [ \"test\" : \"123\" ]\n"+
						     "}\n" +
						"}");	
		
		Class c2 = cl.parseClass("class SimpleController {\n"+
				"@Property Closure test = {\n"+
			     "}\n" +
			"}");
		
		Class c3 = cl.parseClass("class NoViewController {\n"+
				"@Property Closure test = {\n"+
			      "request, response ->\n" +
			      "new grails.util.OpenRicoBuilder(response).ajax { element(id:\"test\") { } };\n" +
			      "return null;\n" +				
			     "}\n" +
			"}");		
		
		this.grailsApplication = new DefaultGrailsApplication(new Class[]{c1,c2,c3},cl);
		this.controller = new SimpleGrailsController();
		this.controller.setGrailsApplication(grailsApplication);
		super.setUp();
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
	
	
	public void testSimpleControllerSuccess() throws Exception {
		ModelAndView modelAndView = execute("/simple", null);
		assertNotNull(modelAndView);
	}
		
}
