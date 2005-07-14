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

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 5, 2005
 */
public class OpenRicoBuilderTests extends TestCase {

	public OpenRicoBuilderTests() {
		super();
	}

	public OpenRicoBuilderTests(String arg0) {
		super(arg0);
	}

	private HttpServletResponse getResponse(Writer writer) {
		final PrintWriter printer = new PrintWriter(writer);
		return new MockHttpServletResponse() {
			public PrintWriter getWriter() throws UnsupportedEncodingException {
				return printer;
			}
		};
	}

	private void parse(String groovy) throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Class clazz = cl.parseClass("import grails.util.*; class TestClass { List names = [\"Steven\", \"Hans\", \"Erwin\"]; @Property Closure test = { response -> new OpenRicoBuilder(response)." + groovy + "; } }");
		GroovyObject go = (GroovyObject)clazz.newInstance();
		Closure closure = (Closure)go.getProperty("test");
		StringWriter sw = new StringWriter();
		closure.call(new Object[] { getResponse(sw) });
		System.out.println(sw.getBuffer().toString());
	}

	
	public void testOpenRicoBuilderAjaxResponse() throws Exception {
		StringWriter sw = new StringWriter();
		new OpenRicoBuilder(getResponse(sw));
		System.out.println(sw.getBuffer().toString());
	}

	public void testOpenRicoBuilderElement() throws Exception {
		parse("ajax{ element(id:\"test\") { } }");
		
		try {
			parse("ajax{ test(id:\"test\") { } }");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			parse("element{ test(id:\"test\") { } }");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		
		parse("ajax{ element(id:\"test\") { testTag {} } }");
		
		parse("ajax() { element(id:\"test\") { testTag {} }; element(id:\"test2\") {}  }");

		parse("ajax { object(id:\"test\") { testTag {} }; object(id:\"test2\") {}  }");

		parse("ajax { element(id:\"test\") { select(name:\"test\") { for (name in names) { span(name) } } } }");
	}
}
