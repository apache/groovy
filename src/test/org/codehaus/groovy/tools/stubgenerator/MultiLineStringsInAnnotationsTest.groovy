/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.stubgenerator

import org.junit.Assert

// http://jira.codehaus.org/browse/GROOVY-4470
class MultiLineStringsInAnnotationsTest extends StringSourcesStubTestCase {
	Map<String, String> provideSources() {
		['StringAnn.java': '''
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
public @interface StringAnn {
	String value();
}
		''',
		'StringAnnUsage.groovy': '''
@StringAnn("""Now that's what
I
	call a
  multi-line
String!""")
class StringAnnUsage {}
		'''
		]
	}

	void verifyStubs() {
		def ann = classes['StringAnnUsage'].annotations[0]
		Assert.assertEquals("\"Now that's what\\nI\\n	call a\\n  multi-line\\nString!\"", ann.getNamedParameter("value"))
	}
}