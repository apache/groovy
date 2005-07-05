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
package org.codehaus.groovy.grails.commons;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class DefaultGrailsControllerClassTests extends TestCase {

	public DefaultGrailsControllerClassTests() {
		super();
	}

	public DefaultGrailsControllerClassTests(String arg0) {
		super(arg0);
	}
	
	public void testDefaultGrailsControllerClassURIs() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Class clazz = cl.parseClass("class OverviewController { }");
		GrailsControllerClass grailsClass = new DefaultGrailsControllerClass(clazz);
		assertEquals(0, grailsClass.getURIs().length);
	}
	
	public void testDefaultGrailsControllerViewNames() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Class clazz = cl.parseClass("class OverviewController { @Property String listView = \"listPage\"; @Property Closure list = { request, response -> return null }; } ");
		GrailsControllerClass grailsClass = new DefaultGrailsControllerClass(clazz);
		assertEquals("Overview", grailsClass.getName());
		assertEquals("OverviewController", grailsClass.getFullName());
		assertEquals("listPage", grailsClass.getViewName("/overview/list"));
		assertEquals(2, grailsClass.getURIs().length);
	}

}
