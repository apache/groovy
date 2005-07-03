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

import java.io.File;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 3, 2005
 */
public class MultipleClassesPerFileTests extends TestCase {

	public MultipleClassesPerFileTests() {
		super();
	}

	public MultipleClassesPerFileTests(String arg0) {
		super(arg0);
	}

	public void testMultipleClassesPerFile() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		cl.parseClass(new File("test/commons/org/codehaus/groovy/grails/commons/classes.groovy"));
		Class testClass1 = cl.loadClass("TestClass1");
		Class testClass2 = cl.loadClass("TestClass2");
		
		try {
			cl.loadClass("TestClass3");
			fail();
		} catch (ClassNotFoundException e) {
			// expected
		}
		
		assertNotNull(testClass1);
		assertNotNull(testClass2);
	}
}
