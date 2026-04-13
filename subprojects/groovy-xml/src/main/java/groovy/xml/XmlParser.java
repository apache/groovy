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
import groovy.util.Node;
import org.apache.groovy.xml.util.JacksonHelper;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static groovy.xml.XmlUtil.setFeatureQuietly;

/**
 * A helper class for parsing XML into a tree of Node instances for a
 * simple way of processing XML. This parser does not preserve the XML
 * InfoSet - if that's what you need try using W3C DOM, dom4j, JDOM, XOM etc.
 * This parser ignores comments and processing instructions and converts
 * the XML into a Node for each element in the XML with attributes
 * and child Nodes and Strings. This simple model is sufficient for
 * most simple use cases of processing XML.
 * <p>
 * Example usage:
 * <pre class="groovyTestCase">
 * import groovy.xml.XmlParser
 * def xml = '&lt;root&gt;&lt;one a1="uno!"/&gt;&lt;two&gt;Some text!&lt;/two&gt;&lt;/root&gt;'
 * def rootNode = new XmlParser().parseText(xml)
 * assert rootNode.name() == 'root'
 * assert rootNode.one[0].@a1 == 'uno!'
 * assert rootNode.two.text() == 'Some text!'
 * rootNode.children().each { assert it.name() in ['one','two'] }
 * </pre>
 */
public class XmlParser implements ContentHandler {

    private StringBuilder bodyText = new StringBuilder();
    private final List<Node> stack = new ArrayList<Node>();
    private Locator locator;
    private XMLReader reader;
    private Node parent;

    private boolean trimWhitespace = false;
    private boolean keepIgnorableWhitespace = false;
    private boolean namespaceAware = true;
    private boolean validating = false;
    private boolean allowDocTypeDeclaration = false;

    /**
     * Creates a non-validating and namespace-aware <code>XmlParser</code> which does not allow DOCTYPE declarations in documents.
     * <p>
     * Parser options can be configured via setters before the first parse call:
     * <pre>
     * // Using Groovy named parameters:
     * def parser = new XmlParser(namespaceAware: false, trimWhitespace: true)
     * </pre>
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlParser() throws ParserConfigurationException, SAXException {
    }

    /**
     * Creates a <code>XmlParser</code> which does not allow DOCTYPE declarations in documents.
     *
     * @param validating <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlParser(boolean validating, boolean namespaceAware) throws ParserConfigurationException, SAXException {
        this(validating, namespaceAware, false);
    }

    /**
     * Creates a <code>XmlParser</code>.
     *
     * @param validating <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     * @param allowDocTypeDeclaration <code>true</code> if the parser should provide support for DOCTYPE declarations; <code>false</code> otherwise.
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException for SAX errors.
     */
    public XmlParser(boolean validating, boolean namespaceAware, boolean allowDocTypeDeclaration) throws ParserConfigurationException, SAXException {
        this.validating = validating;
        this.namespaceAware = namespaceAware;
        this.allowDocTypeDeclaration = allowDocTypeDeclaration;
    }

    public XmlParser(XMLReader reader) {
        this.reader = reader;
    }

    public XmlParser(SAXParser parser) throws SAXException {
        reader = parser.getXMLReader();
    }

    private void initReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", !allowDocTypeDeclaration);
        reader = factory.newSAXParser().getXMLReader();
    }

    private XMLReader ensureReader() {
        try {
            if (reader == null) {
                initReader();
            }
            return reader;
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
     * Returns the current trim whitespace setting.
     *
     * @return true if whitespace will be trimmed
     */
    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    /**
     * Sets the trim whitespace setting value.
     *
     * @param trimWhitespace the desired setting value
     */
    public void setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    /**
     * Returns the current keep ignorable whitespace setting.
     *
     * @return true if ignorable whitespace will be kept (default false)
     */
    public boolean isKeepIgnorableWhitespace() {
        return keepIgnorableWhitespace;
    }

    /**
     * Sets the keep ignorable whitespace setting value.
     *
     * @param keepIgnorableWhitespace the desired new value
     */
    public void setKeepIgnorableWhitespace(boolean keepIgnorableWhitespace) {
        this.keepIgnorableWhitespace = keepIgnorableWhitespace;
    }

    /**
     * Parses the content of the given file as XML turning it into a tree
     * of Nodes.
     *
     * @param file the File containing the XML to be parsed
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(File file) throws IOException, SAXException {
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            InputSource input = new InputSource(stream);
            input.setSystemId("file://" + file.getAbsolutePath());
            getXMLReader().parse(input);
            return parent;
        }
    }

    /**
     * Parses the content of the file at the given path as XML turning it into a tree
     * of Nodes.
     *
     * @param path the path of the File containing the XML to be parsed
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(Path path) throws IOException, SAXException {
        try (InputStream stream = Files.newInputStream(path)) {
            InputSource input = new InputSource(stream);
            input.setSystemId("file://" + path.toAbsolutePath());
            getXMLReader().parse(input);
            return parent;
        }
    }

    /**
     * Parse the content of the specified input source into a tree of Nodes.
     *
     * @param input the InputSource for the XML to parse
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(InputSource input) throws IOException, SAXException {
        getXMLReader().parse(input);
        return parent;
    }

    /**
     * Parse the content of the specified input stream into a tree of Nodes.
     * <p>
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     *
     * @param input an InputStream containing the XML to be parsed
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(InputStream input) throws IOException, SAXException {
        InputSource is = new InputSource(input);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified reader into a tree of Nodes.
     * <p>
     * Note that using this method will not provide the parser with any URI
     * for which to find DTDs etc
     *
     * @param in a Reader to read the XML to be parsed
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(Reader in) throws IOException, SAXException {
        InputSource is = new InputSource(in);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * Parse the content of the specified URI into a tree of Nodes.
     *
     * @param uri a String containing a URI pointing to the XML to be parsed
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parse(String uri) throws IOException, SAXException {
        InputSource is = new InputSource(uri);
        getXMLReader().parse(is);
        return parent;
    }

    /**
     * A helper method to parse the given text as XML.
     *
     * @param text the XML text to parse
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @throws IOException  An IO exception from the parser,
     *                      possibly from a byte stream or character stream
     *                      supplied by the application.
     */
    public Node parseText(String text) throws IOException, SAXException {
        return parse(new StringReader(text));
    }

    /**
     * Parse the content of the specified XML text into a typed object.
     * Requires jackson-databind on the classpath for type conversion.
     * Supports {@code @JsonProperty} and {@code @JsonFormat} annotations.
     *
     * @param type the target type
     * @param text the XML text to parse
     * @param <T>  the target type
     * @return a typed object
     * @throws XmlRuntimeException if parsing or conversion fails, or jackson-databind is absent
     * @since 6.0.0
     */
    public <T> T parseTextAs(Class<T> type, String text) {
        return parseAs(type, new StringReader(text));
    }

    /**
     * Parse XML from a reader into a typed object.
     * Requires jackson-databind on the classpath for type conversion.
     *
     * @param type   the target type
     * @param reader the reader of XML
     * @param <T>    the target type
     * @return a typed object
     * @throws XmlRuntimeException if parsing or conversion fails, or jackson-databind is absent
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, Reader reader) {
        try {
            Node root = parse(reader);
            return JacksonHelper.convertMapToType(root.toMap(), type);
        } catch (IOException | SAXException e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * Parse XML from an input stream into a typed object.
     * Requires jackson-databind on the classpath for type conversion.
     *
     * @param type   the target type
     * @param stream the input stream of XML
     * @param <T>    the target type
     * @return a typed object
     * @throws XmlRuntimeException if parsing or conversion fails, or jackson-databind is absent
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, InputStream stream) {
        try {
            Node root = parse(stream);
            return JacksonHelper.convertMapToType(root.toMap(), type);
        } catch (IOException | SAXException e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * Parse XML from a file into a typed object.
     * Requires jackson-databind on the classpath for type conversion.
     *
     * @param type the target type
     * @param file the XML file
     * @param <T>  the target type
     * @return a typed object
     * @throws IOException if the file cannot be read
     * @throws XmlRuntimeException if parsing or conversion fails, or jackson-databind is absent
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, File file) throws IOException {
        try {
            Node root = parse(file);
            return JacksonHelper.convertMapToType(root.toMap(), type);
        } catch (SAXException e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * Parse XML from a path into a typed object.
     * Requires jackson-databind on the classpath for type conversion.
     *
     * @param type the target type
     * @param path the path to the XML file
     * @param <T>  the target type
     * @return a typed object
     * @throws IOException if the file cannot be read
     * @throws XmlRuntimeException if parsing or conversion fails, or jackson-databind is absent
     * @since 6.0.0
     */
    public <T> T parseAs(Class<T> type, Path path) throws IOException {
        try {
            Node root = parse(path);
            return JacksonHelper.convertMapToType(root.toMap(), type);
        } catch (SAXException e) {
            throw new XmlRuntimeException(e);
        }
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
    @Override
    public void startDocument() throws SAXException {
        parent = null;
    }

    @Override
    public void endDocument() throws SAXException {
        stack.clear();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes list)
            throws SAXException {
        addTextToNode();

        Object nodeName = getElementName(namespaceURI, localName, qName);

        int size = list.getLength();
        Map<Object, String> attributes = new LinkedHashMap<Object, String>(size);
        for (int i = 0; i < size; i++) {
            Object attributeName = getElementName(list.getURI(i), list.getLocalName(i), list.getQName(i));
            String value = list.getValue(i);
            attributes.put(attributeName, value);
        }
        parent = createNode(parent, nodeName, attributes);
        stack.add(parent);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        addTextToNode();

        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
            if (!stack.isEmpty()) {
                parent = stack.get(stack.size() - 1);
            }
        }
    }

    @Override
    public void characters(char[] buffer, int start, int length) throws SAXException {
        bodyText.append(buffer, start, length);
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] buffer, int start, int len) throws SAXException {
        if (keepIgnorableWhitespace) characters(buffer, start, len);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
    }

    public Locator getDocumentLocator() {
        return locator;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected XMLReader getXMLReader() {
        XMLReader r = ensureReader();
        r.setContentHandler(this);
        return r;
    }

    protected void addTextToNode() {
        if (parent == null) {
            // TODO store this on root node? reset bodyText?
            return;
        }
        String text = bodyText.toString();
        if (!trimWhitespace && keepIgnorableWhitespace) {
            parent.children().add(text);
        } else if (!trimWhitespace && !text.trim().isEmpty()) {
            parent.children().add(text);
        } else if (!text.trim().isEmpty()) {
            parent.children().add(text.trim());
        }
        bodyText = new StringBuilder();
    }

    /**
     * Creates a new node with the given parent, name, and attributes. The
     * default implementation returns an instance of
     * <code>groovy.util.Node</code>.
     *
     * @param parent     the parent node, or null if the node being created is the
     *                   root node
     * @param name       an Object representing the name of the node (typically
     *                   an instance of {@link QName})
     * @param attributes a Map of attribute names to attribute values
     * @return a new Node instance representing the current node
     */
    protected Node createNode(Node parent, Object name, Map attributes) {
        return new Node(parent, name, attributes);
    }

    /**
     * Return a name given the namespaceURI, localName and qName.
     *
     * @param namespaceURI the namespace URI
     * @param localName    the local name
     * @param qName        the qualified name
     * @return the newly created representation of the name
     */
    protected Object getElementName(String namespaceURI, String localName, String qName) {
        String name = localName;
        String prefix = "";
        if ((name == null) || (name.isEmpty())) {
            name = qName;
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            return name;
        }
        if (qName != null && !qName.isEmpty() && namespaceAware) {
            int index = qName.lastIndexOf(':');
            if (index > 0) {
                prefix = qName.substring(0, index);
            }
        }
        return new QName(namespaceURI, name, prefix);
    }
}
