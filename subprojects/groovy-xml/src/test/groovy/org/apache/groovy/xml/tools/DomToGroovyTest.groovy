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
package org.apache.groovy.xml.tools

import groovy.test.StringTestUtil
import groovy.xml.XmlSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import static org.junit.jupiter.api.Assertions.assertTrue

class DomToGroovyTest {

    private static final String TEST_XML_1 =
        "<a href='http://groovy.codehaus.org'>Groovy</a>"
    private static final String TEST_XML_2 =
        "<project name='testProject'><target name='testTarget'><echo>message</echo><echo/></target></project>"
    private static final String TEST_XML_3 = '''<?xml version="1.0"?>
        <!-- this example demonstrates using markup to specify a rich user interface -->
        <frame size="[300,300]" text="My Window">
          <label bounds="[10,10,290,30]" text="Save changes"/>
          <panel bounds="[10,40,290,290]">
            <button action="save()" text="OK"/>
            <button action="close()" text="Cancel"/>
          </panel>
        </frame>'''
    private static final String TEST_XML_4 = '''
        <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
          <xsd:simpleType name="SKU">
            <xsd:restriction base="xsd:string">
              <xsd:pattern value="\\d{3}-[A-Z]{2}"/>
            </xsd:restriction>
          </xsd:simpleType>
        </xsd:schema>'''
    private static final String TEST_XML_5 = '''
        <element xml:lang="en-US" > blabla </element>
    '''
    private static final String EXPECTED_BUILDER_SCRIPT_1 =
        "a(href:'http://groovy.codehaus.org', 'Groovy')"
    private static final String EXPECTED_BUILDER_SCRIPT_2 = '''
        project(name:'testProject') {
          target(name:'testTarget') {
            echo('message')
            echo()
          }
        }'''
    private static final String EXPECTED_BUILDER_SCRIPT_3 = '''
        /* this example demonstrates using markup to specify a rich user interface */
        frame(size:'[300,300]', text:'My Window') {
          label(bounds:'[10,10,290,30]', text:'Save changes')
          panel(bounds:'[10,40,290,290]') {
            button(action:'save()', text:'OK')
            button(action:'close()', text:'Cancel')
          }
        }'''
    private static final String EXPECTED_BUILDER_SCRIPT_4 = '''
        mkp.declareNamespace(xsd:'http://www.w3.org/2001/XMLSchema')
        'xsd.schema'() {
          'xsd.simpleType'(name:'SKU') {
            'xsd.restriction'(base:'xsd:string') {
              'xsd.pattern'(value:'\\\\d{3}-[A-Z]{2}')
            }
          }
        }'''
    private static final String EXPECTED_BUILDER_SCRIPT_5 = '''
        element('xml:lang':'en-US', 'blabla')
    '''

    protected DocumentBuilder builder
    protected DomToGroovy converter
    protected File dir = new File("build/generated-groovyxml")

    @Test
    void conversion() {
        convert("test1.xml", "test1.groovy")
        convert("po.xsd", "poSchema.groovy")
        convert("swing.xml", "swing.groovy")
    }

    @Test
    void conversionFormat() {
        checkConversion(TEST_XML_1, EXPECTED_BUILDER_SCRIPT_1)
        checkConversion(TEST_XML_2, EXPECTED_BUILDER_SCRIPT_2)
        checkConversion(TEST_XML_3, EXPECTED_BUILDER_SCRIPT_3)
        checkConversion(TEST_XML_4, EXPECTED_BUILDER_SCRIPT_4)
        checkConversion(TEST_XML_5, EXPECTED_BUILDER_SCRIPT_5)
    }

    @Test
    void multilineTextSurvivesTheRoundTrip() {
        // multi-line content is emitted in a triple-quoted literal, where a backslash
        // still starts an escape sequence and a run of quotes can close the literal early
        ['line one\nC:\\temp\\new',
         'line one\n' + "'" * 3,
         "line one\nends with a quote'",
         "line one\ndon't touch ordinary prose"].each { text ->
            assert roundTrip(text) == text
        }
    }

    @Test
    void apostrophesInProseAreNotEscaped() {
        // the generated source is meant to be read and edited, so only quotes that would
        // run into the closing delimiter are escaped
        assert generate("line one\ndon't split ordinary prose").contains("don't split")
    }

    @Test
    void commentContentCannotCloseTheGeneratedComment() {
        String generated = generateFrom('<a><!-- */ marker() /* --><b>ok</b></a>')
        assert !generated.contains('*/ marker')
        assert generated.contains('* /')
    }

    @Test
    void processingInstructionDataIsQuoted() {
        String generated = generateFrom("<a><?target it's data?><b>ok</b></a>")
        // the apostrophe must not close the literal the data sits in
        assert generated.contains("it\\'s data")
    }

    private String generateFrom(String xml) {
        Document document = builder.parse(new ByteArrayInputStream(xml.bytes))
        StringWriter writer = new StringWriter()
        new DomToGroovy(new PrintWriter(writer)).print(document)
        writer.toString().trim()
    }

    private String roundTrip(String text) {
        String recovered = new GroovyShell().evaluate(
                'def out = new StringWriter()\n' +
                'def mb = new groovy.xml.MarkupBuilder(out)\n' +
                'mb.' + generate(text) + '\n' +
                'out.toString()')
        new XmlSlurper().parseText(recovered).b.text()
    }

    private String generate(String text) {
        Document document = builder.parse(new ByteArrayInputStream("<a><b>${text}</b></a>".bytes))
        StringWriter writer = new StringWriter()
        new DomToGroovy(new PrintWriter(writer)).print(document)
        writer.toString().trim()
    }

    private void checkConversion(String testXml, String expectedScript) throws SAXException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(testXml.getBytes())
        Document document = builder.parse(inputStream)
        StringWriter writer = new StringWriter()
        converter = new DomToGroovy(new PrintWriter(writer))
        converter.print(document)
        StringTestUtil.assertMultilineStringsEqual(expectedScript, writer.toString())
    }

    private void convert(String name, String output) throws Exception {
        Document document = parse(name)
        PrintWriter writer = new PrintWriter(new FileWriter(new File(dir, output)))
        converter = new DomToGroovy(writer)
        writer.println("#!/bin/groovy")
        writer.println()
        writer.println("// generated from " + name)
        writer.println()
        converter.print(document)
        writer.close()
    }

    private Document parse(String name) throws SAXException, IOException {
        URL resource = getClass().getResource(name)
        assertTrue(resource != null, "Could not find resource: " + name)
        return builder.parse(new InputSource(resource.toString()))
    }

    @BeforeEach
    void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.setNamespaceAware(true)
        builder = factory.newDocumentBuilder()
        dir.mkdirs()
    }
}
