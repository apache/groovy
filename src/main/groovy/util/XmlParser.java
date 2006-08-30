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
import java.io.StringReader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;

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
    private List stack = new ArrayList();
    private Locator locator;
    private XMLReader reader;
    private Node parent;
    private boolean trimWhitespace = true;

    public XmlParser() throws ParserConfigurationException, SAXException {
        this(false, true);
    }

    public XmlParser(boolean validating, boolean namespaceAware) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = null;
        try {
            factory = (SAXParserFactory) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ParserConfigurationException {
                    return SAXParserFactory.newInstance();
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);

        SAXParser parser = factory.newSAXParser();
        reader = parser.getXMLReader();
    }

    public XmlParser(XMLReader reader) {
        this.reader = reader;
    }

    public XmlParser(SAXParser parser) throws SAXException {
        reader = parser.getXMLReader();
    }


    /**
     * Parses the content of the given file as XML turning it into a tree
     * of Nodes
     */
    public Node parse(File file) throws IOException, SAXException {

        InputSource input = new InputSource(new FileInputStream(file));
        input.setSystemId("file://" + file.getAbsolutePath());
        getXMLReader().parse(input);
        return parent;

    }

    /**
     * Parse the content of the specified input source into a tree of Nodes.
     */
    public Node parse(InputSource input) throws IOException, SAXException {
        getXMLReader().parse(input);
        return parent;
    }

    /**
     * Parse the content of the specified input stream into a tree of Nodes.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public Node parse(InputStream input) throws IOException, SAXException {
        InputSource is = new InputSource(input);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified reader into a tree of Nodes.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     */
    public Node parse(Reader in) throws IOException, SAXException {
        InputSource is = new InputSource(in);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified URI into a tree of Nodes
     */
    public Node parse(String uri) throws IOException, SAXException {
        InputSource is = new InputSource(uri);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * A helper method to parse the given text as XML
     * 
     * @param text
     */
    public Node parseText(String text) throws IOException, SAXException {
        return parse(new StringReader(text));
    }
    // Delegated XMLReader methods
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    public DTDHandler getDTDHandler() {
        return this.reader.getDTDHandler();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    public EntityResolver getEntityResolver() {
        return this.reader.getEntityResolver();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    public ErrorHandler getErrorHandler() {
        return this.reader.getErrorHandler();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    public boolean getFeature(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.reader.getFeature(uri);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    public Object getProperty(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.reader.getProperty(uri);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    public void setDTDHandler(final DTDHandler dtdHandler) {
        this.reader.setDTDHandler(dtdHandler);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    public void setEntityResolver(final EntityResolver entityResolver) {
        this.reader.setEntityResolver(entityResolver);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    public void setErrorHandler(final ErrorHandler errorHandler) {
        this.reader.setErrorHandler(errorHandler);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    public void setFeature(final String uri, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.reader.setFeature(uri, value);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(final String uri, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
         this.reader.setProperty(uri, value);
    }

    // ContentHandler interface
    //-------------------------------------------------------------------------                    
    public void startDocument() throws SAXException {
        parent = null;
    }

    public void endDocument() throws SAXException {
        stack.clear();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes list)
        throws SAXException {
        addTextToNode();

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
        addTextToNode();

        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
            if (!stack.isEmpty()) {
                parent = (Node) stack.get(stack.size() - 1);
            }
        }
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

    protected void addTextToNode() {
        String text = bodyText.toString();
        if (trimWhitespace) {
            text = text.trim();
        }
        if (text.length() > 0) {
            parent.children().add(text);
        }
        bodyText = new StringBuffer();
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
