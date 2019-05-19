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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A helper class for creating nested trees of data
 */
public class NodePrinter {

    protected final IndentPrinter out;

    public NodePrinter() {
        this(new IndentPrinter(new PrintWriter(new OutputStreamWriter(System.out))));
    }

    public NodePrinter(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    public NodePrinter(IndentPrinter out) {
        if (out == null) {
            throw new NullPointerException("IndentPrinter 'out' must not be null!");
        }
        this.out = out;
    }

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

    protected void printName(Node node) {
        Object name = node.name();
        if (name != null) {
            out.print(name.toString());
        } else {
            out.print("null");
        }
    }

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
                    out.println(InvokerHelper.toString(value));
                }
            }
            out.decrementIndent();
            out.printIndent();
            out.println("}");
        }
    }


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
                out.print(InvokerHelper.toString(entry.getValue()));
            }
        }
        out.print(")");
    }

}
