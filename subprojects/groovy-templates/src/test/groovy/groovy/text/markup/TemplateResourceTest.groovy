/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.text.markup

import groovy.test.GroovyTestCase
import groovy.text.markup.MarkupTemplateEngine.TemplateResource;

class TemplateResourceTest extends GroovyTestCase {
	
	void testSimplePath() {
		def resource = TemplateResource.parse("simple.foo")
		assertFalse(resource.hasLocale())
		assertEquals("simple.foo", resource.toString())
		assertEquals("simple_fr_FR.foo", resource.withLocale("fr_FR").toString())
	}
	
	void testPathWithLocale() {
		def resource = TemplateResource.parse("simple_fr_FR.foo")
		assertTrue(resource.hasLocale())
		assertEquals("simple_fr_FR.foo", resource.toString())
		assertEquals("simple.foo", resource.withLocale(null).toString())
	}
	
	void testPathWithMultipleDots() {
		def resource = TemplateResource.parse("simple.foo.bar")
		assertFalse(resource.hasLocale())
		assertEquals("simple.foo.bar", resource.toString())
		assertEquals("simple_fr_FR.foo.bar", resource.withLocale("fr_FR").toString())
	}
	
	void testPathWithLocaleAndMultipleDots() {
		def resource = TemplateResource.parse("simple_fr_FR.foo.bar")
		assertTrue(resource.hasLocale())
		assertEquals("simple_fr_FR.foo.bar", resource.toString())
		assertEquals("simple.foo.bar", resource.withLocale(null).toString())
	}

}
