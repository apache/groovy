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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A helper class for creating a W3C DOM tree
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DOMBuilder extends BuilderSupport {

    Document document;
    DocumentBuilder documentBuilder;

    public static DOMBuilder newInstance() throws ParserConfigurationException, FactoryConfigurationError {
    	DocumentBuilder documentBuilder = null;
    	try {
			documentBuilder = (DocumentBuilder) AccessController.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws ParserConfigurationException {
					return DocumentBuilderFactory.newInstance().newDocumentBuilder();
				}
			});
    	} catch (PrivilegedActionException pae) {
    		Exception e = pae.getException();
    		if (e instanceof ParserConfigurationException) {
    			throw (ParserConfigurationException) e;
    		} else {
    			throw new RuntimeException(e);
    		}
    	}
        return new DOMBuilder(documentBuilder);
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
        }
        else {
            return document.createElement(name.toString());
        }
    }

    protected Document createDocument() {
        if (documentBuilder == null) {
            throw new IllegalArgumentException("No Document or DOMImplementation available so cannot create Document");
        }
        else {
            return documentBuilder.newDocument();
        }
    }

    protected Object createNode(Object name, Object value) {
        Element element = (Element) createNode(name);
        element.setNodeValue(value.toString());
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
                }
                else {
                    throw new IllegalArgumentException("The value of the xmlns attribute must be a Map of QNames to String URIs");
                }
            }
            else {
                element.setAttribute(attrName, value.toString());
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
            }
            else if (key instanceof QName) {
                QName qname = (QName) key;
                element.setAttributeNS(qname.getNamespaceURI(), qname.getQualifiedName(), value.toString());
            }
            else {
                throw new IllegalArgumentException("The key: " + key + " should be an instanceof of " + QName.class);
            }
        }
    }
}
