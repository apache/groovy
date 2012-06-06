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

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A builder for generating W3C SAX events.  Use similar to MarkupBuilder.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class SAXBuilder extends BuilderSupport {

    private ContentHandler handler;
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

    /**
     * @param value
     */
    private void doText(Object value) {
        try {
            char[] text = value.toString().toCharArray();
            handler.characters(text, 0, text.length);
        }
        catch (SAXException e) {
            handleException(e);
        }
    }

    protected Object createNode(Object name, Map attributeMap, Object text) {
        AttributesImpl attributes = new AttributesImpl();
        for (Iterator iter = attributeMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            String uri = "";
            String localName = null;
            String qualifiedName = "";
            String valueText = (value != null) ? value.toString() : "";
            if (key instanceof QName) {
                QName qname = (QName) key;
                uri = qname.getNamespaceURI();
                localName = qname.getLocalPart();
                qualifiedName = qname.getQualifiedName();
            }
            else {
                localName = key.toString();
                qualifiedName = localName;
            }

            attributes.addAttribute(uri, localName, qualifiedName, "CDATA", valueText);
        }
        doStartElement(name, attributes);
        if (text != null) {
            doText(text);
        }
        return name;
    }

    protected void doStartElement(Object name, Attributes attributes) {
        String uri = "";
        String localName = null;
        String qualifiedName = "";
        if (name instanceof QName) {
            QName qname = (QName) name;
            uri = qname.getNamespaceURI();
            localName = qname.getLocalPart();
            qualifiedName = qname.getQualifiedName();
        }
        else {
            localName = name.toString();
            qualifiedName = localName;
        }
        try {
            handler.startElement(uri, localName, qualifiedName, attributes);
        }
        catch (SAXException e) {
            handleException(e);
        }
    }

    protected void nodeCompleted(Object parent, Object name) {
        String uri = "";
        String localName = null;
        String qualifiedName = "";
        if (name instanceof QName) {
            QName qname = (QName) name;
            uri = qname.getNamespaceURI();
            localName = qname.getLocalPart();
            qualifiedName = qname.getQualifiedName();
        }
        else {
            localName = name.toString();
            qualifiedName = localName;
        }
        try {
            handler.endElement(uri, localName, qualifiedName);
        }
        catch (SAXException e) {
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
}
