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

class XmlParserTypedTest {

    static class ServerConfig {
        String host
        int port
        boolean debug
    }

    static class AppConfig {
        String name
        ServerConfig server
    }

    static class SimpleConfig {
        String host
        String port
    }

    @Test
    void testParseTextAsSimple() {
        def config = new XmlParser().parseTextAs(ServerConfig, '''\
            <server>
                <host>localhost</host>
                <port>8080</port>
                <debug>true</debug>
            </server>'''.stripIndent())
        assert config instanceof ServerConfig
        assert config.host == 'localhost'
        assert config.port == 8080
        assert config.debug == true
    }

    @Test
    void testParseTextAsNested() {
        def config = new XmlParser().parseTextAs(AppConfig, '''\
            <app>
                <name>myapp</name>
                <server>
                    <host>localhost</host>
                    <port>9090</port>
                    <debug>false</debug>
                </server>
            </app>'''.stripIndent())
        assert config instanceof AppConfig
        assert config.name == 'myapp'
        assert config.server instanceof ServerConfig
        assert config.server.host == 'localhost'
        assert config.server.port == 9090
    }

    @Test
    void testParseAsFromReader() {
        def xml = '<server><host>localhost</host><port>8080</port><debug>false</debug></server>'
        def config = new XmlParser().parseAs(ServerConfig, new StringReader(xml))
        assert config.host == 'localhost'
        assert config.port == 8080
    }

    @Test
    void testParseAsFromInputStream() {
        def xml = '<server><host>localhost</host><port>8080</port><debug>false</debug></server>'
        def config = new XmlParser().parseAs(ServerConfig, new ByteArrayInputStream(xml.bytes))
        assert config.host == 'localhost'
        assert config.port == 8080
    }

    @Test
    void testParseAsFromFile() {
        def xml = '<server><host>localhost</host><port>8080</port><debug>false</debug></server>'
        def file = File.createTempFile('xmltest', '.xml')
        file.deleteOnExit()
        file.text = xml
        def config = new XmlParser().parseAs(ServerConfig, file)
        assert config.host == 'localhost'
        assert config.port == 8080
    }

    @Test
    void testParseAsFromPath() {
        def xml = '<server><host>localhost</host><port>8080</port><debug>false</debug></server>'
        def file = File.createTempFile('xmltest', '.xml')
        file.deleteOnExit()
        file.text = xml
        def config = new XmlParser().parseAs(ServerConfig, file.toPath())
        assert config.host == 'localhost'
        assert config.port == 8080
    }

    @Test
    void testParseTextAsWithAttributes() {
        def config = new XmlParser().parseTextAs(ServerConfig, '<server host="localhost" port="8080" debug="true"/>')
        assert config.host == 'localhost'
        assert config.port == 8080
        assert config.debug == true
    }

    @Test
    void testNodeAsMap() {
        def node = new XmlParser().parseText('<server><host>localhost</host><port>8080</port></server>')
        def map = node as Map
        assert map instanceof Map
        assert map.host == 'localhost'
        assert map.port == '8080'
    }

    @Test
    void testNodeAsTypedObject() {
        // as coercion works for String-typed properties without Jackson
        def node = new XmlParser().parseText('<server><host>localhost</host><port>8080</port></server>')
        def config = node as SimpleConfig
        assert config instanceof SimpleConfig
        assert config.host == 'localhost'
        assert config.port == '8080'
    }

    @Test
    void testNodeAsTypedObjectWithTypeConversion() {
        // parseAs uses Jackson for full type conversion (String -> int, boolean, etc.)
        def config = new XmlParser().parseTextAs(ServerConfig, '<server><host>localhost</host><port>8080</port><debug>true</debug></server>')
        assert config instanceof ServerConfig
        assert config.host == 'localhost'
        assert config.port == 8080
        assert config.debug == true
    }
}
