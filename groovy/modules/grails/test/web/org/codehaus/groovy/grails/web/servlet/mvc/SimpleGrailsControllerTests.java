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
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;

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
	private GenericApplicationContext localContext;
	private ConfigurableApplicationContext appCtx;
	
	
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
		
//		this.grailsApplication = new DefaultGrailsApplication(new Class[]{c1,c2,c3},cl);
//		this.controller = new SimpleGrailsController();
//		this.controller.setGrailsApplication(grailsApplication);
		
		Thread.currentThread().setContextClassLoader(cl);
		
		//grailsApplication = new DefaultGrailsApplication(,cl);
		this.localContext = new GenericApplicationContext();
		
		ConstructorArgumentValues args = new ConstructorArgumentValues();
		args.addGenericArgumentValue(new Class[]{c1,c2,c3});
		args.addGenericArgumentValue(cl);
		MutablePropertyValues propValues = new MutablePropertyValues();
		
		BeanDefinition grailsApplicationBean = new RootBeanDefinition(DefaultGrailsApplication.class,args,propValues);		
		localContext.registerBeanDefinition( "grailsApplication", grailsApplicationBean );
		this.localContext.refresh();
		
		this.grailsApplication = (GrailsApplication)localContext.getBean("grailsApplication");
		
		/*BeanDefinition applicationEventMulticaster = new RootBeanDefinition(SimpleApplicationEventMulticaster.class);
		context.registerBeanDefinition( "applicationEventMulticaster ", applicationEventMulticaster);*/
		SpringConfig springConfig = new SpringConfig(grailsApplication);
		this.appCtx = (ConfigurableApplicationContext) 
		new XmlApplicationContextDriver().getApplicationContext(
				springConfig.getBeanReferences(), this.localContext);
		
		this.controller = (SimpleGrailsController)appCtx.getBean("simpleGrailsController");
		
		
		assertNotNull(appCtx);		
		super.setUp();
	}


	private ModelAndView execute(String uri, Properties parameters) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
		request.setContextPath("/simple");
		
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
		ModelAndView modelAndView = execute("/test", null);
		assertNotNull(modelAndView);
	}
		
}
