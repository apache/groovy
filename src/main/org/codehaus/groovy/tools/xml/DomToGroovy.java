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
package org.codehaus.groovy.tools.xml;

import java.io.PrintWriter;

import org.w3c.dom.*;

/**
 * A SAX handler for turning XML into Groovy scripts
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DomToGroovy {

    private PrintWriter out;
    private int indent;

    public DomToGroovy(PrintWriter out) {
        this.out = out;
    }

    public void print(Document document) {
        printChildren(document);
    }

    public void print(Node node) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE :
                printElement((Element) node);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE :
                printPI((ProcessingInstruction) node);
                break;
            case Node.TEXT_NODE :
                printText((Text) node);
                break;
        }
    }

    public void printElement(Element element) {
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
            println("();");
        }
        else {
            Node node = list.item(0);
            if (length == 1 && node instanceof Text) {
                Text textNode = (Text) node;
                String text = textNode.getData().trim();
                if (hasAttributes) {
                    print(".append('");
                }
                else {
                    print("('");
                }
                print(text);
                println("');");
            }
            else {
                println(" {");
                ++indent;
                printChildren(element);
                --indent;
                printIndent();
                println("}");
            }
        }
    }

    public void printPI(ProcessingInstruction instruction) {
        printIndent();
        print("xml.pi('");
        print(instruction.getTarget());
        print("', '");
        print(instruction.getData());
        println("');");
    }

    public void printText(Text node) {
        String text = node.getData().trim();
        if (text.length() > 0) {
            printIndent();
            print("xml.append('");
            print(text);
            println("');");
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------
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
                        buffer.append(prefix);
                        buffer.append(".");
                        buffer.append(getLocalName(attribute));
                        buffer.append(":'");
                        buffer.append(attribute.getValue());
                        buffer.append("'");
                    }
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
            print(")");
        }
        return hasAttribute;
    }

    protected void printChildren(Node parent) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            print(node);
        }
    }

    protected String getLocalName(Node node) {
        String answer = node.getLocalName();
        if (answer == null) {
            answer = node.getNodeName();
        }
        return answer.trim();
    }

    protected void println(String text) {
        out.print(text);
        out.println();
    }

    protected void print(String text) {
        out.print(text);
    }

    protected void printIndent() {
        for (int i = 0; i < indent; i++) {
            out.print("    ");
        }
    }
}
