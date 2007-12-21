/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.xml;

import groovy.util.BuilderSupport;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A helper class for creating a W3C DOM tree
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
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

    public static Document parse(Reader reader) throws SAXException, IOException, ParserConfigurationException {
        return parse(reader, false, true);
    }

    public static Document parse(Reader reader, boolean validating, boolean namespaceAware)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory();
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(validating);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(reader));
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
                } else {
                    throw new IllegalArgumentException("The value of the xmlns attribute must be a Map of QNames to String URIs");
                }
            } else {
                String valueText = (value != null) ? value.toString() : "";
                element.setAttribute(attrName, valueText);
            }
        }
        return element;
    }

    protected void appendNamespaceAttributes(Element element, Map attributes) {
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("The value of key: " + key + " cannot be null");
            }
            if (key instanceof String) {
                String prefix = (String) key;

                //System.out.println("Creating namespace for prefix: " + prefix + " with value: " + value);

                //element.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xmlns:" + prefix, value.toString());
                element.setAttributeNS("", prefix, value.toString());
            } else if (key instanceof QName) {
                QName qname = (QName) key;
                element.setAttributeNS(qname.getNamespaceURI(), qname.getQualifiedName(), value.toString());
            } else {
                throw new IllegalArgumentException("The key: " + key + " should be an instanceof of " + QName.class);
            }
        }
    }
}
