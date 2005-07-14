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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents an arbitrary tree node which can be used for structured  metadata which can be any arbitrary XML-like tree.
 * A node can have a name, a value and an optional Map of attributes.
 * Typically the name is a String and a value is either a String or a List of other Nodes.
 * Though the types are extensible to provide a flexible structure. 
 * e.g. you could use a QName as the name which includes a namespace URI and a local name. Or a JMX ObjectName etc.
 * So this class can represent metadata like {foo a=1 b="abc"} or nested metadata like {foo a=1 b="123" { bar x=12 text="hello" }}
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Node {

    private Node parent;
    private Object name;
    private Map attributes;
    private Object value;

    public Node(Node parent, Object name) {
        this(parent, name, Collections.EMPTY_MAP, Collections.EMPTY_LIST);
    }

    public Node(Node parent, Object name, Object value) {
        this(parent, name, Collections.EMPTY_MAP, value);
    }

    public Node(Node parent, Object name, Map attributes) {
        this(parent, name, attributes, Collections.EMPTY_LIST);
    }

    public Node(Node parent, Object name, Map attributes, Object value) {
        this.parent = parent;
        this.name = name;
        this.attributes = attributes;
        this.value = value;
        
        if (parent != null) {
            Object parentValue = parent.value();
            List parentList = null;
            if (parentValue instanceof List) {
                parentList = (List) parentValue;
            }
            else {
                parentList = new ArrayList();
                parentList.add(parentValue);
                parent.setValue(parentList);
            }
            parentList.add(this);
        }
    }

    public String text() {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Collection) {
            Collection coll = (Collection) value;
            String previousText = null;
            StringBuffer buffer = null;
            for (Iterator iter = coll.iterator(); iter.hasNext();) {
                Object child = iter.next();
                if (child instanceof String) {
                    String childText = (String) child;
                    if (previousText == null) {
                        previousText = childText;
                    }
                    else {
                        if (buffer == null) {
                            buffer = new StringBuffer();
                            buffer.append(previousText);
                        }
                        buffer.append(childText);
                    }
                }
            }
            if (buffer != null) {
                return buffer.toString();
            }
            else {
                if (previousText != null) {
                    return previousText;
                }
            }
        }
        return "";
    }

    public Iterator iterator() {
        return children().iterator();
    }
    
    public List children() {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        else if (value instanceof List) {
            return (List) value;
        }
        else {
            // we're probably just a String
            return Collections.singletonList(value);
        }
    }

    public Map attributes() {
        return attributes;
    }

    public Object attribute(Object key) {
        return (attributes != null) ? attributes.get(key) : null;
    }
    
    public Object name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Node parent() {
        return parent;
    }

    public Object get(String key) {
        if (key.charAt(0) == '@') {
            String attributeName = key.substring(1);
            return attributes().get(attributeName);
        }
        else {
            // iterate through list looking for node with name 'key'
            List answer = new ArrayList();
            for (Iterator iter = children().iterator(); iter.hasNext();) {
                Object child = iter.next();
                if (child instanceof Node) {
                    Node childNode = (Node) child;
                    Object childNodeName = childNode.name();
                    if (childNodeName != null && childNodeName.equals(key)) {
                        answer.add(childNode);
                    }
                }
            }
            return answer;
        }
    }

//    public Object get(int idx) {
//        return children().get(idx);
//    }



    /**
     * Provide a collection of all the nodes in the tree
     * using a depth first traversal
     */
    public List depthFirst() {
        List answer = new ArrayList();
        answer.add(this);
        answer.addAll(depthFirstRest());
        return answer;
    }
    
    private  List depthFirstRest() {
        List answer = new ArrayList();
        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext(); ) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                List children = childNode.depthFirstRest();
                answer.add(childNode);
                answer.addAll(children);
            }
        }
        return answer;
    }

    /**
     * Provide a collection of all the nodes in the tree
     * using a bredth first traversal
     */
    public List breadthFirst() {
        List answer = new ArrayList();
        answer.add(this);
        answer.addAll(breadthFirstRest());
        return answer;
    }
    
    private  List breadthFirstRest() {
        List answer = new ArrayList();
        for (Iterator iter = InvokerHelper.asIterator(value); iter.hasNext(); ) {
            Object child = iter.next();
            if (child instanceof Node) {
                Node childNode = (Node) child;
                answer.add(childNode);
            }
        }
        List copy = new ArrayList(answer);
        for (Iterator iter = copy.iterator(); iter.hasNext(); ) {
            Node childNode = (Node) iter.next();
            List children = childNode.breadthFirstRest();
            answer.addAll(children);
        }
        return answer;
    }

    public String toString() {
        return name + "[attributes=" + attributes + "; value=" + value + "]";
    }

    public void print(PrintWriter out) {
        new NodePrinter(out).print(this);
    }
}
