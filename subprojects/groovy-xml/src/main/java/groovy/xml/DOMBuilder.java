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

import groovy.util.BuilderSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class for creating a W3C DOM tree
 */
public class DOMBuilder extends BuilderSupport {

    Document document;
    DocumentBuilder documentBuilder;

    public static DOMBuilder newInstance() throws ParserConfigurationException {
        return newInstance(false, true);
    }

    public static DOMBuilder newInstance(boolean validating, boolean namespaceAware) throws ParserConfigurationException {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory();
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
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory();
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        setQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", !allowDocTypeDeclaration);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(reader));
    }
    
    private static void setQuietly(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        }
        catch (ParserConfigurationException ignored) { }
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

    public DOMBuilder(Document document) {
        this.document = document;
    }

    public DOMBuilder(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    protected void setParent(Object parent, Object child) {
        Node current = (Node) parent;
        Node node = (Node) child;

        current.appendChild(node);
    }

    protected Object createNode(Object name) {
        if (document == null) {
            document = createDocument();
        }
        if (name instanceof QName) {
            QName qname = (QName) name;
            return document.createElementNS(qname.getNamespaceURI(), qname.getQualifiedName());
        } else {
            return document.createElement(name.toString());
        }
    }

    protected Document createDocument() {
        if (documentBuilder == null) {
            throw new IllegalArgumentException("No Document or DOMImplementation available so cannot create Document");
        } else {
            return documentBuilder.newDocument();
        }
    }

    protected Object createNode(Object name, Object value) {
        Element element = (Element) createNode(name);
        element.appendChild(document.createTextNode(value.toString()));
        return element;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        Element element = (Element) createNode(name, attributes);
        element.appendChild(document.createTextNode(value.toString()));
        return element;
    }

    protected Object createNode(Object name, Map attributes) {
        Element element = (Element) createNode(name);
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
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

    protected void appendNamespaceAttributes(Element element, Map<Object, Object> attributes) {
        for (Map.Entry entry : attributes.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("The value of key: " + key + " cannot be null");
            }
            if (key instanceof String) {
                setStringNS(element, key, value);
            } else if (key instanceof QName) {
                QName qname = (QName) key;
                element.setAttributeNS(qname.getNamespaceURI(), qname.getQualifiedName(), value.toString());
            } else {
                throw new IllegalArgumentException("The key: " + key + " should be an instanceof of " + QName.class);
            }
        }
    }

    private static void setStringNS(Element element, Object key, Object value) {
        String prefix = (String) key;
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "".equals(prefix) ? "xmlns" : "xmlns:" + prefix, value.toString());
    }
}
