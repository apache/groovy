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
package groovy.xml;

import groovy.xml.slurpersupport.GPathResult;
import groovy.xml.slurpersupport.NamespaceAwareHashMap;
import groovy.xml.slurpersupport.Node;
import groovy.xml.slurpersupport.NodeChild;
import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Parse XML into a document tree that may be traversed similar to XPath
 * expressions.  For example:
 * <pre class="groovyTestCase">
 * def rootNode = new XmlSlurper().parseText(
 *    '&lt;root&gt;&lt;one a1="uno!"/&gt;&lt;two&gt;Some text!&lt;/two&gt;&lt;/root&gt;' )
 *
 * assert rootNode.name() == 'root'
 * assert rootNode.one[0].@a1 == 'uno!'
 * assert rootNode.two.text() == 'Some text!'
 * rootNode.children().each { assert it.name() in ['one','two'] }
 * </pre>
 * <p>
 * Note that in some cases, a 'selector' expression may not resolve to a
 * single node.  For example:
 * <pre class="groovyTestCase">
 * def rootNode = new XmlSlurper().parseText(
 *    '''&lt;root&gt;
 *         &lt;a&gt;one!&lt;/a&gt;
 *         &lt;a&gt;two!&lt;/a&gt;
 *       &lt;/root&gt;''' )
 *
 * assert rootNode.a.size() == 2
 * rootNode.a.each { assert it.text() in ['one!','two!'] }
 * </pre>
 *
 * @see GPathResult
 */
public class XmlSlurper extends DefaultHandler {
    private final XMLReader reader;
    private Node currentNode = null;
    private final Stack<Node> stack = new Stack<Node>();
    private final StringBuilder charBuffer = new StringBuilder();
    private final Map<String, String> namespaceTagHints = new HashMap<String, String>();
    private boolean keepIgnorableWhitespace = false;
    private boolean namespaceAware = false;

    /**
     * Creates a non-validating and namespace-aware <code>XmlSlurper</code> which does not allow DOCTYPE declarations in documents.
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlSlurper() throws ParserConfigurationException, SAXException {
        this(false, true);
    }
    
    /**
     * Creates a <code>XmlSlurper</code> which does not allow DOCTYPE declarations in documents.
     * 
     * @param validating <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlSlurper(final boolean validating, final boolean namespaceAware) throws ParserConfigurationException, SAXException {
        this(validating, namespaceAware, false);
    }

    /**
     * Creates a <code>XmlSlurper</code>.
     * 
     * @param validating <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     * @param allowDocTypeDeclaration <code>true</code> if the parser should provide support for DOCTYPE declarations; <code>false</code> otherwise.
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlSlurper(final boolean validating, final boolean namespaceAware, boolean allowDocTypeDeclaration) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        factory.setNamespaceAware(namespaceAware);
        this.namespaceAware = namespaceAware;
        factory.setValidating(validating);
        setQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", !allowDocTypeDeclaration);
        reader = factory.newSAXParser().getXMLReader();
    }

    public XmlSlurper(final XMLReader reader) {
        this.reader = reader;
    }

    public XmlSlurper(final SAXParser parser) throws SAXException {
        this(parser.getXMLReader());
    }
    
    private static void setQuietly(SAXParserFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        }
        catch (ParserConfigurationException | SAXNotSupportedException | SAXNotRecognizedException ignored) { }
    }

    /**
     * @deprecated use setKeepIgnorableWhitespace
     * @param keepWhitespace If true then whitespace before elements is kept.
     *                       The default is to discard the whitespace.
     */
    @Deprecated
    public void setKeepWhitespace(boolean keepWhitespace) {
        setKeepIgnorableWhitespace(keepWhitespace);
    }

    /**
     * @param keepIgnorableWhitespace If true then ignorable whitespace (i.e. whitespace before elements) is kept.
     *                       The default is to discard the whitespace.
     */
    public void setKeepIgnorableWhitespace(boolean keepIgnorableWhitespace) {
        this.keepIgnorableWhitespace = keepIgnorableWhitespace;
    }

    /**
     * @return true if ignorable whitespace is kept
     */
    public boolean isKeepIgnorableWhitespace() {
        return keepIgnorableWhitespace;
    }

    /**
     * @return The GPathResult instance created by consuming a stream of SAX events
     *         Note if one of the parse methods has been called then this returns null
     *         Note if this is called more than once all calls after the first will return null
     */
    public GPathResult getDocument() {
        try {
            // xml namespace is always defined
            if (namespaceAware) {
                namespaceTagHints.put("xml", "http://www.w3.org/XML/1998/namespace");
            }
            return new NodeChild(currentNode, null, namespaceTagHints);
        } finally {
            currentNode = null;
        }
    }

    /**
     * Parse the content of the specified input source into a GPathResult object
     *
     * @param input the InputSource to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final InputSource input) throws IOException, SAXException {
        reader.setContentHandler(this);
        reader.parse(input);
        return getDocument();
    }

    /**
     * Parses the content of the given file as XML turning it into a GPathResult object
     *
     * @param file the File to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final File file) throws IOException, SAXException {
        final FileInputStream fis = new FileInputStream(file);
        final InputSource input = new InputSource(fis);
        input.setSystemId("file://" + file.getAbsolutePath());
        try {
            return parse(input);
        } finally {
            fis.close();
        }
    }

    /**
     * Parse the content of the specified input stream into an GPathResult Object.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc. It is up to you to close the InputStream
     * after parsing is complete (if required).
     *
     * @param input the InputStream to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final InputStream input) throws IOException, SAXException {
        return parse(new InputSource(input));
    }

    /**
     * Parse the content of the specified reader into a GPathResult Object.
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc. It is up to you to close the Reader
     * after parsing is complete (if required).
     *
     * @param in the Reader to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final Reader in) throws IOException, SAXException {
        return parse(new InputSource(in));
    }

    /**
     * Parse the content of the specified URI into a GPathResult Object
     *
     * @param uri a String containing the URI to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final String uri) throws IOException, SAXException {
        return parse(new InputSource(uri));
    }

    /**
     * A helper method to parse the given text as XML
     *
     * @param text a String containing XML to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parseText(final String text) throws IOException, SAXException {
        return parse(new StringReader(text));
    }

    // Delegated XMLReader methods
    //------------------------------------------------------------------------

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getDTDHandler()
    */
    public DTDHandler getDTDHandler() {
        return reader.getDTDHandler();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getEntityResolver()
    */
    public EntityResolver getEntityResolver() {
        return reader.getEntityResolver();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getErrorHandler()
    */
    public ErrorHandler getErrorHandler() {
        return reader.getErrorHandler();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
    */
    public boolean getFeature(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return reader.getFeature(uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
    */
    public Object getProperty(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return reader.getProperty(uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
    */
    public void setDTDHandler(final DTDHandler dtdHandler) {
        reader.setDTDHandler(dtdHandler);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
    */
    public void setEntityResolver(final EntityResolver entityResolver) {
        reader.setEntityResolver(entityResolver);
    }

    /**
     * Resolves entities against using the supplied URL as the base for relative URLs
     *
     * @param base The URL used to resolve relative URLs
     */
    public void setEntityBaseUrl(final URL base) {
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(final String publicId, final String systemId) throws IOException {
                return new InputSource(new URL(base, systemId).openStream());
            }
        });
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
    */
    public void setErrorHandler(final ErrorHandler errorHandler) {
        reader.setErrorHandler(errorHandler);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
    */
    public void setFeature(final String uri, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature(uri, value);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
    */
    public void setProperty(final String uri, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setProperty(uri, value);
    }

    // ContentHandler interface
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#startDocument()
    */
    public void startDocument() throws SAXException {
        currentNode = null;
        charBuffer.setLength(0);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
    */
    public void startPrefixMapping(final String tag, final String uri) throws SAXException {
        if (namespaceAware) namespaceTagHints.put(tag, uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
        addCdata();

        final Map<String, String> attributes = new NamespaceAwareHashMap();
        final Map<String, String> attributeNamespaces = new HashMap<String, String>();

        for (int i = atts.getLength() - 1; i != -1; i--) {
            if (atts.getURI(i).length() == 0) {
                attributes.put(atts.getQName(i), atts.getValue(i));
            } else {
                String key = new QName(atts.getURI(i), atts.getLocalName(i)).toString();
                attributes.put(key, atts.getValue(i));
                attributeNamespaces.put(key, atts.getURI(i));
            }
        }

        final Node newElement;

        if (namespaceURI.length() == 0) {
            newElement = new Node(currentNode, qName, attributes, attributeNamespaces, namespaceURI);
        } else {
            newElement = new Node(currentNode, localName, attributes, attributeNamespaces, namespaceURI);
        }

        if (currentNode != null) {
            currentNode.addChild(newElement);
        }

        stack.push(currentNode);
        currentNode = newElement;
    }

    public void ignorableWhitespace(char buffer[], int start, int len) throws SAXException {
        if (keepIgnorableWhitespace) characters(buffer, start, len);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#characters(char[], int, int)
    */
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        charBuffer.append(ch, start, length);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
    */
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
        addCdata();
        Node oldCurrentNode = stack.pop();
        if (oldCurrentNode != null) {
            currentNode = oldCurrentNode;
        }
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#endDocument()
    */
    public void endDocument() throws SAXException {
    }

    private void addCdata() {
        if (charBuffer.length() != 0) {
            //
            // This element is preceded by CDATA if keepIgnorableWhitespace is false (the default setting) and
            // it's not whitespace add it to the body
            // Note that, according to the XML spec, we should preserve the CDATA if it's all whitespace
            // but for the sort of work I'm doing ignoring the whitespace is preferable
            //
            final String cdata = charBuffer.toString();
            charBuffer.setLength(0);
            if (keepIgnorableWhitespace || cdata.trim().length() != 0) {
                currentNode.addChild(cdata);
            }
        }
    }
}
