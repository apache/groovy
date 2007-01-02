/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * A helper class for creating a W3C D
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
