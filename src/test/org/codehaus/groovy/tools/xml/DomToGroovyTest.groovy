package org.codehaus.groovy.tools.xml

import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException

/**
 * @author James Strachan
 * @author paulk
 * @version $Revision: 4111 $
 */
public class DomToGroovyTest extends GroovyTestCase {
    private static final String LS = System.getProperty("line.separator")
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

    protected DocumentBuilder builder
    protected DomToGroovy converter
    protected File dir = new File("target/generated-groovyxml")

    public void testConversion() throws Exception {
        convert("test1.xml", "test1.groovy")
        convert("po.xsd", "poSchema.groovy")
        convert("swing.xml", "swing.groovy")
    }

    public void testConversionFormat() throws Exception {
        checkConversion(TEST_XML_1, EXPECTED_BUILDER_SCRIPT_1)
        checkConversion(TEST_XML_2, EXPECTED_BUILDER_SCRIPT_2)
        checkConversion(TEST_XML_3, EXPECTED_BUILDER_SCRIPT_3)
        checkConversion(TEST_XML_4, EXPECTED_BUILDER_SCRIPT_4)
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
        assertTrue("Could not find resource: " + name, resource != null)
        return builder.parse(new InputSource(resource.toString()))
    }

    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.setNamespaceAware(true)
        builder = factory.newDocumentBuilder()
        dir.mkdirs()
    }
}
