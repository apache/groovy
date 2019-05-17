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

import groovy.namespace.QName
import groovy.test.GroovyTestCase

/**
 * Tests for the Groovy Xml user guide related to XmlParser.
 */
class UserGuideXmlParserTest extends GroovyTestCase {

    // tag::responseBookXml[]
    def xml = """
    <response version-api="2.0">
        <value>
            <books>
                <book id="2">
                    <title>Don Quixote</title>
                    <author id="1">Miguel de Cervantes</author>
                </book>
            </books>
        </value>
    </response>
    """
    // end::responseBookXml[]

    void testParseText() {
        // tag::testParseText[]
        def text = '''
            <list>
                <technology>
                    <name>Groovy</name>
                </technology>
            </list>
        '''

        def list = new XmlParser().parseText(text) // <1>

        assert list instanceof groovy.util.Node // <2>
        assert list.technology.name.text() == 'Groovy' // <3>
        // end::testParseText[]
    }

    void testAddingNodes1() {
        // tag::testAddingNodes1[]
        def parser = new XmlParser()
        def response = parser.parseText(xml)
        def numberOfResults = parser.createNode(
                response,
                new QName("numberOfResults"),
                [:]
        )

        numberOfResults.value = "1"
        assert response.numberOfResults.text() == "1"
        // end::testAddingNodes1[]
    }

    void testAddingNodes2() {
        // tag::testAddingNodes2[]
        def parser = new XmlParser()
        def response = parser.parseText(xml)

        response.appendNode(
                new QName("numberOfResults"),
                [:],
                "1"
        )

        response.numberOfResults.text() == "1"
        // end::testAddingNodes2[]
    }

    void testModifyingNodes1() {
        // tag::testModifyingNodes1[]
        def response = new XmlParser().parseText(xml)

        /* Use the same syntax as groovy.xml.MarkupBuilder */
        response.value.books.book[0].replaceNode { // <1>
            book(id: "3") {
                title("To Kill a Mockingbird")
                author(id: "3", "Harper Lee")
            }
        }

        def newNode = response.value.books.book[0]

        assert newNode.name() == "book"
        assert newNode.@id == "3"
        assert newNode.title.text() == "To Kill a Mockingbird"
        assert newNode.author.text() == "Harper Lee"
        assert newNode.author.@id.first() == "3"
        // end::testModifyingNodes1[]
    }

    void testSettingAttributes1() {
        // tag::testSettingAttributes1[]
        def parser = new XmlParser()
        def response = parser.parseText(xml)

        response.@numberOfResults = "1"

        assert response.@numberOfResults == "1"
        // end::testSettingAttributes1[]
    }

}
