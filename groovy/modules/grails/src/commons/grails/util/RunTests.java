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
package grails.util;

import java.lang.reflect.Modifier;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.codehaus.groovy.grails.support.GrailsTestSuite;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 8, 2005
 */
public class RunTests {

	private static Logger log = Logger.getLogger(RunTests.class);
	
	public static void main(String[] args) {
		ApplicationContext parent = new ClassPathXmlApplicationContext("test-applicationContext.xml");
		DefaultGrailsApplication application = (DefaultGrailsApplication)parent.getBean("grailsApplication", DefaultGrailsApplication.class);
		SpringConfig config = new SpringConfig(application);
		ConfigurableApplicationContext appCtx = (ConfigurableApplicationContext) 
			new XmlApplicationContextDriver().getApplicationContext(
				config.getBeanReferences(), parent);
		
		Class[] allClasses = application.getAllClasses();
		TestSuite s = new TestSuite();
		for (int i = 0; i < allClasses.length; i++) {
			Class clazz = allClasses[i];
			if (TestCase.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
				log.debug("Adding test [" + clazz.getName() + "]");
				s.addTest(new GrailsTestSuite(appCtx.getBeanFactory(), clazz));
			} else {
				log.debug("[" + clazz.getName() + "] is not a test case.");
			}
		}
		TestRunner.run(s);
	}
}
