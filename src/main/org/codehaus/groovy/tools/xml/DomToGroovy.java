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
package org.codehaus.groovy.tools.xml;

import groovy.util.IndentPrinter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * A SAX handler for turning XML into Groovy scripts
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class DomToGroovy {

    private IndentPrinter out;

    public DomToGroovy(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    public DomToGroovy(IndentPrinter out) {
        this.out = out;
    }


    public void print(Document document) {
        printChildren(document, new HashMap());
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void print(Node node, Map namespaces, boolean endWithComma) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE :
                printElement((Element) node, namespaces, endWithComma);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE :
                printPI((ProcessingInstruction) node, endWithComma);
                break;
            case Node.TEXT_NODE :
                printText((Text) node, endWithComma);
                break;
            case Node.COMMENT_NODE :
                printComment((Comment) node, endWithComma);
                break;
        }
    }

    protected void printElement(Element element, Map namespaces, boolean endWithComma) {
        namespaces = defineNamespaces(element, namespaces);

        element.normalize();
        printIndent();

        String prefix = element.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            print(prefix);
            print(".");
        }
        print(getLocalName(element));

        boolean hasAttributes = printAttributes(element);

        NodeList list = element.getChildNodes();
        int length = list.getLength();
        if (length == 0) {
            printEnd("", endWithComma);
        }
        else {
            Node node = list.item(0);
            if (length == 1 && node instanceof Text) {
                Text textNode = (Text) node;
                String text = getTextNodeData(textNode);
                if (hasAttributes) print(", \"");
                else print("(\"");
                print(text);
                printEnd("\")", endWithComma);
            }
            else if (mixedContent(list)) {
                println(" [");
                out.incrementIndent();
                for (node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
                    boolean useComma = node.getNextSibling() != null;
                    print(node, namespaces, useComma);
                }
                out.decrementIndent();
                printIndent();
                printEnd("]", endWithComma);
            }
            else {
                println(" {");
                out.incrementIndent();
                printChildren(element, namespaces);
                out.decrementIndent();
                printIndent();
                printEnd("}", endWithComma);
            }
        }
    }

    protected void printPI(ProcessingInstruction instruction, boolean endWithComma) {
        printIndent();
        print("xml.pi('");
        print(instruction.getTarget());
        print("', '");
        print(instruction.getData());
        printEnd("');", endWithComma);
    }

    protected void printComment(Comment comment, boolean endWithComma) {
        String text = comment.getData().trim();
        if (text.length() >0) {
            printIndent();
            print("/* ");
            print(text);
            printEnd(" */", endWithComma);
        }
    }

    protected void printText(Text node, boolean endWithComma) {
        String text = getTextNodeData(node);
        if (text.length() > 0) {
            printIndent();
            //            print("xml.append('");
            //            print(text);
            //            println("');");
            print("\"");
            print(text);
            printEnd("\"", endWithComma);
        }
    }

    protected Map defineNamespaces(Element element, Map namespaces) {
        Map answer = null;
        String prefix = element.getPrefix();
        if (prefix != null && prefix.length() > 0 && !namespaces.containsKey(prefix)) {
            answer = new HashMap(namespaces);
            defineNamespace(answer, prefix, element.getNamespaceURI());
        }
        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Attr attribute = (Attr) attributes.item(i);
            prefix = attribute.getPrefix();
            if (prefix != null && prefix.length() > 0 && !namespaces.containsKey(prefix)) {
                if (answer == null) {
                    answer = new HashMap(namespaces);
                }
                defineNamespace(answer, prefix, attribute.getNamespaceURI());
            }
        }
        return (answer != null) ? answer : namespaces;
    }

    protected void defineNamespace(Map namespaces, String prefix, String uri) {
        namespaces.put(prefix, uri);
        if (!prefix.equals("xmlns") && !prefix.equals("xml")) {
            printIndent();
            print(prefix);
            print(" = xmlns.namespace('");
            print(uri);
            println("')");
        }
    }

    protected boolean printAttributes(Element element) {
        boolean hasAttribute = false;

        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        if (length > 0) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                Attr attribute = (Attr) attributes.item(i);
                String prefix = attribute.getPrefix();
                if (prefix != null && prefix.length() > 0) {
                    if (buffer.length() > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(prefix);
                    buffer.append(".");
                    buffer.append(getLocalName(attribute));
                    buffer.append(":'");
                    buffer.append(attribute.getValue());
                    buffer.append("'");
                }
            }

            print("(");
            for (int i = 0; i < length; i++) {
                Attr attribute = (Attr) attributes.item(i);
                String prefix = attribute.getPrefix();
                if (prefix == null || prefix.length() == 0) {
                    if (!hasAttribute) {
                        hasAttribute = true;
                    }
                    else {
                        print(", ");
                    }
                    print(getLocalName(attribute));
                    print(":'");
                    print(attribute.getValue());
                    print("'");
                }
            }
            if (buffer.length() > 0) {
                if (hasAttribute) {
                    print(", ");
                }
                print("xmlns=[");
                print(buffer.toString());
                print("]");
                hasAttribute = true;
            }
        }
        return hasAttribute;
    }

    protected String getTextNodeData(Text node) {
        return node.getData().trim();
    }

    protected boolean mixedContent(NodeList list) {
        boolean hasText = false;
        boolean hasElement = false;
        for (int i = 0, size = list.getLength(); i < size; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                hasElement = true;
            }
            else if (node instanceof Text) {
                String text = getTextNodeData((Text) node);
                if (text.length() > 0) {
                    hasText = true;
                }
            }
        }
        return hasText && hasElement;
    }

    protected void printChildren(Node parent, Map namespaces) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            print(node, namespaces, false);
        }
    }

    protected String getLocalName(Node node) {
        String answer = node.getLocalName();
        if (answer == null) {
            answer = node.getNodeName();
        }
        return answer.trim();
    }

    protected void printEnd(String text, boolean endWithComma) {
        if (endWithComma) {
            print(text);
            println(",");
        }
        else {
            println(text);
        }
    }

    protected void println(String text) {
        out.println(text);
    }

    protected void print(String text) {
        out.print(text);
    }

    protected void printIndent() {
        out.printIndent();
    }
}
