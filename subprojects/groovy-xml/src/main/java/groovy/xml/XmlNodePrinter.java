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
package groovy.xml;

import groovy.namespace.QName;
import groovy.util.IndentPrinter;
import groovy.util.Node;
import org.codehaus.groovy.runtime.FormatHelper;

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

    /**
     * Printer receiving serialized XML output.
     */
    protected final IndentPrinter out;
    private String quote;
    private boolean namespaceAware = true;
    private boolean preserveWhitespace = false;
    private boolean expandEmptyElements = false;

    /**
     * Creates a printer that writes to the supplied writer using two-space indentation and double quotes.
     *
     * @param out the writer receiving the serialized XML
     */
    public XmlNodePrinter(PrintWriter out) {
        this(out, "  ");
    }

    /**
     * Creates a printer that writes to the supplied writer using the supplied indentation string.
     *
     * @param out the writer receiving the serialized XML
     * @param indent the indentation unit to use
     */
    public XmlNodePrinter(PrintWriter out, String indent) {
        this(out, indent, "\"");
    }

    /**
     * Creates a printer that writes to the supplied writer using the supplied indentation and attribute quote.
     *
     * @param out the writer receiving the serialized XML
     * @param indent the indentation unit to use
     * @param quote the quote string to use around attribute values
     */
    public XmlNodePrinter(PrintWriter out, String indent, String quote) {
        this(new IndentPrinter(out, indent), quote);
    }

    /**
     * Creates a printer that writes to the supplied indent printer using double quotes for attributes.
     *
     * @param out the indent printer receiving the serialized XML
     */
    public XmlNodePrinter(IndentPrinter out) {
        this(out, "\"");
    }

    /**
     * Creates a printer that writes to the supplied indent printer using the supplied attribute quote.
     *
     * @param out the indent printer receiving the serialized XML
     * @param quote the quote string to use around attribute values
     */
    public XmlNodePrinter(IndentPrinter out, String quote) {
        if (out == null) {
            throw new IllegalArgumentException("Argument 'IndentPrinter out' must not be null!");
        }
        this.out = out;
        this.quote = quote;
    }

    /**
     * Creates a printer that writes to standard output using default formatting.
     */
    public XmlNodePrinter() {
        this(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    /**
     * Prints the supplied node and its descendants.
     *
     * @param node the root node to serialize
     */
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

    /**
     * Prints a node using the supplied namespace context.
     * Subclasses may override to customize node serialization while reusing the helper methods in this class.
     *
     * @param node the node to serialize
     * @param ctx the namespace context active for this node
     */
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

    /**
     * Prints any indentation required before the current line.
     */
    protected void printLineBegin() {
        out.printIndent();
    }

    /**
     * Terminates the current line without a trailing comment.
     */
    protected void printLineEnd() {
        printLineEnd(null);
    }

    /**
     * Terminates the current line and optionally appends an XML comment.
     *
     * @param comment the comment text to append, or {@code null} for none
     */
    protected void printLineEnd(String comment) {
        if (comment != null) {
            out.print(" <!-- ");
            out.print(comment);
            out.print(" -->");
        }
        out.println();
        out.flush();
    }

    /**
     * Prints the contents of a node value list.
     *
     * @param list the node contents to print
     * @param ctx the namespace context to propagate to child nodes
     */
    protected void printList(List list, NamespaceContext ctx) {
        out.incrementIndent();
        for (Object value : list) {
            /*
             * If the current value is a node, recurse into that node.
             */
            if (value instanceof Node) {
                print((Node) value, new NamespaceContext(ctx));
                continue;
            }
            printSimpleItem(value);
        }
        out.decrementIndent();
    }

    /**
     * Prints a simple non-node value, escaping it as element content.
     *
     * @param value the value to print
     */
    protected void printSimpleItem(Object value) {
        if (!preserveWhitespace) printLineBegin();
        printEscaped(FormatHelper.toString(value), false);
        if (!preserveWhitespace) printLineEnd();
    }

    /**
     * Prints an opening or closing tag for the supplied node.
     *
     * @param node the node whose name and attributes should be printed
     * @param ctx the namespace context active for this node
     * @param begin {@code true} to print the opening tag, {@code false} for the closing tag
     * @param preserve whether surrounding whitespace should be preserved
     */
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

    /**
     * Hook for subclasses to intercept node printing.
     *
     * @param node the node about to be printed
     * @return {@code true} if the node was handled completely and normal printing should stop
     */
    protected boolean printSpecialNode(Node node) {
        return false;
    }

    /**
     * Prints any namespace declaration required by the supplied node or qualified name.
     *
     * @param object the node or qualified name whose namespace should be declared
     * @param ctx the namespace context tracking declarations already emitted
     */
    protected void printNamespace(Object object, NamespaceContext ctx) {
        if (namespaceAware) {
            if (object instanceof Node) {
                printNamespace(((Node) object).name(), ctx);
            } else if (object instanceof QName qname) {
                String namespaceUri = qname.getNamespaceURI();
                if (namespaceUri != null) {
                    String prefix = qname.getPrefix();
                    if (!ctx.isPrefixRegistered(prefix, namespaceUri)) {
                        ctx.registerNamespacePrefix(prefix, namespaceUri);
                        out.print(" ");
                        out.print("xmlns");
                        if (!prefix.isEmpty()) {
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

    /**
     * Prints the attributes for the current element.
     *
     * @param attributes the attributes to print
     * @param ctx the namespace context used for namespace-aware attribute names
     */
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
                printEscaped(FormatHelper.toString(value), true);
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
        return node.text().isEmpty();
    }

    /**
     * Resolves the printable name for a node, qualified name or plain string.
     * Subclasses may override to customize name rendering.
     *
     * @param object the object representing a node name
     * @return the printable name
     */
    protected String getName(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof QName qname) {
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
                    if (isAttributeValue && "'".equals(quote))
                        out.print("&apos;");
                    else
                        out.print(c);
                    break;
                case '"':
                    if (isAttributeValue && "\"".equals(quote))
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

    /**
     * Tracks namespace declarations already emitted while printing a subtree.
     */
    protected static class NamespaceContext {
        private final Map<String, String> namespaceMap;

        /**
         * Creates an empty namespace context.
         */
        public NamespaceContext() {
            namespaceMap = new HashMap<String, String>();
        }

        /**
         * Creates a namespace context initialized from an existing context.
         *
         * @param context the context to copy
         */
        public NamespaceContext(NamespaceContext context) {
            this();
            namespaceMap.putAll(context.namespaceMap);
        }

        /**
         * Checks whether a prefix is already registered for the supplied namespace URI.
         *
         * @param prefix the namespace prefix to look up
         * @param uri the namespace URI to compare against
         * @return {@code true} if the prefix is already registered for that URI
         */
        public boolean isPrefixRegistered(String prefix, String uri) {
            return namespaceMap.containsKey(prefix) && namespaceMap.get(prefix).equals(uri);
        }

        /**
         * Records a namespace prefix mapping if it has not already been registered.
         *
         * @param prefix the namespace prefix to register
         * @param uri the namespace URI to associate with {@code prefix}
         */
        public void registerNamespacePrefix(String prefix, String uri) {
            if (!isPrefixRegistered(prefix, uri)) {
                namespaceMap.put(prefix, uri);
            }
        }

        /**
         * Returns the namespace URI currently registered for the supplied prefix.
         *
         * @param prefix the namespace prefix to resolve
         * @return the registered namespace URI, or {@code null} if none is registered
         */
        public String getNamespace(String prefix) {
            Object uri = namespaceMap.get(prefix);
            return (uri == null) ? null : uri.toString();
        }
    }
}
