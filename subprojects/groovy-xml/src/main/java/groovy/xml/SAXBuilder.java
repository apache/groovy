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

    public SAXBuilder(ContentHandler handler) {
        this.handler = handler;
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object createNode(Object name) {
        doStartElement(name, emptyAttributes);
        return name;
    }

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

    protected void handleException(SAXException e) {
        throw new RuntimeException(e);
    }

    /* (non-Javadoc)
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object, java.util.Map, java.lang.Object)
     */
    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }


    private Tuple3<String, String, String> getNameInfo(Object name) {
        String uri;
        String localName;
        String qualifiedName;

        if (name instanceof QName) {
            QName qname = (QName) name;
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
