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
import groovy.util.BuilderSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * A helper class for creating a W3C DOM tree
 */
public class DOMBuilder extends BuilderSupport {

    /**
     * The document currently being populated by this builder.
     */
    Document document;
    /**
     * The document builder used to lazily create {@link #document} when needed.
     */
    DocumentBuilder documentBuilder;

    /**
     * Creates a non-validating, namespace-aware DOM builder.
     *
     * @return a new DOM builder backed by a freshly created {@link DocumentBuilder}
     * @throws ParserConfigurationException if the parser cannot be configured
     */
    public static DOMBuilder newInstance() throws ParserConfigurationException {
        return newInstance(false, true);
    }

    /**
     * Creates a DOM builder using the requested parser settings.
     * The underlying parser does not allow DOCTYPE declarations.
     *
     * @param validating whether the parser should validate source documents
     * @param namespaceAware whether the parser should be namespace aware
     * @return a new DOM builder backed by a freshly created {@link DocumentBuilder}
     * @throws ParserConfigurationException if the parser cannot be configured
     * @see #newInstance(boolean, boolean, boolean)
     */
    public static DOMBuilder newInstance(boolean validating, boolean namespaceAware) throws ParserConfigurationException {
        return newInstance(validating, namespaceAware, false);
    }

    /**
     * Creates a DOM builder using the requested parser settings, including
     * whether DOCTYPE declarations are permitted in parsed documents.
     *
     * @param validating              whether the parser should validate source documents
     * @param namespaceAware          whether the parser should be namespace aware
     * @param allowDocTypeDeclaration whether the parser should allow DOCTYPE declarations
     * @return a new DOM builder backed by a freshly created {@link DocumentBuilder}
     * @throws ParserConfigurationException if the parser cannot be configured
     * @since 6.0.0
     */
    public static DOMBuilder newInstance(boolean validating, boolean namespaceAware, boolean allowDocTypeDeclaration) throws ParserConfigurationException {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory(allowDocTypeDeclaration);
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        return new DOMBuilder(factory.newDocumentBuilder());
    }

    /**
     * Creates a DocumentBuilder and uses it to parse the XML text read from the given reader.
     * A non-validating, namespace aware parser which does not allow DOCTYPE declarations is used.
     *
     * @param reader the reader to read the XML text from
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException                 Any SAX exception, possibly wrapping another exception.
     * @throws IOException                  An IO exception from the parser, possibly from a byte
     *                                      stream or character stream supplied by the application.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies
     *                                      the configuration requested.
     * @see #parse(Reader, boolean, boolean)
     */
    public static Document parse(Reader reader) throws SAXException, IOException, ParserConfigurationException {
        return parse(reader, false, true);
    }

    /**
     * Creates a DocumentBuilder and uses it to parse the XML text read from the given reader, allowing
     * parser validation and namespace awareness to be controlled. Documents are not allowed to contain
     * DOCYTYPE declarations.
     *
     * @param reader         the reader to read the XML text from
     * @param validating     whether to validate the XML
     * @param namespaceAware whether the parser should be namespace aware
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException                 Any SAX exception, possibly wrapping another exception.
     * @throws IOException                  An IO exception from the parser, possibly from a byte
     *                                      stream or character stream supplied by the application.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies
     *                                      the configuration requested.
     */
    public static Document parse(Reader reader, boolean validating, boolean namespaceAware)
            throws SAXException, IOException, ParserConfigurationException {
        return parse(reader, validating, namespaceAware, false);
    }

    /**
     * Creates a DocumentBuilder and uses it to parse the XML text read from the given reader, allowing
     * parser validation, namespace awareness and permission of DOCTYPE declarations to be controlled.
     *
     * @param reader                  the reader to read the XML text from
     * @param validating              whether to validate the XML
     * @param namespaceAware          whether the parser should be namespace aware
     * @param allowDocTypeDeclaration whether the parser should allow DOCTYPE declarations
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException                 Any SAX exception, possibly wrapping another exception.
     * @throws IOException                  An IO exception from the parser, possibly from a byte
     *                                      stream or character stream supplied by the application.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies
     *                                      the configuration requested.
     */
    public static Document parse(Reader reader, boolean validating, boolean namespaceAware, boolean allowDocTypeDeclaration)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory(allowDocTypeDeclaration);
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(reader));
    }

    /**
     * A helper method to parse the given text as XML.
     *
     * @param text the XML text to parse
     * @return the root node of the parsed tree of Nodes
     * @throws SAXException                 Any SAX exception, possibly wrapping another exception.
     * @throws IOException                  An IO exception from the parser, possibly from a byte
     *                                      stream or character stream supplied by the application.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies
     *                                      the configuration requested.
     * @see #parse(Reader)
     */
    public Document parseText(String text) throws SAXException, IOException, ParserConfigurationException {
        return parse(new StringReader(text));
    }

    /**
     * Creates a builder that appends newly created elements to the supplied document.
     *
     * @param document the target document to populate
     */
    public DOMBuilder(Document document) {
        this.document = document;
    }

    /**
     * Creates a builder that lazily creates a backing document from the supplied document builder.
     *
     * @param documentBuilder the document builder used when a new document is required
     */
    public DOMBuilder(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    /**
     * Builder lifecycle callback that attaches a completed child node to its parent node.
     *
     * @param parent the parent {@link Node}
     * @param child the child {@link Node} to append
     */
    @Override
    protected void setParent(Object parent, Object child) {
        Node current = (Node) parent;
        Node node = (Node) child;

        current.appendChild(node);
    }

    /**
     * Builder lifecycle callback that creates an element for the supplied node name.
     *
     * @param name the node name, either a {@link QName} or plain element name
     * @return the created {@link Element}
     */
    @Override
    protected Object createNode(Object name) {
        if (document == null) {
            document = createDocument();
        }
        if (name instanceof QName qname) {
            return document.createElementNS(qname.getNamespaceURI(), qname.getQualifiedName());
        } else {
            return document.createElement(name.toString());
        }
    }

    /**
     * Creates the backing document used by subsequent builder callbacks.
     * Subclasses may override to supply a custom document implementation.
     *
     * @return a new document ready to receive builder output
     * @throws IllegalArgumentException if no {@link DocumentBuilder} is available
     */
    protected Document createDocument() {
        if (documentBuilder == null) {
            throw new IllegalArgumentException("No Document or DOMImplementation available so cannot create Document");
        } else {
            return documentBuilder.newDocument();
        }
    }

    /**
     * Builder lifecycle callback that creates an element and adds text content to it.
     *
     * @param name the node name, either a {@link QName} or plain element name
     * @param value the text value to append
     * @return the created {@link Element}
     */
    @Override
    protected Object createNode(Object name, Object value) {
        Element element = (Element) createNode(name);
        element.appendChild(document.createTextNode(value.toString()));
        return element;
    }

    /**
     * Builder lifecycle callback that creates an element, applies attributes and adds text content.
     *
     * @param name the node name, either a {@link QName} or plain element name
     * @param attributes the attributes to apply to the created element
     * @param value the text value to append
     * @return the created {@link Element}
     */
    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        Element element = (Element) createNode(name, attributes);
        element.appendChild(document.createTextNode(value.toString()));
        return element;
    }

    /**
     * Builder lifecycle callback that creates an element and applies the supplied attributes.
     *
     * @param name the node name, either a {@link QName} or plain element name
     * @param attributes the attributes to apply, including namespace declarations
     * @return the created {@link Element}
     */
    @Override
    protected Object createNode(Object name, Map attributes) {
        Element element = (Element) createNode(name);
        for (Object o : attributes.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String attrName = entry.getKey().toString();
            Object value = entry.getValue();
            if ("xmlns".equals(attrName)) {
                if (value instanceof Map) {
                    appendNamespaceAttributes(element, (Map) value);
                } else if (value instanceof String) {
                    setStringNS(element, "", value);
                } else {
                    throw new IllegalArgumentException("The value of the xmlns attribute must be a Map of QNames to String URIs");
                }
            } else if (attrName.startsWith("xmlns:") && value instanceof String) {
                setStringNS(element, attrName.substring(6), value);
            } else {
                String valueText = (value != null) ? value.toString() : "";
                element.setAttribute(attrName, valueText);
            }
        }
        return element;
    }

    /**
     * Applies namespace declaration attributes from an {@code xmlns} map to the supplied element.
     *
     * @param element the element receiving namespace attributes
     * @param attributes the namespace attributes keyed by prefix or {@link QName}
     * @throws IllegalArgumentException if an entry has a {@code null} value or an unsupported key type
     */
    protected void appendNamespaceAttributes(Element element, Map<Object, Object> attributes) {
        for (Map.Entry entry : attributes.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("The value of key: " + key + " cannot be null");
            }
            if (key instanceof String) {
                setStringNS(element, key, value);
            } else if (key instanceof QName qname) {
                element.setAttributeNS(qname.getNamespaceURI(), qname.getQualifiedName(), value.toString());
            } else {
                throw new IllegalArgumentException("The key: " + key + " should be an instance of " + QName.class);
            }
        }
    }

    private static void setStringNS(Element element, Object key, Object value) {
        String prefix = (String) key;
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "".equals(prefix) ? "xmlns" : "xmlns:" + prefix, value.toString());
    }
}
