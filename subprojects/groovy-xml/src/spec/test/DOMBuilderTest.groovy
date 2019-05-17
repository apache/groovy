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
import groovy.test.GroovyTestCase

class DOMBuilderTest extends GroovyTestCase {

    void testParse() {
        // tag::xml_string[]
        String recordsXML = '''
            <records>
              <car name='HSV Maloo' make='Holden' year='2006'>
                <country>Australia</country>
                <record type='speed'>Production Pickup Truck with speed of 271kph</record>
              </car>
              <car name='P50' make='Peel' year='1962'>
                <country>Isle of Man</country>
                <record type='size'>Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record>
              </car>
              <car name='Royale' make='Bugatti' year='1931'>
                <country>France</country>
                <record type='price'>Most Valuable Car at $15 million</record>
              </car>
            </records>'''
        // end::xml_string[]
        
        // tag::dom_builder_parse[]
        def reader = new StringReader(recordsXML)
        def doc = groovy.xml.DOMBuilder.parse(reader)
        // end::dom_builder_parse[]
        
        // tag::dom_builder_process_result[]
        def records = doc.documentElement
        use(groovy.xml.dom.DOMCategory) {
            assert records.car.size() == 3
        }
        // end::dom_builder_process_result[]
  }
}
