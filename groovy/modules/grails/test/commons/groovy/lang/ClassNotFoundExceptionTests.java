package groovy.lang;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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

public class ClassNotFoundExceptionTests extends TestCase {


	public ClassNotFoundExceptionTests() {
		super();
	}

	public ClassNotFoundExceptionTests(String arg0) {
		super(arg0);
	}

	public void testClassNotFoundException() throws Exception {
		GroovyClassLoader gcl = new GroovyClassLoader();
		gcl.setResourceLoader(new GroovyResourceLoader() {
			public File loadGroovyFile(String filename) {
				if ("Order".equals(filename)) {
					return new File("test/commons/groovy/lang/Order.groovy");
				} else {
					return null;
				}
			}

			public URL loadGroovySource(String filename) throws MalformedURLException {
				return null;
			}
		});
		gcl.parseClass(new File("test/commons/groovy/lang/OrderService.groovy"));
	}
	
	public void testClassNotFoundExceptionGrailsApplication() throws Exception {
		DefaultGrailsApplication app = new DefaultGrailsApplication(new PathMatchingResourcePatternResolver().getResources("file:test/commons/groovy/lang/*.groovy"));
	}
}
