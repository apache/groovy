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

import groovy.xml.QName;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prints a <code>groovy.util.Node</code> (as used with <code>XmlParser</code>) including all children in XML format.
 * Typical usage:
 * <pre>
 * def xml = '&lt;html&gt;&lt;head&gt;&lt;title&gt;Title&lt;/title&gt;&lt;/head&gt;&lt;body&gt;&lt;h1&gt;Header&lt;/h1&gt;&lt;/body&gt;&lt;/html&gt;'
 * def root = new XmlParser().parseText(xml)
 * new XmlNodePrinter(preserveWhitespace:true).print(root.body[0])
 * </pre>
 * which when run produces this on stdout (or use your own <code>PrintWriter</code> to direct elsewhere):
 * <pre>
 * &lt;body&gt;
 *   &lt;h1&gt;Header&lt;/h1&gt;
 * &lt;/body&gt;
 * </pre>
 *
 * @see groovy.util.NodePrinter
 * @see groovy.xml.XmlUtil#serialize(Node)
 */
public class XmlNodePrinter {

    protected final IndentPrinter out;
    private String quote;
    private boolean namespaceAware = true;
    private boolean preserveWhitespace = false;
    private boolean expandEmptyElements = false;

    public XmlNodePrinter(PrintWriter out) {
        this(out, "  ");
    }

    public XmlNodePrinter(PrintWriter out, String indent) {
        this(out, indent, "\"");
    }

    public XmlNodePrinter(PrintWriter out, String indent, String quote) {
        this(new IndentPrinter(out, indent), quote);
    }

    public XmlNodePrinter(IndentPrinter out) {
        this(out, "\"");
    }

    public XmlNodePrinter(IndentPrinter out, String quote) {
        if (out == null) {
            throw new IllegalArgumentException("Argument 'IndentPrinter out' must not be null!");
        }
        this.out = out;
        this.quote = quote;
    }

    public XmlNodePrinter() {
        this(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    public void print(Node node) {
        print(node, new NamespaceContext());
    }

    /**
     * Check if namespace handling is enabled.
     * Defaults to <code>true</code>.
     *
     * @return true if namespace handling is enabled
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Enable and/or disable namespace handling.
     *
     * @param namespaceAware the new desired value
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Check if whitespace preservation is enabled.
     * Defaults to <code>false</code>.
     *
     * @return true if whitespaces are honoured when printing simple text nodes
     */
    public boolean isPreserveWhitespace() {
        return preserveWhitespace;
    }

    /**
     * Enable and/or disable preservation of whitespace.
     *
     * @param preserveWhitespace the new desired value
     */
    public void setPreserveWhitespace(boolean preserveWhitespace) {
        this.preserveWhitespace = preserveWhitespace;
    }

    /**
     * Get Quote to use when printing attributes.
     *
     * @return the quote character
     */
    public String getQuote() {
        return quote;
    }

    /**
     * Set Quote to use when printing attributes.
     *
     * @param quote the quote character
     */
    public void setQuote(String quote) {
        this.quote = quote;
    }

    /**
     * Whether empty elements are expanded from &lt;tagName/&gt; to &lt;tagName&gt;&lt;/tagName&gt;.
     *
     * @return <code>true</code>, if empty elements will be represented by an opening tag
     *                            followed immediately by a closing tag.
     */
    public boolean isExpandEmptyElements() {
        return expandEmptyElements;
    }

    /**
     * Whether empty elements are expanded from &lt;tagName/&gt; to &lt;tagName&gt;&lt;/tagName&gt;.
     *
     * @param expandEmptyElements if <code>true</code>, empty
     *                            elements will be represented by an opening tag
     *                            followed immediately by a closing tag.
     *                            Defaults to <code>false</code>.
     */
    public void setExpandEmptyElements(boolean expandEmptyElements) {
        this.expandEmptyElements = expandEmptyElements;
    }

    protected void print(Node node, NamespaceContext ctx) {
        /*
         * Handle empty elements like '<br/>', '<img/> or '<hr noshade="noshade"/>.
         */
        if (isEmptyElement(node)) {
            printLineBegin();
            out.print("<");
            out.print(getName(node));
            if (ctx != null) {
                printNamespace(node, ctx);
            }
            printNameAttributes(node.attributes(), ctx);
            if (expandEmptyElements) {
                out.print("></");
                out.print(getName(node));
                out.print(">");
            } else {
                out.print("/>");
            }
            printLineEnd();
            out.flush();
            return;
        }

        /*
         * Hook for extra processing, e.g. GSP tag element!
         */
        if (printSpecialNode(node)) {
            out.flush();
            return;
        }

        /*
         * Handle normal element like <html> ... </html>.
         */
        Object value = node.value();
        if (value instanceof List) {
            printName(node, ctx, true, isListOfSimple((List) value));
            printList((List) value, ctx);
            printName(node, ctx, false, isListOfSimple((List) value));
            out.flush();
            return;
        }

        // treat as simple type - probably a String
        printName(node, ctx, true, preserveWhitespace);
        printSimpleItemWithIndent(value);
        printName(node, ctx, false, preserveWhitespace);
        out.flush();
    }

    private boolean isListOfSimple(List value) {
        for (Object p : value) {
            if (p instanceof Node) return false;
        }
        return preserveWhitespace;
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
        out.println();
        out.flush();
    }

    protected void printList(List list, NamespaceContext ctx) {
        out.incrementIndent();
        for (Object value : list) {
            NamespaceContext context = new NamespaceContext(ctx);
            /*
             * If the current value is a node, recurse into that node.
             */
            if (value instanceof Node) {
                print((Node) value, context);
                continue;
            }
            printSimpleItem(value);
        }
        out.decrementIndent();
    }

    protected void printSimpleItem(Object value) {
        if (!preserveWhitespace) printLineBegin();
        printEscaped(InvokerHelper.toString(value), false);
        if (!preserveWhitespace) printLineEnd();
    }

    protected void printName(Node node, NamespaceContext ctx, boolean begin, boolean preserve) {
        if (node == null) {
            throw new NullPointerException("Node must not be null.");
        }
        Object name = node.name();
        if (name == null) {
            throw new NullPointerException("Name must not be null.");
        }
        if (!preserve || begin) printLineBegin();
        out.print("<");
        if (!begin) {
            out.print("/");
        }
        out.print(getName(node));
        if (ctx != null) {
            printNamespace(node, ctx);
        }
        if (begin) {
            printNameAttributes(node.attributes(), ctx);
        }
        out.print(">");
        if (!preserve || !begin) printLineEnd();
    }

    protected boolean printSpecialNode(Node node) {
        return false;
    }

    protected void printNamespace(Object object, NamespaceContext ctx) {
        if (namespaceAware) {
            if (object instanceof Node) {
                printNamespace(((Node) object).name(), ctx);
            } else if (object instanceof QName) {
                QName qname = (QName) object;
                String namespaceUri = qname.getNamespaceURI();
                if (namespaceUri != null) {
                    String prefix = qname.getPrefix();
                    if (!ctx.isPrefixRegistered(prefix, namespaceUri)) {
                        ctx.registerNamespacePrefix(prefix, namespaceUri);
                        out.print(" ");
                        out.print("xmlns");
                        if (prefix.length() > 0) {
                            out.print(":");
                            out.print(prefix);
                        }
                        out.print("=" + quote);
                        out.print(namespaceUri);
                        out.print(quote);
                    }
                }
            }
        }
    }

    protected void printNameAttributes(Map attributes, NamespaceContext ctx) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        for (Object p : attributes.entrySet()) {
            Map.Entry entry = (Map.Entry) p;
            out.print(" ");
            out.print(getName(entry.getKey()));
            out.print("=");
            Object value = entry.getValue();
            out.print(quote);
            if (value instanceof String) {
                printEscaped((String) value, true);
            } else {
                printEscaped(InvokerHelper.toString(value), true);
            }
            out.print(quote);
            printNamespace(entry.getKey(), ctx);
        }
    }

    private static boolean isEmptyElement(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null!");
        }
        if (!node.children().isEmpty()) {
            return false;
        }
        return node.text().length() == 0;
    }

    private String getName(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof QName) {
            QName qname = (QName) object;
            if (!namespaceAware) {
                return qname.getLocalPart();
            }
            return qname.getQualifiedName();
        } else if (object instanceof Node) {
            Object name = ((Node) object).name();
            return getName(name);
        }
        return object.toString();
    }

    private void printSimpleItemWithIndent(Object value) {
        if (!preserveWhitespace) out.incrementIndent();
        printSimpleItem(value);
        if (!preserveWhitespace) out.decrementIndent();
    }

    // For ' and " we only escape if needed. As far as XML is concerned,
    // we could always escape if we wanted to.
    private void printEscaped(String s, boolean isAttributeValue) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    out.print("&lt;");
                    break;
                case '>':
                    out.print("&gt;");
                    break;
                case '&':
                    out.print("&amp;");
                    break;
                case '\'':
                    if (isAttributeValue && quote.equals("'"))
                        out.print("&apos;");
                    else
                        out.print(c);
                    break;
                case '"':
                    if (isAttributeValue && quote.equals("\""))
                        out.print("&quot;");
                    else
                        out.print(c);
                    break;
                case '\n':
                    if (isAttributeValue)
                        out.print("&#10;");
                    else
                        out.print(c);
                    break;
                case '\r':
                    if (isAttributeValue)
                        out.print("&#13;");
                    else
                        out.print(c);
                    break;
                default:
                    out.print(c);
            }
        }
    }

    protected static class NamespaceContext {
        private final Map<String, String> namespaceMap;

        public NamespaceContext() {
            namespaceMap = new HashMap<>();
        }

        public NamespaceContext(NamespaceContext context) {
            this();
            namespaceMap.putAll(context.namespaceMap);
        }

        public boolean isPrefixRegistered(String prefix, String uri) {
            return namespaceMap.containsKey(prefix) && namespaceMap.get(prefix).equals(uri);
        }

        public void registerNamespacePrefix(String prefix, String uri) {
            if (!isPrefixRegistered(prefix, uri)) {
                namespaceMap.put(prefix, uri);
            }
        }

        public String getNamespace(String prefix) {
            Object uri = namespaceMap.get(prefix);
            return (uri == null) ? null : uri.toString();
        }
    }
}
