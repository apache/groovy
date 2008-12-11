package groovy.util

import groovy.xml.GpathSyntaxTestSupport
import groovy.xml.MixedMarkupTestSupport
import groovy.xml.TraversalTestSupport

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
      <g:developer>Dierk König</g:developer>,
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
        def node = new XmlParser().parseText(text)
        def StringWriter sw = new StringWriter()
        new NodePrinter(new PrintWriter(sw)).print(node)
        def result = fixEOLs(sw.toString())
        def expected = '''\
p() {
  builder.append(Please read the)
  a(href:'index.html') {
    builder.append(Home)
  }
  builder.append(page)
}
'''
        assert result == expected
    }

    void testXmlNodePrinter() {
        def text = """
<p>Please read the <a href="index.html">Home</a> page</p>
"""
        def node = new XmlParser().parseText(text)
        def StringWriter sw = new StringWriter()
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
        def html = new XmlParser().parseText(bookXml)
        def StringWriter sw = new StringWriter()
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
              Dierk König
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
        def anyName = new groovy.xml.QName("*", "*")
        def anyHtml = new groovy.xml.QName("http://www.w3.org/HTML/1998/html4", "*")
        def anyTitle = new groovy.xml.QName("*", "title")
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
        GpathSyntaxTestSupport.checkElement(getRoot)
        GpathSyntaxTestSupport.checkFindElement(getRoot)
        GpathSyntaxTestSupport.checkElementTypes(getRoot)
        GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
    }

    void testAttribute() {
        GpathSyntaxTestSupport.checkAttribute(getRoot)
        GpathSyntaxTestSupport.checkAttributes(getRoot)
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

    void testMixedMarkup() {
        MixedMarkupTestSupport.checkMixedMarkup(getRoot)
    }

    void testWhitespaceTrimming() {
        def text = '<outer><inner>   Here is some text    </inner></outer>'
        def parser = new XmlParser()
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

    void testXmlParserExtensionPoints() {
        def html = new CustomXmlParser().parseText(bookXml)
        assert html.getClass() == CustomNode
        assert html.name() == new Integer(42)
    }
}
