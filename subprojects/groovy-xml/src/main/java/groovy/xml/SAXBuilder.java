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

import groovy.lang.Tuple3;
import groovy.namespace.QName;
import groovy.util.BuilderSupport;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Map;

/**
 * A builder for generating W3C SAX events.  Use similar to MarkupBuilder.
 */
public class SAXBuilder extends BuilderSupport {

    private final ContentHandler handler;
    private Attributes emptyAttributes = new AttributesImpl();

    /**
     * Creates a builder that forwards markup events to the supplied SAX content handler.
     *
     * @param handler the content handler receiving generated SAX events
     */
    public SAXBuilder(ContentHandler handler) {
        this.handler = handler;
    }

    /**
     * Builder lifecycle callback invoked after a child node has been created.
     * This implementation is a no-op because parent relationships are expressed through SAX events.
     *
     * @param parent the parent node marker
     * @param child the child node marker
     */
    @Override
    protected void setParent(Object parent, Object child) {
    }

    /**
     * Builder lifecycle callback that starts an element with no attributes or body text.
     *
     * @param name the node name
     * @return the node marker used for subsequent callbacks
     */
    @Override
    protected Object createNode(Object name) {
        doStartElement(name, emptyAttributes);
        return name;
    }

    /**
     * Builder lifecycle callback that starts an element and immediately emits text content.
     *
     * @param name the node name
     * @param value the text content to emit
     * @return the node marker used for subsequent callbacks
     */
    @Override
    protected Object createNode(Object name, Object value) {
        doStartElement(name, emptyAttributes);
        doText(value);
        return name;
    }

    private void doText(Object value) {
        try {
            char[] text = value.toString().toCharArray();
            handler.characters(text, 0, text.length);
        } catch (SAXException e) {
            handleException(e);
        }
    }

    /**
     * Builder lifecycle callback that starts an element, emits attributes and optionally emits text content.
     *
     * @param name the node name
     * @param attributeMap the attributes to expose through the SAX event
     * @param text the optional text content to emit after the start element
     * @return the node marker used for subsequent callbacks
     */
    @Override
    protected Object createNode(Object name, Map attributeMap, Object text) {
        AttributesImpl attributes = new AttributesImpl();
        for (Object o : attributeMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object key = entry.getKey();
            Object value = entry.getValue();

            Tuple3<String, String, String> nameInfo = getNameInfo(key);
            String uri = nameInfo.getV1();
            String localName = nameInfo.getV2();
            String qualifiedName = nameInfo.getV3();
            String valueText = (value != null) ? value.toString() : "";

            attributes.addAttribute(uri, localName, qualifiedName, "CDATA", valueText);
        }
        doStartElement(name, attributes);
        if (text != null) {
            doText(text);
        }
        return name;
    }

    /**
     * Emits a SAX {@code startElement} event for the supplied node name.
     * Subclasses may override to customize how start-element events are generated.
     *
     * @param name the node name
     * @param attributes the attributes to include with the start-element event
     */
    protected void doStartElement(Object name, Attributes attributes) {
        Tuple3<String, String, String> nameInfo = getNameInfo(name);
        String uri = nameInfo.getV1();
        String localName = nameInfo.getV2();
        String qualifiedName = nameInfo.getV3();

        try {
            handler.startElement(uri, localName, qualifiedName, attributes);
        } catch (SAXException e) {
            handleException(e);
        }
    }

    /**
     * Builder lifecycle callback invoked when the current node is complete.
     *
     * @param parent the parent node marker
     * @param name the completed node name
     */
    @Override
    protected void nodeCompleted(Object parent, Object name) {
        Tuple3<String, String, String> nameInfo = getNameInfo(name);
        String uri = nameInfo.getV1();
        String localName = nameInfo.getV2();
        String qualifiedName = nameInfo.getV3();

        try {
            handler.endElement(uri, localName, qualifiedName);
        } catch (SAXException e) {
            handleException(e);
        }
    }

    /**
     * Handles checked {@link SAXException}s raised while emitting SAX events.
     * Subclasses may override to translate them differently.
     *
     * @param e the SAX exception to handle
     * @throws RuntimeException by default, wrapping {@code e}
     */
    protected void handleException(SAXException e) {
        throw new RuntimeException(e);
    }

    /**
     * Builder lifecycle callback that starts an element and emits attributes without body text.
     *
     * @param name the node name
     * @param attributes the attributes to expose through the SAX event
     * @return the node marker used for subsequent callbacks
     */
    @Override
    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }


    private Tuple3<String, String, String> getNameInfo(Object name) {
        String uri;
        String localName;
        String qualifiedName;

        if (name instanceof QName qname) {
            uri = qname.getNamespaceURI();
            localName = qname.getLocalPart();
            qualifiedName = qname.getQualifiedName();
        } else {
            uri = "";
            localName = name.toString();
            qualifiedName = localName;
        }

        return new Tuple3<>(uri, localName, qualifiedName);
    }
}
