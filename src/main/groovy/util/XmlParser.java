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
package groovy.util;

import groovy.xml.QName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A helper class for parsing XML into a tree of Node instances for 
 * a simple way of processing XML. This parser does not preserve the
 * XML InfoSet - if thats what you need try using W3C DOM, dom4j, JDOM, XOM etc.
 * This parser ignores comments and processing instructions and converts the
 * XML into a Node for each element in the XML with attributes
 * and child Nodes and Strings. This simple model is sufficient for
 * most simple use cases of processing XML.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class XmlParser implements ContentHandler {

    private StringBuffer bodyText = new StringBuffer();
    private List bodyTexts = new ArrayList();
    private List stack = new ArrayList();
    private Locator locator;
    private XMLReader reader;
    private Node parent;
    private boolean trimWhitespace = true;

    public XmlParser() throws ParserConfigurationException, SAXException {
        this(false, true);
    }

    public XmlParser(boolean validating, boolean namespaceAware) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);

        SAXParser parser = factory.newSAXParser();
        reader = parser.getXMLReader();
    }

    /**
     * Parses the content of the given file as XML turning it into a tree
     * of Nodes
     */
    public Object parse(File file) throws IOException, SAXException {

        InputSource input = new InputSource(new FileInputStream(file));
        input.setSystemId("file://" + file.getAbsolutePath());
        getXMLReader().parse(input);
        return parent;

    }

    /**
     * Parse the content of the specified input source into a tree of Nodes.
     */
    public Object parse(InputSource input) throws IOException, SAXException {
        getXMLReader().parse(input);
        return parent;
    }

    /**
     * Parse the content of the specified input stream into a tree of Nodes.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public Object parse(InputStream input) throws IOException, SAXException {
        InputSource is = new InputSource(input);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified reader into a tree of Nodes.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public Object parse(Reader in) throws IOException, SAXException {
        InputSource is = new InputSource(in);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified URI into a tree of Nodes
     */
    public Object parse(String uri) throws IOException, SAXException {
        InputSource is = new InputSource(uri);
        getXMLReader().parse(is);
        return parent;
    }

    // ContentHandler interface
    //-------------------------------------------------------------------------                    
    public void startDocument() throws SAXException {
        parent = null;
    }

    public void endDocument() throws SAXException {
        bodyTexts.clear();
        stack.clear();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes list)
        throws SAXException {
        bodyTexts.add(bodyText);
        bodyText = new StringBuffer();

        Object name = getElementName(namespaceURI, localName, qName);

        int size = list.getLength();
        Map attributes = new HashMap(size);
        for (int i = 0; i < size; i++) {
            Object attributeName = getElementName(list.getURI(i), list.getLocalName(i), list.getQName(i));
            String value = list.getValue(i);
            attributes.put(attributeName, value);
        }
        parent = new Node(parent, name, attributes, new ArrayList());
        stack.add(parent);
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        String text = bodyText.toString();
        if (trimWhitespace) {
            text = text.trim();
        }
        if (text.length() > 0) {
            parent.children().add(text);
        }

        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
            if (!stack.isEmpty()) {
                parent = (Node) stack.get(stack.size() - 1);
            }
        }
        bodyText = (StringBuffer) bodyTexts.remove(bodyTexts.size() - 1);
    }

    public void characters(char buffer[], int start, int length) throws SAXException {
        bodyText.append(buffer, start, length);
    }

    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char buffer[], int start, int len) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public Locator getDocumentLocator() {
        return locator;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void skippedEntity(String name) throws SAXException {
    }

    // Implementation methods
    //-------------------------------------------------------------------------           
    protected XMLReader getXMLReader() {
        reader.setContentHandler(this);
        return reader;
    }

    protected Object getElementName(String namespaceURI, String localName, String qName) throws SAXException {
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            return name;
        }
        else {
            return new QName(namespaceURI, name, qName);
        }
    }
}
