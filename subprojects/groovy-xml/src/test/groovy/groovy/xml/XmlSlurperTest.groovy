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

import groovy.test.GroovyTestCase

import static groovy.xml.XmlUtil.serialize

class XmlSlurperTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlSlurper().parseText(xml) }

    void testWsdl() {
        def wsdl = '''
            <definitions name="AgencyManagementService"
                         xmlns:ns1="http://www.example.org/NS1"
                         xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                         xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                         xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
                         xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                         xmlns="http://schemas.xmlsoap.org/wsdl/">
                <message name="SomeRequest">
                    <part name="parameters" element="ns1:SomeReq" />
                </message>
                <message name="SomeResponse">
                    <part name="result" element="ns1:SomeRsp" />
                </message>
            </definitions>
        '''
        def xml = new XmlSlurper().parseText(wsdl)
        assert xml.message.part.@element*.text().findAll {it =~ /.Req$/}.size() == 1
        assert xml.message.part.findAll { true }.size() == 2
        assert xml.message.part.find { it.name() == 'part' }.name() == 'part'
        assert xml.message.findAll { true }.size() == 2
        assert xml.message.part.lookupNamespace("ns1") == "http://www.example.org/NS1"
        assert xml.message.part.lookupNamespace("") == "http://schemas.xmlsoap.org/wsdl/"
        assert xml.message.part.lookupNamespace("undefinedPrefix") == null
        xml.message.findAll { true }.each { assert it.name() == "message"}
    }

    void testElement() {
        // can't update value directly with XmlSlurper, use replaceNode instead
        // GpathSyntaxTestSupport.checkUpdateElementValue(getRoot)
        GpathSyntaxTestSupport.checkElement(getRoot)
        GpathSyntaxTestSupport.checkFindElement(getRoot)
        GpathSyntaxTestSupport.checkElementTypes(getRoot)
        GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
        GpathSyntaxTestSupport.checkElementTruth(getRoot)
        GpathSyntaxTestSupport.checkCDataText(getRoot)
    }

    void testAttribute() {
        GpathSyntaxTestSupport.checkAttribute(getRoot)
        GpathSyntaxTestSupport.checkAttributes(getRoot)
        GpathSyntaxTestSupport.checkAttributeTruth(getRoot)
    }

    void testNavigation() {
        GpathSyntaxTestSupport.checkChildren(getRoot)
        GpathSyntaxTestSupport.checkParent(getRoot)
        GpathSyntaxTestSupport.checkNestedSizeExpressions(getRoot)
    }

    void testTraversal() {
        TraversalTestSupport.checkDepthFirst(getRoot)
        TraversalTestSupport.checkBreadthFirst(getRoot)
    }

    void testIndices() {
        GpathSyntaxTestSupport.checkNegativeIndices(getRoot)
        GpathSyntaxTestSupport.checkRangeIndex(getRoot)
    }

    void testReplacementsAndAdditions() {
        GpathSyntaxTestSupport.checkReplaceNode(getRoot)
        GpathSyntaxTestSupport.checkReplaceMultipleNodes(getRoot)
        GpathSyntaxTestSupport.checkPlus(getRoot)
    }

    void testMixedMarkup() {
        MixedMarkupTestSupport.checkMixedMarkup(getRoot)
        MixedMarkupTestSupport.checkMixedMarkupText(getRoot)
    }

    void testReplace() {
        def input = "<doc><sec>Hello<p>World</p></sec></doc>"
        def replaceSlurper = new XmlSlurper().parseText(input)
        replaceSlurper.sec.replaceNode { node ->
            t { delegate.mkp.yield node.getBody() }
        }
        def outputSlurper = new StreamingMarkupBuilder()
        String output = outputSlurper.bind { mkp.yield replaceSlurper }
        assert output == "<doc><t>Hello<p>World</p></t></doc>"
    }

    void testNamespacedName() {
        def wsdl = '''
        <definitions name="AgencyManagementService"
            xmlns:ns1="http://www.example.org/NS1"
            xmlns:ns2="http://www.example.org/NS2">
            <ns1:message name="SomeRequest">
                <ns1:part name="parameters" element="SomeReq" />
            </ns1:message>
            <ns2:message name="SomeRequest">
                <ns2:part name="parameters" element="SomeReq" />
            </ns2:message>
        </definitions>
        '''
        def xml = new XmlSlurper().parseText(wsdl)
        assertEquals("message", xml.children()[0].name())
        assertEquals("http://www.example.org/NS1", xml.children()[0].namespaceURI())
        assertEquals("message", xml.children()[1].name())
        assertEquals("http://www.example.org/NS2", xml.children()[1].namespaceURI())
    }

    // GROOVY-4637
    void testNamespacedAttributes() {
        def xml = """
        <RootElement xmlns="http://www.ivan.com/ns1" xmlns:two="http://www.ivan.com/ns2">
            <ChildElement ItemId="FirstItemId" two:ItemId="SecondItemId">Child element data</ChildElement>
        </RootElement>"""
        def root = new XmlSlurper().parseText(xml).declareNamespace(one:"http://www.ivan.com/ns1", two: "http://www.ivan.com/ns2")
        // Default namespace declarations do not apply to attribute names, see https://www.w3.org/TR/xml-names/#defaulting
        assert root.ChildElement.@ItemId == 'FirstItemId'
        assert root.ChildElement.@ItemId[0].namespaceURI() == ''
        assert root.ChildElement.@'two:ItemId' == 'SecondItemId'
        assert root.ChildElement.@'two:ItemId'[0].namespaceURI() == 'http://www.ivan.com/ns2'
    }

    // GROOVY-6255
    void testXmlNamespacedAttributes() {
        def xml = '''
        <appendix version="5.0" xmlns="http://docbook.org/ns/docbook" xmlns:xml="http://www.w3.org/XML/1998/namespace">
            <section xml:id="a"/>
        </appendix>
        '''

        def root = new XmlSlurper().parseText(xml).declareNamespace(docbook: 'http://docbook.org/ns/docbook')

        assert root.section[0].@'xml:id' == 'a'
    }

    // GROOVY-6356
    void testSetAndRemoveAttributesWithNamespace() {
        def xmlSource = '''<bob:root
                xmlns:bob="stuff"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:xlink="http://www.w3.org/1999/xlink">
            <gmi:instrument xlink:title="$INSTRUMENT"/>
        </bob:root>'''

        def bobRoot = new XmlSlurper(false, true).parseText(xmlSource).declareNamespace(bob: 'stuff',
                ns2: 'http://www.example.org/NS2', gmi: "http://www.isotc211.org/2005/gmi")

        def instrument = bobRoot.'gmi:instrument'

        assert serialize(instrument).contains('xlink:title="$INSTRUMENT"')
        instrument[0].'@xlink:title' = 'XXX'
        assert !serialize(instrument).contains('xlink:title="$INSTRUMENT"')
        assert serialize(instrument).contains('xlink:title="XXX"')
        instrument[0].attributes().remove('xlink:title')
        assert !serialize(instrument).contains('xlink:title="XXX"')
    }

    // GROOVY-6356
    void testSetAndRemoveAttributesNamespaceUnaware() {
        def xmlSource = '''<bob:root
                xmlns:bob="stuff"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:xlink="http://www.w3.org/1999/xlink">
            <gmi:instrument xlink:title="$INSTRUMENT"/>
        </bob:root>'''

        def bobRoot = new XmlSlurper(false, false, true).parseText(xmlSource)

        def instrument = bobRoot.children()[0]
        assert instrument.'@xlink:title' == '$INSTRUMENT'
        instrument.'@xlink:title' = 'XXX'
        assert instrument.'@xlink:title' == 'XXX'
        instrument.attributes().remove('xlink:title')
        assert instrument.'@xlink:title' == ''
    }

    // GROOVY-5931
    void testIterableGPathResult() {
        def xml = """
        <RootElement>
            <ChildElement ItemId="FirstItemId">Child element data</ChildElement>
        </RootElement>"""

        def root = new XmlSlurper().parseText(xml)

        assert root instanceof Iterable
        assert root.ChildElement instanceof Iterable
        assert root.ChildElement.@'ItemId' instanceof Iterable

        // execute a DGM on the Iterable
        assert root.first() == 'Child element data'
    }

    // GROOVY-7781
    void testNamespacedAttributesAccessedWithDifferentPrefix() {
        def xml = '''
        <x:root xmlns:x="blah">
            <x:child x:id="1">c</x:child>
        </x:root>
        '''

        def root = new XmlSlurper(false, true).parseText(xml).declareNamespace(t: 'blah')
        assert root.'t:child'.text() == 'c'
        assert root.'t:child'.@'t:id' == '1'
    }

    void testParsePath() {

        def file = File.createTempFile('test','xml')
        file.deleteOnExit()
        file.text = '''
            <definitions name="AgencyManagementService"
                         xmlns:ns1="http://www.example.org/NS1"
                         xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                         xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                         xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
                         xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                         xmlns="http://schemas.xmlsoap.org/wsdl/">
                <message name="SomeRequest">
                    <part name="parameters" element="ns1:SomeReq" />
                </message>
                <message name="SomeResponse">
                    <part name="result" element="ns1:SomeRsp" />
                </message>
            </definitions>
        '''

        def xml = new XmlSlurper().parse(file.toPath())
        assert xml.message.part.@element*.text().findAll {it =~ /.Req$/}.size() == 1
        assert xml.message.part.findAll { true }.size() == 2
        assert xml.message.part.find { it.name() == 'part' }.name() == 'part'
        assert xml.message.findAll { true }.size() == 2
        assert xml.message.part.lookupNamespace("ns1") == "http://www.example.org/NS1"
        assert xml.message.part.lookupNamespace("") == "http://schemas.xmlsoap.org/wsdl/"
        assert xml.message.part.lookupNamespace("undefinedPrefix") == null
        xml.message.findAll { true }.each { assert it.name() == "message"}

    }
}
