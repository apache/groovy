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

import groovy.util.Node
import org.junit.jupiter.api.Test

/**
 * Tests for the Groovy Xml user guide related to typed XML parsing.
 */
class UserGuideXmlTypedTest {

    // tag::server_class[]
    static class ServerConfig {
        String host
        int port
        boolean debug
    }
    // end::server_class[]

    // tag::app_class[]
    static class AppConfig {
        String name
        ServerConfig server
    }
    // end::app_class[]

    // tag::simple_config_class[]
    static class SimpleConfig {
        String host
        String port
    }
    // end::simple_config_class[]

    @Test
    void testToMap() {
        // tag::toMap[]
        def node = new XmlParser().parseText('''
            <server>
                <host>localhost</host>
                <port>8080</port>
                <debug>true</debug>
            </server>'''.stripIndent())

        def map = node.toMap()
        assert map == [host: 'localhost', port: '8080', debug: 'true']
        // end::toMap[]
    }

    @Test
    void testToMapNested() {
        // tag::toMap_nested[]
        def node = new XmlParser().parseText('''
            <app>
                <name>myapp</name>
                <server>
                    <host>localhost</host>
                    <port>8080</port>
                </server>
            </app>'''.stripIndent())

        def map = node.toMap()
        assert map == [name: 'myapp', server: [host: 'localhost', port: '8080']]
        // end::toMap_nested[]
    }

    @Test
    void testToMapAttributes() {
        // tag::toMap_attributes[]
        def node = new XmlParser().parseText('<server host="localhost" port="8080"/>')

        def map = node.toMap()
        assert map == [host: 'localhost', port: '8080']
        // end::toMap_attributes[]
    }

    @Test
    void testToMapRepeatedElements() {
        // tag::toMap_repeated[]
        def node = new XmlParser().parseText('''
            <config>
                <alias>web1</alias>
                <alias>web2</alias>
                <alias>web3</alias>
            </config>'''.stripIndent())

        def map = node.toMap()
        assert map == [alias: ['web1', 'web2', 'web3']]
        // end::toMap_repeated[]
    }

    @Test
    void testToMapTextKey() {
        // tag::toMap_text_key[]
        def node = new XmlParser().parseText('<price currency="USD">9.99</price>')

        def map = node.toMap()
        assert map == [currency: 'USD', _text: '9.99']
        assert map[Node.TEXT_KEY] == '9.99'
        // end::toMap_text_key[]
    }

    @Test
    void testAsCoercion() {
        // tag::as_coercion[]
        def node = new XmlParser().parseText('''
            <server>
                <host>localhost</host>
                <port>8080</port>
            </server>'''.stripIndent())

        def config = node as SimpleConfig
        assert config.host == 'localhost'
        assert config.port == '8080'
        // end::as_coercion[]
    }

    @Test
    void testAsMap() {
        // tag::as_map[]
        def node = new XmlParser().parseText('<server><host>localhost</host></server>')

        Map map = node as Map
        assert map.host == 'localhost'
        // end::as_map[]
    }

    @Test
    void testParseTextAs() {
        // tag::parseTextAs[]
        def config = new XmlParser().parseTextAs(ServerConfig, '''
            <server>
                <host>localhost</host>
                <port>8080</port>
                <debug>true</debug>
            </server>'''.stripIndent())

        assert config instanceof ServerConfig
        assert config.host == 'localhost'
        assert config.port == 8080
        assert config.debug == true
        // end::parseTextAs[]
    }

    @Test
    void testParseTextAsNested() {
        // tag::parseTextAs_nested[]
        def config = new XmlParser().parseTextAs(AppConfig, '''
            <app>
                <name>myapp</name>
                <server>
                    <host>localhost</host>
                    <port>9090</port>
                    <debug>false</debug>
                </server>
            </app>'''.stripIndent())

        assert config.name == 'myapp'
        assert config.server.host == 'localhost'
        assert config.server.port == 9090
        // end::parseTextAs_nested[]
    }

    @Test
    void testRollYourOwnJacksonXml() {
        // tag::roll_your_own[]
        // If you need full Jackson XML support (e.g. @JacksonXmlText,
        // @JacksonXmlElementWrapper), use jackson-dataformat-xml directly:
        //
        // @Grab('com.fasterxml.jackson.dataformat:jackson-dataformat-xml')
        // import com.fasterxml.jackson.dataformat.xml.XmlMapper
        //
        // def config = new XmlMapper().readValue(xmlString, ServerConfig)
        //
        // Or parse first with XmlParser for validation, then re-serialize:
        //
        // def node = new XmlParser().parseText(xmlString)
        // // ... inspect or validate ...
        // def config = new XmlMapper().readValue(XmlUtil.serialize(node), ServerConfig)
        // end::roll_your_own[]
    }
}
