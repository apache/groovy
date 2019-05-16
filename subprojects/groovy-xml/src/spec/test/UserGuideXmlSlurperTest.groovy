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
/**
 * Tests for the Groovy Xml user guide related to XmlSlurper.
 */
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlSlurper
import groovy.test.GroovyTestCase

class UserGuideXmlSlurperTest extends GroovyTestCase {

    // tag::books[]
    static final String books = '''
        <response version-api="2.0">
            <value>
                <books>
                    <book available="20" id="1">
                        <title>Don Quixote</title>
                        <author id="1">Miguel de Cervantes</author>
                    </book>
                    <book available="14" id="2">
                        <title>Catcher in the Rye</title>
                       <author id="2">JD Salinger</author>
                   </book>
                   <book available="13" id="3">
                       <title>Alice in Wonderland</title>
                       <author id="3">Lewis Carroll</author>
                   </book>
                   <book available="5" id="4">
                       <title>Don Quixote</title>
                       <author id="4">Miguel de Cervantes</author>
                   </book>
               </books>
           </value>
        </response>
    '''
    // end::books[]

    void testParseText() {
        // tag::testParseText[]
        def text = '''
            <list>
                <technology>
                    <name>Groovy</name>
                </technology>
            </list>
        '''

        def list = new XmlSlurper().parseText(text) // <1>

        assert list instanceof groovy.xml.slurpersupport.GPathResult // <2>
        assert list.technology.name == 'Groovy' // <3>
        // end::testParseText[]
    }

    void testGettingANodeText() {
        // tag::testGettingANodeText[]
        def response = new XmlSlurper().parseText(books)
        def authorResult = response.value.books.book[0].author

        assert authorResult.text() == 'Miguel de Cervantes'
        // end::testGettingANodeText[]
    }

    void testGettingAnAttributeText() {
        // tag::testGettingAnAttributeText[]
        def response = new XmlSlurper().parseText(books)

        def book = response.value.books.book[0] // <1>
        def bookAuthorId1 = book.@id // <2>
        def bookAuthorId2 = book['@id'] // <3>

        assert bookAuthorId1 == '1' // <4>
        assert bookAuthorId1.toInteger() == 1 // <5>
        assert bookAuthorId1 == bookAuthorId2
        // end::testGettingAnAttributeText[]
    }

    void testChildren() {
        // tag::testChildren[]
        def response = new XmlSlurper().parseText(books)

        // .'*' could be replaced by .children()
        def catcherInTheRye = response.value.books.'*'.find { node ->
            // node.@id == 2 could be expressed as node['@id'] == 2
            node.name() == 'book' && node.@id == '2'
        }

        assert catcherInTheRye.title.text() == 'Catcher in the Rye'
        // end::testChildren[]
    }

    void testDepthFirst1() {
        // tag::testDepthFirst1[]
        def response = new XmlSlurper().parseText(books)

        // .'**' could be replaced by .depthFirst()
        def bookId = response.'**'.find { book ->
            book.author.text() == 'Lewis Carroll'
        }.@id

        assert bookId == 3
        // end::testDepthFirst1[]
    }

    void testDepthFirst2() {
        // tag::testDepthFirst2[]
        def response = new XmlSlurper().parseText(books)

        def titles = response.'**'.findAll { node -> node.name() == 'title' }*.text()

        assert titles.size() == 4
        // end::testDepthFirst2[]
    }

    void testDepthVsBreadth() {
        // tag::testDepthVsBreadth[]
        def response = new XmlSlurper().parseText(books)
        def nodeName = { node -> node.name() }
        def withId2or3 = { node -> node.@id in [2, 3] }

        assert ['book', 'author', 'book', 'author'] ==
                response.value.books.depthFirst().findAll(withId2or3).collect(nodeName)
        assert ['book', 'book', 'author', 'author'] ==
                response.value.books.breadthFirst().findAll(withId2or3).collect(nodeName)
        // end::testDepthVsBreadth[]
    }

    void testHelpers() {
        // tag::testHelpers[]
        def response = new XmlSlurper().parseText(books)

        def titles = response.value.books.book.findAll { book ->
            /* You can use toInteger() over the GPathResult object */
            book.@id.toInteger() > 2
        }*.title

        assert titles.size() == 2
        // end::testHelpers[]
    }

    void testModifyingNodes1() {
        // tag::testModifyingNodes1[]
        def response = new XmlSlurper().parseText(books)

        /* Use the same syntax as groovy.xml.MarkupBuilder */
        response.value.books.book[0].replaceNode {
            book(id: "3") {
                title("To Kill a Mockingbird")
                author(id: "3", "Harper Lee")
            }
        }

        assert response.value.books.book[0].title.text() == "Don Quixote"

        /* That mkp is a special namespace used to escape away from the normal building mode
           of the builder and get access to helper markup methods
           'yield', 'pi', 'comment', 'out', 'namespaces', 'xmlDeclaration' and
           'yieldUnescaped' */
        def result = new StreamingMarkupBuilder().bind { mkp.yield response }.toString()
        def changedResponse = new XmlSlurper().parseText(result)

        assert changedResponse.value.books.book[0].title.text() == "To Kill a Mockingbird"
        // end::testModifyingNodes1[]
    }

    void testSettingAttributes1() {
        // tag::testSettingAttributes1[]
        def response = new XmlSlurper().parseText(books)
        response.@numberOfResults = "2"

        assert response.@numberOfResults == "2"
        // end::testSettingAttributes1[]
    }

}
