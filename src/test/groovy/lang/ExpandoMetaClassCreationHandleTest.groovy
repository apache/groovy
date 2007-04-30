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
package groovy.lang;


/**
 * @author Graeme Rocher
 */

class ExpandoMetaClassCreationHandleTest extends GroovyTestCase {

	def registry
	def original
	void setUp() {
		registry = GroovySystem.metaClassRegistry
		def handle = new ExpandoMetaClassCreationHandle()
		original = registry.metaClassCreationHandle
		registry.setMetaClassCreationHandle(handle);
	}

	void tearDown() {
		registry.metaClassCreationHandle = original
		original = null
		registry = null
	}

	void testExpandoCreationHandle() {
		def metaClass = registry.getMetaClass(URL.class)
		if(!(metaClass instanceof ExpandoMetaClass)) {
			registry.removeMetaClass(URL.class)
		}

		def url = new URL("http://grails.org")
		metaClass = registry.getMetaClass(url.getClass())
		assertTrue(metaClass instanceof ExpandoMetaClass)


		metaClass.toUpperString = {->
			delegate.toString().toUpperCase()
		}

		assertEquals "http://grails.org", url.toString()
		assertEquals "HTTP://GRAILS.ORG", url.toUpperString()
	}

	void testExpandoInheritance() {
		registry.removeMetaClass(String.class)

		def metaClass = registry.getMetaClass(Object.class)
		assertTrue(metaClass instanceof ExpandoMetaClass)

		metaClass.toFoo = {-> "foo" }

		def uri = new URI("http://bar.com")
		def s = "bar"

		assertEquals "foo", uri.toFoo()
		assertEquals "foo", s.toFoo()

		metaClass.toBar = {-> "bar" }

		assertEquals "bar", uri.toBar()
		assertEquals "bar", s.toBar()
	}
}