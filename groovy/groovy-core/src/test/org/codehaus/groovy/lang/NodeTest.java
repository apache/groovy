/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

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

package org.codehaus.groovy.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.GroovyTestCase;

/**
 * Tests the use of the structured Attribute type
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class NodeTest extends GroovyTestCase {

    public void testSimpleAttribute() {
        Node attribute = new Node("transactional");
        assertEquals("name", "transactional", attribute.getName());
        assertEquals("attributes", 0, attribute.getAttributes().size());
        assertEquals("value", 0, attribute.getChildren().size());
        assertEquals("text", "", attribute.getText());
    }

    public void testAttributeWithAttributes() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        Node attribute = new Node("foo", attributes);
        assertEquals("name", "foo", attribute.getName());
        assertEquals("attributes", 1, attribute.getAttributes().size());
        assertEquals("value", 0, attribute.getChildren().size());
        assertEquals("text", "", attribute.getText());
    }

    public void testAttributeWithText() {
        Node attribute = new Node("foo", "the text");
        assertEquals("name", "foo", attribute.getName());
        assertEquals("attributes", 0, attribute.getAttributes().size());
        assertEquals("value", 1, attribute.getChildren().size());
        assertEquals("text", "the text", attribute.getText());
    }

    public void testAttributeWithAttributesAndChildren() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        List children = new ArrayList();
        children.add(new Node("person", "James"));
        children.add(new Node("person", "Bob"));
        children.add("someText");
        
        Node attribute = new Node("foo", attributes, children);
        assertEquals("name", "foo", attribute.getName());
        assertEquals("attributes", 1, attribute.getAttributes().size());
        assertEquals("value", 3, attribute.getChildren().size());
        assertEquals("text", "someText", attribute.getText());
    }

    public void testAttributeWithAttributesAndChildrenWithMixedText() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        List children = new ArrayList();
        children.add("someText");
        children.add(new Node("person", "James"));
        children.add("moreText");
        children.add(new Node("person", "Bob"));
        children.add("moreText");
        
        Node attribute = new Node("foo", attributes, children);
        assertEquals("name", "foo", attribute.getName());
        assertEquals("attributes", 1, attribute.getAttributes().size());
        assertEquals("value", 5, attribute.getChildren().size());
        assertEquals("text", "someTextmoreTextmoreText", attribute.getText());
    }

}
