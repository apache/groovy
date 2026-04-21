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

import groovy.namespace.QName;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static groovy.xml.XmlUtil.setFeatureQuietly;

/**
 * Parse XML into a document tree that may be traversed similar to XPath
 * expressions.  For example:
 * <pre class="language-groovy groovyTestCase">
 * import groovy.xml.XmlSlurper
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
 * <pre class="language-groovy groovyTestCase">
 * import groovy.xml.XmlSlurper
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
 * <p>
 * A more realistic example — a book catalog. Given this XML:
 * {@snippet lang="xml" :
 * <catalog>
 *   <book id="b1">
 *     <title>Programming Groovy 3</title>
 *     <author>Venkat Subramaniam</author>
 *     <year>2024</year>
 *   </book>
 *   <book id="b2">
 *     <title>Groovy in Action</title>
 *     <author>Dierk Koenig</author>
 *     <year>2015</year>
 *   </book>
 * </catalog>
 * }
 * the equivalent Groovy to slurp it and navigate the tree:
 * {@snippet lang="groovy" :
 * def catalog = new XmlSlurper().parseText(xml)
 * assert catalog.book.size() == 2
 * assert catalog.book[0].title.text() == 'Programming Groovy 3'
 * catalog.book.findAll { it.year.text().toInteger() >= 2020 }.each { book ->
 *     println "${book.title} by ${book.author}"
 * }
 * }
 *
 * @see GPathResult
 */
public class XmlSlurper extends DefaultHandler {
    private XMLReader reader;
    private Node currentNode = null;
    private final Stack<Node> stack = new Stack<>();
    private final StringBuilder charBuffer = new StringBuilder();
    private final Map<String, String> namespaceTagHints = new HashMap<>();
    private boolean keepIgnorableWhitespace = false;
    private boolean namespaceAware = true;
    private boolean validating = false;
    private boolean allowDocTypeDeclaration = false;

    /**
     * Creates a non-validating and namespace-aware <code>XmlSlurper</code> which does not allow DOCTYPE declarations in documents.
     * <p>
     * Parser options can be configured via setters before the first parse call:
     * <pre>
     * // Using Groovy named parameters:
     * def slurper = new XmlSlurper(namespaceAware: false, keepIgnorableWhitespace: true)
     * </pre>
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlSlurper() throws ParserConfigurationException, SAXException {
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
        this.validating = validating;
        this.namespaceAware = namespaceAware;
        this.allowDocTypeDeclaration = allowDocTypeDeclaration;
    }

    public XmlSlurper(final XMLReader reader) {
        this.reader = reader;
    }

    public XmlSlurper(final SAXParser parser) throws SAXException {
        this(parser.getXMLReader());
    }

    private void initReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", !allowDocTypeDeclaration);
        reader = factory.newSAXParser().getXMLReader();
    }

    private XMLReader getReader() throws ParserConfigurationException, SAXException {
        if (reader == null) {
            initReader();
        }
        return reader;
    }

    private XMLReader ensureReader() {
        try {
            return getReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Failed to initialize XML reader", e);
        }
    }

    private void checkNotInitialized(String property) {
        if (reader != null) {
            throw new IllegalStateException(property + " must be set before parsing");
        }
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
     * Determine if namespace handling is enabled.
     *
     * @return true if namespace handling is enabled
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Enable and/or disable namespace handling.
     * Must be set before the first parse call.
     *
     * @param namespaceAware the new desired value
     * @throws IllegalStateException if called after parsing has started
     * @since 6.0.0
     */
    public void setNamespaceAware(boolean namespaceAware) {
        checkNotInitialized("namespaceAware");
        this.namespaceAware = namespaceAware;
    }

    /**
     * Determine if the parser validates documents.
     *
     * @return true if validation is enabled
     * @since 6.0.0
     */
    public boolean isValidating() {
        return validating;
    }

    /**
     * Enable and/or disable validation.
     * Must be set before the first parse call.
     *
     * @param validating the new desired value
     * @throws IllegalStateException if called after parsing has started
     * @since 6.0.0
     */
    public void setValidating(boolean validating) {
        checkNotInitialized("validating");
        this.validating = validating;
    }

    /**
     * Determine if DOCTYPE declarations are allowed.
     *
     * @return true if DOCTYPE declarations are allowed
     * @since 6.0.0
     */
    public boolean isAllowDocTypeDeclaration() {
        return allowDocTypeDeclaration;
    }

    /**
     * Enable and/or disable DOCTYPE declaration support.
     * Must be set before the first parse call.
     *
     * @param allowDocTypeDeclaration the new desired value
     * @throws IllegalStateException if called after parsing has started
     * @since 6.0.0
     */
    public void setAllowDocTypeDeclaration(boolean allowDocTypeDeclaration) {
        checkNotInitialized("allowDocTypeDeclaration");
        this.allowDocTypeDeclaration = allowDocTypeDeclaration;
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
        try {
            XMLReader r = getReader();
            r.setContentHandler(this);
            r.parse(input);
            return getDocument();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
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
     * Parses the content of the file at the given path as XML turning it into a GPathResult object
     *
     * @param path the path of the File to parse
     * @return An object which supports GPath expressions
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream
     *         or character stream supplied by the application.
     */
    public GPathResult parse(final Path path) throws IOException, SAXException {
       return parse(Files.newInputStream(path));
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
        return ensureReader().getDTDHandler();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getEntityResolver()
    */
    public EntityResolver getEntityResolver() {
        return ensureReader().getEntityResolver();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getErrorHandler()
    */
    public ErrorHandler getErrorHandler() {
        return ensureReader().getErrorHandler();
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
    */
    public boolean getFeature(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return ensureReader().getFeature(uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
    */
    public Object getProperty(final String uri) throws SAXNotRecognizedException, SAXNotSupportedException {
        return ensureReader().getProperty(uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
    */
    public void setDTDHandler(final DTDHandler dtdHandler) {
        ensureReader().setDTDHandler(dtdHandler);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
    */
    public void setEntityResolver(final EntityResolver entityResolver) {
        ensureReader().setEntityResolver(entityResolver);
    }

    /**
     * Resolves entities against using the supplied URL as the base for relative URLs
     *
     * @param base The URL used to resolve relative URLs
     */
    public void setEntityBaseUrl(final URL base) {
        ensureReader().setEntityResolver((publicId, systemId) -> {
            try {
                return new InputSource(base.toURI().resolve(systemId).toURL().openStream());
            } catch (URISyntaxException e) {
                throw new SAXException(e);
            }
        });
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
    */
    public void setErrorHandler(final ErrorHandler errorHandler) {
        ensureReader().setErrorHandler(errorHandler);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
    */
    public void setFeature(final String uri, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        ensureReader().setFeature(uri, value);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
    */
    public void setProperty(final String uri, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        ensureReader().setProperty(uri, value);
    }

    // ContentHandler interface
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#startDocument()
    */
    @Override
    public void startDocument() throws SAXException {
        currentNode = null;
        charBuffer.setLength(0);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
    */
    @Override
    public void startPrefixMapping(final String tag, final String uri) throws SAXException {
        if (namespaceAware) namespaceTagHints.put(tag, uri);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
        addCdata();

        final Map<String, String> attributes = new NamespaceAwareHashMap();
        final Map<String, String> attributeNamespaces = new HashMap<String, String>();

        for (int i = atts.getLength() - 1; i != -1; i--) {
            if (atts.getURI(i).isEmpty()) {
                attributes.put(atts.getQName(i), atts.getValue(i));
            } else {
                String key = new QName(atts.getURI(i), atts.getLocalName(i)).toString();
                attributes.put(key, atts.getValue(i));
                attributeNamespaces.put(key, atts.getURI(i));
            }
        }

        final Node newElement;

        if (namespaceURI.isEmpty()) {
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

    @Override
    public void ignorableWhitespace(char[] buffer, int start, int len) throws SAXException {
        if (keepIgnorableWhitespace) characters(buffer, start, len);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#characters(char[], int, int)
    */
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        charBuffer.append(ch, start, length);
    }

    /* (non-Javadoc)
    * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
    */
    @Override
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
    @Override
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
            if (keepIgnorableWhitespace || !cdata.trim().isEmpty()) {
                currentNode.addChild(cdata);
            }
        }
    }
}
