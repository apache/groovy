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

class StaxBuilderTest extends GroovyTestCase {

    void testStaxBuilder() {
        // tag::stax_builder[]
        def factory = javax.xml.stream.XMLOutputFactory.newInstance()
        def writer = new StringWriter()
        def builder = new groovy.xml.StaxBuilder(factory.createXMLStreamWriter(writer))
        
        builder.root(attribute:1) {
            elem1('hello')
            elem2('world')
        }
        
        assert writer.toString() == '<?xml version="1.0" ?><root attribute="1"><elem1>hello</elem1><elem2>world</elem2></root>'
        // end::stax_builder[]
    }
    
    void testStaxBuilderExternalLibrary() {
        assertScript '''
            // tag::stax_builder_external_library[]
            @Grab('org.codehaus.jettison:jettison:1.3.3')
            @GrabExclude('stax:stax-api') // part of Java 6 and later
            import org.codehaus.jettison.mapped.*

            def writer = new StringWriter()
            def mappedWriter = new MappedXMLStreamWriter(new MappedNamespaceConvention(), writer)
            def builder = new groovy.xml.StaxBuilder(mappedWriter)
            
            builder.root(attribute:1) {
                 elem1('hello')
                 elem2('world')
            }
            
            assert writer.toString() == '{"root":{"@attribute":"1","elem1":"hello","elem2":"world"}}'
            // end::stax_builder_external_library[]
        '''
    }
}