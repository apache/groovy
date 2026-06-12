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
package groovy.xml

import org.junit.jupiter.api.Test

class XmlUtilSerializeOptionsTest {

    @Test
    void defaultOptionsMatchExistingBehaviour() {
        def xml = '<root><item>value</item></root>'
        def withoutOptions = XmlUtil.serialize(xml)
        def withOptions = XmlUtil.serialize(xml, new SerializeOptions())
        assert withoutOptions == withOptions
    }

    @Test
    void encodingInXmlDeclaration() {
        def xml = '<root><item>value</item></root>'
        def result = XmlUtil.serialize(xml, new SerializeOptions(encoding: 'ISO-8859-1'))
        assert result.contains('encoding="ISO-8859-1"')
    }

    @Test
    void defaultEncodingIsUtf8() {
        def xml = '<root><item>value</item></root>'
        def result = XmlUtil.serialize(xml, new SerializeOptions())
        assert result.contains('encoding="UTF-8"')
    }

    @Test
    void customIndent() {
        def xml = '<root><item>value</item></root>'
        def twoSpaces = XmlUtil.serialize(xml, new SerializeOptions(indent: 2))
        def fourSpaces = XmlUtil.serialize(xml, new SerializeOptions(indent: 4))
        assert twoSpaces.contains('  <item>')
        assert fourSpaces.contains('    <item>')
    }

    @Test
    void serializeNodeWithOptions() {
        def node = new XmlParser().parseText('<root><item>value</item></root>')
        def result = XmlUtil.serialize(node, new SerializeOptions(encoding: 'ISO-8859-1'))
        assert result.contains('encoding="ISO-8859-1"')
        assert result.contains('<item>value</item>')
    }

    @Test
    void serializeGPathResultWithOptions() {
        def gpath = new XmlSlurper().parseText('<root><item>value</item></root>')
        def result = XmlUtil.serialize(gpath, new SerializeOptions(encoding: 'ISO-8859-1'))
        assert result.contains('encoding="ISO-8859-1"')
    }

    @Test
    void serializeToOutputStreamWithEncoding() {
        def xml = '<root><item>value</item></root>'
        def baos = new ByteArrayOutputStream()
        XmlUtil.serialize(xml, baos, new SerializeOptions(encoding: 'ISO-8859-1'))
        def result = baos.toString('ISO-8859-1')
        assert result.contains('encoding="ISO-8859-1"')
    }

    @Test
    void serializeOptionsNamedParams() {
        def opts = new SerializeOptions(encoding: 'UTF-16', indent: 4, allowDocTypeDeclaration: true)
        assert opts.encoding == 'UTF-16'
        assert opts.indent == 4
        assert opts.allowDocTypeDeclaration == true
    }
}
