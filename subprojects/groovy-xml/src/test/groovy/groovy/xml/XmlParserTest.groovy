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

class XmlParserTest extends GroovyTestCase {

    def getRoot = { xml -> new XmlParser().parseText(xml) }

    static bookXml = """
<html xmlns="http://www.w3.org/HTML/1998/html4"
  xmlns:g="http://groovy.codehaus.org/roles"
  xmlns:dc="http://purl.org/dc/elements/1.1/">
  <head><title>GINA Book Review</title></head>
  <body>
<h1><dc:title>Groovy in Action Review</dc:title></h1>
<table>
  <tr align="center">
    <td>Author</td><td>Price</td><td>Pages</td><td>Date</td>
  </tr>
  <tr align="left">
    <td><dc:author>
      <g:developer>Dierk K�nig</g:developer>,
      <g:advocate>Andrew Glover</g:advocate>,
      <g:developer>Paul King</g:developer>,
      <g:projectmanager>Guillaume Laforge</g:projectmanager>,
      <g:advocate>Jon Skeet</g:advocate>,
    </dc:author></td>
    <td>49.99</td>
    <td>696</td>
    <td><dc:date>January, 2007</dc:date></td>
  </tr>
</table>
<p>Review: Great book!</p>
  </body>
</html>
"""

    void testNodePrinter() {
        def text = """
<p>Please read the <a href="index.html">Home</a> page</p>
"""
        def node = new XmlParser(trimWhitespace: true).parseText(text)
        StringWriter sw = new StringWriter()
        new NodePrinter(new PrintWriter(sw)).print(node)
        def result = fixEOLs(sw.toString())
        def expected = '''\
p() {
  Please read the
  a(href:'index.html') {
    Home
  }
  page
}
'''
        assert result == expected
    }

    void testXmlNodePrinter() {
        def text = """
<p>Please read the <a href="index.html">Home</a> page</p>
"""
        def node = new XmlParser(trimWhitespace: true).parseText(text)
        StringWriter sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(node)
        def result = fixEOLs(sw.toString())
        def expected = '''\
<p>
  Please read the
  <a href="index.html">
    Home
  </a>
  page
</p>
'''
        assert result == expected
    }

    void testXmlNodePrinterNamespaces() {
        def html = new XmlParser(trimWhitespace: true).parseText(bookXml)
        StringWriter sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(html)
        def result = fixEOLs(sw.toString())
        def expected = '''\
<html xmlns="http://www.w3.org/HTML/1998/html4">
  <head>
    <title>
      GINA Book Review
    </title>
  </head>
  <body>
    <h1>
      <dc:title xmlns:dc="http://purl.org/dc/elements/1.1/">
        Groovy in Action Review
      </dc:title>
    </h1>
    <table>
      <tr align="center">
        <td>
          Author
        </td>
        <td>
          Price
        </td>
        <td>
          Pages
        </td>
        <td>
          Date
        </td>
      </tr>
      <tr align="left">
        <td>
          <dc:author xmlns:dc="http://purl.org/dc/elements/1.1/">
            <g:developer xmlns:g="http://groovy.codehaus.org/roles">
              Dierk K�nig
            </g:developer>
            ,
            <g:advocate xmlns:g="http://groovy.codehaus.org/roles">
              Andrew Glover
            </g:advocate>
            ,
            <g:developer xmlns:g="http://groovy.codehaus.org/roles">
              Paul King
            </g:developer>
            ,
            <g:projectmanager xmlns:g="http://groovy.codehaus.org/roles">
              Guillaume Laforge
            </g:projectmanager>
            ,
            <g:advocate xmlns:g="http://groovy.codehaus.org/roles">
              Jon Skeet
            </g:advocate>
            ,
          </dc:author>
        </td>
        <td>
          49.99
        </td>
        <td>
          696
        </td>
        <td>
          <dc:date xmlns:dc="http://purl.org/dc/elements/1.1/">
            January, 2007
          </dc:date>
        </td>
      </tr>
    </table>
    <p>
      Review: Great book!
    </p>
  </body>
</html>
'''
        assert result == expected
    }

    void testNamespaceGPath() {
        def anyName = new QName("*", "*")
        def anyHtml = new QName("http://www.w3.org/HTML/1998/html4", "*")
        def anyTitle = new QName("*", "title")
        def html = new XmlParser().parseText(bookXml)

        // string plain style
        def result = html.head.':title'.text()
        assert result == 'GINA Book Review'

        // QName style
        result = html[anyName][anyHtml][anyTitle].text()
        assert result == 'Groovy in Action Review'

        // string wildcard style
        result = html.'*:*'.':*'.'*:title'.text()
        assert result == 'Groovy in Action Review'

        // just for fun, mix the styles
        result = html.'*'.'http://www.w3.org/HTML/1998/html4:*'[anyTitle].text()
        assert result == 'Groovy in Action Review'

        // try traversal
        assert html.'**'['dc:*']*.name()*.localPart == ["title", "author", "date"]
    }

    void testElement() {
        GpathSyntaxTestSupport.checkUpdateElementValue(getRoot)
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

    void testWhitespaceTrimming() {
        def text = '<outer><inner>   Here is some text    </inner></outer>'
        def parser = new XmlParser(trimWhitespace: true)
        def outer = parser.parseText(text)
        assert outer.inner.text() == 'Here is some text'
        parser.setTrimWhitespace false
        outer = parser.parseText(text)
        assert outer.inner.text() == '   Here is some text    '
    }

    void testUpdate() {
        def xml = '<root></root>'
        def parser = new XmlParser()
        def root = parser.parseText(xml)
        def middle = root.appendNode('middle')
        middle.appendNode('child', [attr:'child attr'])
        middle.appendNode('child', 'child text')
        root.appendNode('child', [attr:'child attr'], 'child text')
        root.@attr = 'root attr'
        root.'@other' = 'other attr'

        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(root)
        def result = writer.toString()
        assert result == '''\
<root attr="root attr" other="other attr">
  <middle>
    <child attr="child attr"/>
    <child>
      child text
    </child>
  </middle>
  <child attr="child attr">
    child text
  </child>
</root>
'''
    }

    void testReplaceNode() {
        def xml = '<root><old/></root>'
        def parser = new XmlParser()
        def root = parser.parseText(xml)
        def old = root.old[0]
        def replacement = '<new><child/></new>'
        def replacementNode = parser.parseText(replacement)
        def removed = old.replaceNode(replacementNode)
        assert removed.name() == 'old'

        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(root)
        def result = writer.toString()
        assert result == '''\
<root>
  <new>
    <child/>
  </new>
</root>
'''
    }

    void testXmlParserExtensionPoints() {
        def html = new CustomXmlParser().parseText(bookXml)
        assert html.getClass() == CustomNode
        assert html.name() == new Integer(42)
    }

    void testCloning() {
        def xml = '<root><foo bar="baz"><inner/></foo></root>'
        def parser = new XmlParser()
        def root = parser.parseText(xml)
        def foo = root.foo[0]

        def foo2 = foo.clone()
        foo2.@bar = 'zab'
        foo2.inner[0].value = 'text'
        root.append(foo2)

        def writer = new StringWriter()
        def ip = new IndentPrinter(new PrintWriter(writer), '', false)
        new XmlNodePrinter(ip).print(root)
        def result = writer.toString()
        assert result == '<root><foo bar="baz"><inner/></foo><foo bar="zab"><inner>text</inner></foo></root>'
    }
}
