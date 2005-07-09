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
package grails.pageflow;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class PageFlowBuilderTest extends TestCase {

	public PageFlowBuilderTest() {
		super();
	}

	public PageFlowBuilderTest(String arg0) {
		super(arg0);
	}

	public void testSuccessPageFlowBuilder1() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Class clazz = cl.parseClass("" +
				"import grails.pageflow.*;\n" +
				"\n" +
				"class TestClass {\n" +
				"  @Property Flow flow = new PageFlowBuilder().flow {\n" +
				"    onSubmit(action:[formObjectClass:Object.class,formObjectName:\"object\"]) {\n" +
				"      success(\"myFirstState\")\n" +
				"    }\n" +
				"    myFirstState(view:\"myView\") {\n" +
				"      submit(\"myNextState\")\n" +
				"    }\n" +
				"  }\n" +
				"}\n");
		GroovyObject go = (GroovyObject)clazz.newInstance();
		Object flow = go.getProperty("flow");
		assertNotNull(flow);
		assertTrue(flow instanceof Flow);
		System.out.println(flow);
	}
}
