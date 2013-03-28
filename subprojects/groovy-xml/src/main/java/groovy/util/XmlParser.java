/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import groovy.xml.FactorySupport;
import groovy.xml.QName;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * <pre>
 * def xml = '&lt;root&gt;&lt;one a1="uno!"/&gt;&lt;two&gt;Some text!&lt;/two&gt;&lt;/root&gt;'
 * def rootNode = new XmlParser().parseText(xml)
 * assert rootNode.name() == 'root'
 * assert rootNode.one[0].@a1 == 'uno!'
 * assert rootNode.two.text() == 'Some text!'
 * rootNode.children().each { assert it.name() in ['one','two'] }
 * </pre>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
 */
public class XmlParser implements ContentHandler {

    private StringBuffer bodyText = new StringBuffer();
    private List<Node> stack = new ArrayList<Node>();
    private Locator locator;
    private XMLReader reader;
    private Node parent;

    private boolean trimWhitespace = true;
    private boolean namespaceAware;

    public XmlParser() throws ParserConfigurationException, SAXException {
        this(false, true);
    }

    public XmlParser(boolean validating, boolean namespaceAware) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        factory.setNamespaceAware(namespaceAware);
        this.namespaceAware = namespaceAware;
        factory.setValidating(validating);
        reader = factory.newSAXParser().getXMLReader();
    }

    public XmlParser(XMLReader reader) {
        this.reader = reader;
    }

    public XmlParser(SAXParser parser) throws SAXException {
        reader = parser.getXMLReader();
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
        InputSource input = new InputSource(new FileInputStream(file));
        input.setSystemId("file://" + file.getAbsolutePath());
        getXMLReader().parse(input);
        return parent;

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
     * @param uri a String containing a uri pointing to the XML to be parsed
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
     * Determine if namespace handling is enabled.
     *
     * @return true if namespace handling is enabled
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Enable and/or disable namespace handling.
     *
     * @param namespaceAware the new desired value
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
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
        reader.setProperty(uri, value);
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

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        addTextToNode();

        if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
            if (!stack.isEmpty()) {
                parent = stack.get(stack.size() - 1);
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
// TODO GROOVY-5360: do we want to capture all whitespace when trim is off? or do we need additional flags?
//        if (!trimWhitespace) characters(buffer, start, len);
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
//        if (!trimWhitespace || text.trim().length() > 0) {
// TODO GROOVY-5360: replace next 4 lines with above commented out line (trimming more similar to XmlSlurper)
        if (trimWhitespace) {
            text = text.trim();
        }
        if (text.length() > 0) {
            parent.children().add(text);
        }
        bodyText = new StringBuffer();
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
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            return name;
        }
        if (qName != null && qName.length() > 0 && namespaceAware) {
            int index = qName.lastIndexOf(":");
            if (index > 0) {
                prefix = qName.substring(0, index);
            }
        }
        return new QName(namespaceURI, name, prefix);
    }
}
