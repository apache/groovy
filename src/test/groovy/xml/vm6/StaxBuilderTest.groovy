/*
 * Copyright 2003-2008 the original author or authors.
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

package groovy.xml.vm6

import javax.xml.stream.XMLOutputFactory
import groovy.xml.StaxBuilder
//import org.codehaus.jettison.mapped.MappedNamespaceConvention
//import org.codehaus.jettison.mapped.MappedXMLStreamWriter

/**
 * Tests Stax builder with XML
 * 
 * @author <a href="dejan@nighttale.net">Dejan Bosanac</a>
 * @author Paul King
 */
class StaxBuilderTest extends GroovyTestCase {

	void testJava6() {
		def factory = XMLOutputFactory.newInstance()
        def writer = new StringWriter()
		def builder = new StaxBuilder(factory.createXMLStreamWriter(writer))
        builder.root1(a:5, b:7) {
            elem1('hello1')
            elem2('hello2')
            elem3(x:7)
        }
		assert writer, """<root1 a="5" b="7"><elem1>hello1</elem1><elem2>hello2</elem2><elem3 x="7" /></root1>"""
	}

// @Grab(groupId:'org.codehaus.jettison', artifactId:'jettison', version:'1.0.1', scope:'test', optional:'true')
//	void testJettison() {
//        MappedNamespaceConvention con = new MappedNamespaceConvention();
//		StringWriter writer = new StringWriter()
//		MappedXMLStreamWriter mappedWriter = new MappedXMLStreamWriter(con, writer);
//		StaxBuilder builder = new StaxBuilder(mappedWriter)
//        builder.root1(a:5, b:7) {
//            elem1('hello1')
//            elem2('hello2')
//            elem3(x:7)
//        }
//		assert writer, """{"root1":{"@a":"5","@b":"7","elem1":"hello1","elem2":"hello2","elem3":{"@x":"7"}}}"""
//	}

}
