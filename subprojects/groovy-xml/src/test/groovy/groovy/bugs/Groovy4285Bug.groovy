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
package groovy.bugs

import groovy.xml.XmlUtil

class Groovy4285Bug extends GroovyShellTestCase {
    void testXMLSerializationOfGPathResultObject() {
        def xmlStr = """<?xml version="1.0" encoding="UTF-8"?>
        <stuff ver="1.0">
          <properties>
            <foo>bar</foo>
          </properties>
        </stuff>"""
        
        def gpr = new XmlSlurper().parseText(xmlStr)
        def serializedXml = XmlUtil.serialize(gpr)

        assert serializedXml.contains('<stuff')
        assert serializedXml.contains('<properties>')
        assert serializedXml.contains('<foo>')
        assert serializedXml.contains('bar')
    }

    // GROOVY-9664
    void testXMLSerializationOfLegacyGPathResultObject() {
        assertScript '''
            import groovy.util.slurpersupport.NodeChild
            import groovy.xml.XmlUtil

            String xml = '<xml xmlns="http://example.com/ns"><abc><xyz>true</xyz></abc></xml>'
            String name = 'abc'

            NodeChild parsed = new XmlSlurper().parseText(xml)
            def node = parsed."$name"

            assert XmlUtil.serialize(node).contains('xyz>true</')
        '''
    }
}
