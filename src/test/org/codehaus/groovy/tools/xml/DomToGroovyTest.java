/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.tools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author James Strachan
 * @author paulk
 * @version $Revision$
 */
public class DomToGroovyTest extends TestCase {
    private static final String LS = System.getProperty("line.separator");
    private static final String TEST_XML_1 =
            "<a href='http://groovy.codehaus.org'>Groovy</a>";
    private static final String TEST_XML_2 =
            "<project name='testProject'><target name='testTarget'><echo>message</echo><echo/></target></project>";
    private static final String TEST_XML_3 =
            "<?xml version=\"1.0\"?>\n" +
                    "<!-- this example demonstrates using markup to specify a rich user interface -->\n" +
                    "<frame text=\"My Window\" size=\"[300,300]\">\n" +
                    "  <label text=\"Save changes\" bounds=\"[10,10,290,30]\"/>\n" +
                    "  <panel bounds=\"[10,40,290,290]\">\n" +
                    "    <button text=\"OK\" action=\"save()\"/>\n" +
                    "    <button text=\"Cancel\" action=\"close()\"/>\n" +
                    "  </panel>\n" +
                    "</frame>";
    private static final String TEST_XML_4 =
            "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                    " <xsd:simpleType name=\"SKU\">\n" +
                    "  <xsd:restriction base=\"xsd:string\">\n" +
                    "   <xsd:pattern value=\"\\d{3}-[A-Z]{2}\"/>\n" +
                    "  </xsd:restriction>\n" +
                    " </xsd:simpleType>\n" +
                    "</xsd:schema>";
    private static final String EXPECTED_BUILDER_SCRIPT_1 =
            "a(href:'http://groovy.codehaus.org', 'Groovy')";
    private static final String EXPECTED_BUILDER_SCRIPT_2 =
            "project(name:'testProject') {" + LS +
            "  target(name:'testTarget') {" + LS +
            "    echo('message')" + LS +
            "    echo()" + LS +
            "  }" + LS +
            "}";
    private static final String EXPECTED_BUILDER_SCRIPT_3 =
            "/* this example demonstrates using markup to specify a rich user interface */" + LS +
            "frame(size:'[300,300]', text:'My Window') {" + LS +
            "  label(bounds:'[10,10,290,30]', text:'Save changes')" + LS +
            "  panel(bounds:'[10,40,290,290]') {" + LS +
            "    button(action:'save()', text:'OK')" + LS +
            "    button(action:'close()', text:'Cancel')" + LS +
            "  }" + LS +
            "}";
    private static final String EXPECTED_BUILDER_SCRIPT_4 =
            "xsd = xmlns.namespace('http://www.w3.org/2001/XMLSchema')" + LS +
                    "xsd.schema(xmlns=[xmlns.xsd:'http://www.w3.org/2001/XMLSchema']) {" + LS +
                    "  xsd.simpleType(name:'SKU') {" + LS +
                    "    xsd.restriction(base:'xsd:string') {" + LS +
                    "      xsd.pattern(value:'\\\\d{3}-[A-Z]{2}')" + LS +
                    "    }" + LS +
                    "  }" + LS +
                    "}";

    protected DocumentBuilder builder;
    protected DomToGroovy converter;
    protected File dir = new File("target/generated-groovyxml");

    public void testConversion() throws Exception {
        convert("test1.xml", "test1.groovy");
        convert("po.xsd", "poSchema.groovy");
        convert("swing.xml", "swing.groovy");
    }

    public void testConversionFormat() throws Exception {
        checkConversion(TEST_XML_1, EXPECTED_BUILDER_SCRIPT_1);
        checkConversion(TEST_XML_2, EXPECTED_BUILDER_SCRIPT_2);
        checkConversion(TEST_XML_3, EXPECTED_BUILDER_SCRIPT_3);
        // TODO make work for namespaces and better escape special symbols in strings, e.g. '\'
//        checkConversion(TEST_XML_4, EXPECTED_BUILDER_SCRIPT_4);
    }

    private void checkConversion(String testXml, String expectedScript) throws SAXException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(testXml.getBytes());
        Document document = builder.parse(inputStream);
        StringWriter writer = new StringWriter();
        converter = new DomToGroovy(new PrintWriter(writer));
        converter.print(document);
        assertEquals(expectedScript, writer.toString().trim());
        System.out.println(writer.toString().trim());
    }

    private void convert(String name, String output) throws Exception {
        Document document = parse(name);

        PrintWriter writer = new PrintWriter(new FileWriter(new File(dir, output)));
        converter = new DomToGroovy(writer);

        writer.println("#!/bin/groovy");
        writer.println();
        writer.println("// generated from " + name);
        writer.println();
        converter.print(document);
        writer.close();
    }

    private Document parse(String name) throws SAXException, IOException {
        URL resource = getClass().getResource(name);
        assertTrue("Could not find resource: " + name, resource != null);
        return builder.parse(new InputSource(resource.toString()));
    }

    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        dir.mkdirs();
    }

}
