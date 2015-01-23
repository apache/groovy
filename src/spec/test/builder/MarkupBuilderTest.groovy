/*
 * Copyright 2003-2015 the original author or authors.
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

package builder

import groovy.util.GroovyTestCase

class MarkupBuilderTest extends GroovyTestCase {

    void testMarkupBuilderXMLWithoutNamespaces() {
        assertScript """
            import groovy.xml.MarkupBuilder
            @Grab('xmlunit:xmlunit:1.6')
            import org.custommonkey.xmlunit.*

            // tag::xml_string[]
            String carRecords = '''
                <records>
                  <car name='HSV Maloo' make='Holden' year='2006'>
                    <country>Australia</country>
                    <record type='speed'>production pickup truck with speed of 271kph</record>
                  </car>
                  <car name='Royale' make='Bugatti' year='1931'>
                    <country>France</country>
                    <record type='price'>most valuable car at 15 million dollars</record>
                  </car>
                </records>
            '''
            // end::xml_string[]
            
            // tag::xml_builder[]
            StringWriter writer = new StringWriter()
            MarkupBuilder builder = new MarkupBuilder(writer)
            builder.records() {
              car(name: 'HSV Maloo', make: 'Holden', year: 2006) {
                country 'Australia'
                record(type: 'speed', 'production pickup truck with speed of 271kph')
              }
              car(name: 'Royale', make: 'Bugatti', year: 1931) {
                country 'France'
                record(type: 'price', 'most valuable car at 15 million dollars')
              }
            }
            String xml = writer.toString()
            // end::xml_builder[]
            
            // tag::xml_assert[]
            XMLUnit.setIgnoreWhitespace(true)
            def xmlDiff = new Diff(xml, carRecords)
            assert xmlDiff.similar()
            // end::xml_assert[]
       """
    }
    
     void testMarkupBuilderXMLWithNamespaces() {
        assertScript """
            import groovy.xml.MarkupBuilder
            @Grab('xmlunit:xmlunit:1.6')
            import org.custommonkey.xmlunit.*

            // tag::xml_string_namespaces[]
            String carRecords = '''
                <rec:records xmlns:rec='http://www.groovy-lang.org/records'>
                  <car name='HSV Maloo' make='Holden' year='2006' xmlns='http://www.acme.com/cars'>
                    <country>Australia</country>
                    <record type='speed'>production pickup truck with speed of 271kph</record>
                  </car>
                </rec:records>
            '''
            // end::xml_string_namespaces[]
            
            // tag::xml_builder_namespaces[]
            StringWriter writer = new StringWriter()
            MarkupBuilder builder = new MarkupBuilder(writer)
            builder.'rec:records'('xmlns:rec': 'http://www.groovy-lang.org/records' ) { // <1>
              car(name: 'HSV Maloo', make: 'Holden', year: 2006, xmlns: 'http://www.acme.com/cars') { // <2>
                country 'Australia'
                record(type: 'speed', 'production pickup truck with speed of 271kph')
              }
            }
            String xml = writer.toString()
            // end::xml_builder_namespaces[]
            
            // tag::xml_assert_namespaces[]
            XMLUnit.setIgnoreWhitespace(true)
            def xmlDiff = new Diff(xml, carRecords)
            assert xmlDiff.similar()
            // end::xml_assert_namespaces[]
       """
    }
}