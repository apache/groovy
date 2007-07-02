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
