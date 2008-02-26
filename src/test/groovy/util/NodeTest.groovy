/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.E
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util

import groovy.xml.QName

/**
 * Tests the use of the structured Attribute type
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Paul King
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
        
        // let's test get
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

        // let's look up by QName
        Object value = node.getAt(name1);
        assertTrue("Should return a list: " + value, value instanceof NodeList);
        NodeList list = (NodeList) value;
        assertEquals("Size", 1, list.size());
        
        Node answer = (Node) list.get(0);
        // check node
        assertNotNull("Node is null!", answer);
        assertEquals("namespace of node", "http://something", answer.name().namespaceURI);
        assertEquals("localPart of node", "foo", answer.name().localPart);

        // check grandchild
        NodeList gc = list.getAt(new QName("http://something", "bar"));
        assertEquals("grand children size", 1, gc.size());
        assertEquals("text of grandchildren", "I am a youngling", gc.text());
        assertEquals("namespace of grandchild[0]", "http://something", gc[0].name().namespaceURI);
    }

    public void testRemove() {
        def xml = '''
        <foo>
            <bar id="1" />
            <bar id="2" />
            <bar id="3" />
            <bar id="4" />
        </foo>'''
        Node foo = new XmlParser().parseText(xml)

        assert foo.bar.size() == 4
        assert foo.children().size() == 4
        assert foo.children().collect{ it.@id.toInteger() } == [1, 2, 3, 4]

        def bar2 = foo.bar.find {it.@id == '2'}
        bar2.parent().remove(bar2)
        assert ! bar2.parent()
        assert foo.bar.size() == 3
        assert foo.children().size() == 3
        assert foo.children().collect{ it.@id.toInteger() } == [1, 3, 4]
        assert ! foo.bar.contains(bar2)

        def bar3 = foo.children().get(1)
        foo.remove(bar3)
        assert ! bar3.parent()
        assert foo.bar.size() == 2
        assert foo.children().size() == 2
        assert foo.children().collect{ it.@id.toInteger() } == [1, 4]
        assert ! foo.bar.contains(bar3)
    }

    public void testMove() {
        def xml = '''
        <foo>
            <bar id="1" />
            <bar id="2" />
            <baz/>
        </foo>'''
        Node foo = new XmlParser().parseText(xml)

        assert foo.bar.size() == 2
        assert foo.children().size() == 3
        assert foo.children().collect{ it.@id?.toInteger() }.grep{it} == [1, 2]

        def bar2 = foo.bar.find {it.@id == '2'}
        bar2.parent().remove(bar2)
        def baz = foo.baz[0]
        assert ! bar2.parent()
        baz.append(bar2)
        assert bar2.parent() == baz
        assert foo.children().collect{ it.@id?.toInteger() }.grep{it} == [1]
        assert foo.baz.bar[0] == bar2
    }

    protected void dump(Node node) {
        NodePrinter printer = new NodePrinter();
        printer.print(node);
    }

}
