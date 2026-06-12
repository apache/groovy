/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util;

import org.codehaus.groovy.runtime.FormatHelper;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A helper class for creating nested trees of data
 */
public class NodePrinter {

    /**
     * Writer used for node output.
     */
    protected final IndentPrinter out;

    /**
     * Creates a printer that writes to {@code System.out}.
     */
    public NodePrinter() {
        this(new IndentPrinter(new PrintWriter(new OutputStreamWriter(System.out))));
    }

    /**
     * Creates a printer backed by the supplied writer.
     *
     * @param out the writer to wrap
     */
    public NodePrinter(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    /**
     * Creates a printer backed by the supplied {@link IndentPrinter}.
     *
     * @param out the printer to use
     */
    public NodePrinter(IndentPrinter out) {
        if (out == null) {
            throw new NullPointerException("IndentPrinter 'out' must not be null!");
        }
        this.out = out;
    }

    /**
     * Prints the supplied node tree.
     *
     * @param node the node to print
     */
    public void print(Node node) {
        out.printIndent();
        printName(node);
        Map attributes = node.attributes();
        boolean hasAttributes = attributes != null && !attributes.isEmpty();
        if (hasAttributes) {
            printAttributes(attributes);
        }
        Object value = node.value();
        if (value instanceof List) {
            if (!hasAttributes) {
                out.print("()");
            }
            printList((List) value);
        } else {
            if (value instanceof String) {
                out.print("('");
                out.print((String) value);
                out.println("')");
            } else {
                out.println("()");
            }
        }
        out.flush();
    }

    /**
     * Prints the node name.
     *
     * @param node the node whose name should be printed
     */
    protected void printName(Node node) {
        Object name = node.name();
        if (name != null) {
            out.print(name.toString());
        } else {
            out.print("null");
        }
    }

    /**
     * Prints a node value list.
     *
     * @param list the list to print
     */
    protected void printList(List list) {
        if (list.isEmpty()) {
            out.println("");
        } else {
            out.println(" {");
            out.incrementIndent();
            for (Object value : list) {
                if (value instanceof Node) {
                    print((Node) value);
                } else {
                    out.printIndent();
                    out.println(FormatHelper.toString(value));
                }
            }
            out.decrementIndent();
            out.printIndent();
            out.println("}");
        }
    }


    /**
     * Prints node attributes.
     *
     * @param attributes the attributes to print
     */
    protected void printAttributes(Map attributes) {
        out.print("(");
        boolean first = true;
        for (Object o : attributes.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (first) {
                first = false;
            } else {
                out.print(", ");
            }
            out.print(entry.getKey().toString());
            out.print(":");
            if (entry.getValue() instanceof String) {
                out.print("'" + entry.getValue() + "'");
            } else {
                out.print(FormatHelper.toString(entry.getValue()));
            }
        }
        out.print(")");
    }

}
