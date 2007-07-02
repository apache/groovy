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
 * @author James Strachan
 * @author paulk
 */
public class DomToGroovy {

    private IndentPrinter out;

    public DomToGroovy(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    // TODO allow string quoting delimiter to be specified, e.g. ' vs "
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
            printEnd(hasAttributes ? ")" : "()", endWithComma);
        } else {
            Node node = list.item(0);
            if (length == 1 && node instanceof Text) {
                Text textNode = (Text) node;
                String text = getTextNodeData(textNode);
                if (hasAttributes) print(", '");
                else print("('");
                print(text);
                printEnd("')", endWithComma);
            } else if (mixedContent(list)) {
                println(" [");
                out.incrementIndent();
                for (node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
                    boolean useComma = node.getNextSibling() != null;
                    print(node, namespaces, useComma);
                }
                out.decrementIndent();
                printIndent();
                printEnd("]", endWithComma);
            } else {
                println(") {");
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
            print("'");
            print(text);
            printEnd("'", endWithComma);
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
                printAttributeWithPrefix((Attr) attributes.item(i), buffer);
            }
            print("(");
            for (int i = 0; i < length; i++) {
                hasAttribute = printAttributeWithoutPrefix((Attr) attributes.item(i), hasAttribute);
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

    private void printAttributeWithPrefix(Attr attribute, StringBuffer buffer) {
        String prefix = attribute.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(prefix);
            buffer.append(".");
            buffer.append(getLocalName(attribute));
            buffer.append(":'");
            buffer.append(getAttributeValue(attribute));
            buffer.append("'");
        }
    }

    private String getAttributeValue(Attr attribute) {
        return attribute.getValue();
    }

    private boolean printAttributeWithoutPrefix(Attr attribute, boolean hasAttribute) {
        String prefix = attribute.getPrefix();
        if (prefix == null || prefix.length() == 0) {
            if (!hasAttribute) {
                hasAttribute = true;
            } else {
                print(", ");
            }
            print(getLocalName(attribute));
            print(":'");
            print(getAttributeValue(attribute));
            print("'");
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
            } else if (node instanceof Text) {
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
        } else {
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
