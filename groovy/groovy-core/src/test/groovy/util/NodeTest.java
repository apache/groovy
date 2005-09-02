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

package groovy.util;


import groovy.util.GroovyTestCase;
import groovy.util.Node;
import groovy.xml.QName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tests the use of the structured Attribute type
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class NodeTest extends GroovyTestCase {

    public void testSimpleAttribute() {
        Node attribute = new Node(null, "transactional");
        assertEquals("name", "transactional", attribute.name());
        assertEquals("attributes", 0, attribute.attributes().size());
        assertEquals("value", 0, attribute.children().size());
        assertEquals("text", "", attribute.text());

        dump(attribute);
    }

    public void testAttributeWithAttributes() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        Node attribute = new Node(null, "foo", attributes);
        assertEquals("name", "foo", attribute.name());
        assertEquals("attributes", 1, attribute.attributes().size());
        assertEquals("value", 0, attribute.children().size());
        assertEquals("text", "", attribute.text());

        dump(attribute);
    }

    public void testAttributeWithText() {
        Node attribute = new Node(null, "foo", "the text");
        assertEquals("name", "foo", attribute.name());
        assertEquals("attributes", 0, attribute.attributes().size());
        assertEquals("value", 1, attribute.children().size());
        assertEquals("text", "the text", attribute.text());

        dump(attribute);
    }

    public void testAttributeWithAttributesAndChildren() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        List children = new ArrayList();
        children.add(new Node(null, "person", "James"));
        children.add(new Node(null, "person", "Bob"));
        children.add("someText");
        
        Node attribute = new Node(null, "foo", attributes, children);
        assertEquals("name", "foo", attribute.name());
        assertEquals("attributes", 1, attribute.attributes().size());
        assertEquals("value", 3, attribute.children().size());
        assertEquals("text", "someText", attribute.text());

        dump(attribute);
    }

    public void testAttributeWithAttributesAndChildrenWithMixedText() {
        Map attributes = new HashMap();
        attributes.put("a", "xyz");
        
        List children = new ArrayList();
        children.add("someText");
        Node node1 = new Node(null, "person", "James");
        children.add(node1);
        children.add("moreText");
        Node node2 = new Node(null, "person", "Bob");
        children.add(node2);
        children.add("moreText");
        
        Node attribute = new Node(null, "foo", attributes, children);
        assertEquals("name", "foo", attribute.name());
        assertEquals("attributes", 1, attribute.attributes().size());
        assertEquals("value", 5, attribute.children().size());
        assertEquals("text", "someTextmoreTextmoreText", attribute.text());
        
        
        // lets test get
        List list = (List) attribute.get("person");
        assertEquals("Expected list size: " + list, 2, list.size());
        
        assertEquals("Node1", node1, list.get(0));
        assertEquals("Node2", node2, list.get(1));

        dump(attribute);
    }
    
    public void testNavigationUsingQNames() throws Exception {
        QName name1 = new QName("http://something", "foo", "f");
        
        Node node = new Node(null, null, new ArrayList());
        Node child = new Node(null, new QName("http://something", "foo", "f"), new HashMap(), new ArrayList());
        child.attributes().put("cheese", "Edam");
        Node grandChild = new Node(null, new QName("http://something", "bar", "f"), new HashMap(), new ArrayList());
        grandChild.attributes().put("drink", "Beer");
        grandChild.children().add("I am a youngling");
        child.children().add(grandChild);
        
        node.children().add(child);

        // lets look up by QName
        Object value = node.getAt(name1);
        assertTrue("Should return a list: " + value, value instanceof NodeList);
        NodeList list = (NodeList) value;
        assertEquals("Size", 1, list.size());
        
        Node answer = (Node) list.get(0);
        assertNotNull("Node is null!", answer);
        
        System.out.println("Found node: " + answer);
        
        // now lets navigate the list
        NodeList gc = list.getAt(new QName("http://something", "bar"));
        assertEquals("grand children size", 1, gc.size());
        
        System.out.println("Found grandChild: " + gc);
        
        String text= gc.text();
        assertEquals("text of grandchild", "I am a youngling", text);
    }

    protected void dump(Node node) {
        NodePrinter printer = new NodePrinter();
        printer.print(node);
    }

}
