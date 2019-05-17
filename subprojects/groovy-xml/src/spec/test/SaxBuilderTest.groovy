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

class SaxBuilderTest extends GroovyTestCase {

    // tag::sax_builder_handler[]
    class LogHandler extends org.xml.sax.helpers.DefaultHandler {
        
        String log = ''
        
        void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
            log += "Start Element: $localName, "
        }
        
        void endElement(String uri, String localName, String qName) {
            log += "End Element: $localName, "
        }
    }
    // end::sax_builder_handler[]

    void testSaxBuilder() {
        // tag::sax_builder[]
        def handler = new LogHandler()
        def builder = new groovy.xml.SAXBuilder(handler)
        
        builder.root() {
            helloWorld()
        }
        // end::sax_builder[]
        
        // tag::sax_builder_assert[]
        assert handler.log == 'Start Element: root, Start Element: helloWorld, End Element: helloWorld, End Element: root, '
        // end::sax_builder_assert[]
    }
}