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

import groovy.xml.QName;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Prints a node with all childs in XML format.
 * 
 * @see groovy.util.NodePrinter
 * @author Christian Stein
 */
public class XmlNodePrinter {

    protected final IndentPrinter out;
    private final String quote;

    public XmlNodePrinter() {
        this(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    public XmlNodePrinter(PrintWriter out) {
        this(out, "  ");
    }

    public XmlNodePrinter(PrintWriter out, String indent) {
        this(out, indent, "\"");
    }

    public XmlNodePrinter(PrintWriter out, String indent, String quote) {
        this(new IndentPrinter(out, indent), quote);
    }

    public XmlNodePrinter(IndentPrinter out, String quote) {
        if (out == null) {
            throw new IllegalArgumentException("Argument 'IndentPrinter out' must not be null!");
        }
        this.out = out;
        this.quote = quote;
    }

    public String getNameOfNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null!");
        }
        Object name = node.name();
        if (name instanceof QName) {
            QName qname = (QName) name;
            return /* qname.getPrefix() + ":" + */qname.getLocalPart();
        }
        return name.toString();
    }

    public boolean isEmptyElement(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null!");
        }
        if (!node.children().isEmpty()) {
            return false;
        }
        return node.text().length() == 0;
    }

    public void print(Node node) {
        /*
         * Handle empty elements like '<br/>', '<img/> or '<hr noshade="noshade"/>.
         */
        if (isEmptyElement(node)) {
            // System.err.println("empty-dead");
            printLineBegin();
            out.print("<");
            out.print(getNameOfNode(node));
            printNameAttributes(node.attributes());
            out.print("/>");
            printLineEnd(); // "node named '" + node.name() + "'"
            out.flush();
            return;
        }

        /*
         * Handle GSP tag element!
         */
        if (printSpecialNode(node)) {
            // System.err.println("special-dead");
            out.flush();
            return;
        }

        /*
         * Handle normal element like <html> ... </html>.
         */
        Object value = node.value();
        if (value instanceof List) {
            printName(node, true);
            printList((List) value);
            printName(node, false);
            out.flush();
            return;
        }

        /*
         * Still here?!
         */
        throw new RuntimeException("Unsupported node value: " + node.value());
    }

    protected void printLineBegin() {
        out.printIndent();
    }

    protected void printLineEnd() {
        printLineEnd(null);
    }

    protected void printLineEnd(String comment) {
        if (comment != null) {
            out.print(" <!-- ");
            out.print(comment);
            out.print(" -->");
        }
        out.print("\n");
    }

    protected void printList(List list) {
        out.incrementIndent();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object value = iter.next();
            /*
             * If the current value is a node, recurse into that node.
             */
            if (value instanceof Node) {
                print((Node) value);
                continue;
            }
            /*
             * Print out "simple" text nodes.
             */
            printLineBegin();
            out.print(InvokerHelper.toString(value));
            printLineEnd();
        }
        out.decrementIndent();
    }

    protected void printName(Node node, boolean begin) {
        if (node == null) {
            throw new NullPointerException("Node must not be null.");
        }
        Object name = node.name();
        if (name == null) {
            throw new NullPointerException("Name must not be null.");
        }
        printLineBegin();
        out.print("<");
        if (!begin) {
            out.print("/");
        }
        out.print(getNameOfNode(node));
        if (begin) {
            printNameAttributes(node.attributes());
        }
        out.print(">");
        printLineEnd();
    }

    protected void printNameAttributes(Map attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.print(" ");
            out.print(entry.getKey().toString());
            out.print("=");
            Object value = entry.getValue();
            out.print(quote);
            if (value instanceof String) {
                out.print((String) value);
            } else {
                out.print(InvokerHelper.toString(value));
            }
            out.print(quote);
        }
    }

    protected boolean printSpecialNode(Node node) {
        return false;
    }

}
