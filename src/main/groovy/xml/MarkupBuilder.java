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
package groovy.xml;

import groovy.util.BuilderSupport;
import groovy.util.IndentPrinter;
import groovy.lang.Closure;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class for creating XML or HTML markup
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Stefan Matthias Aust
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
 * @version $Revision$
 */
public class MarkupBuilder extends BuilderSupport {
    private IndentPrinter out;
    private boolean nospace;
    private int state;
    private boolean nodeIsEmpty = true;
    private boolean useDoubleQuotes = false;

    public MarkupBuilder() {
        this(new IndentPrinter());
    }

    public MarkupBuilder(PrintWriter writer) {
        this(new IndentPrinter(writer));
    }

    public MarkupBuilder(Writer writer) {
        this(new IndentPrinter(new PrintWriter(writer)));
    }

    public MarkupBuilder(IndentPrinter out) {
        this.out = out;
    }

    /**
     * Returns <code>true</code> if attribute values are output with
     * double quotes; <code>false</code> if single quotes are used.
     * By default, single quotes are used.
     * @return true if double quotes are used for attributes
     */
    public boolean getDoubleQuotes() {
        return this.useDoubleQuotes;
    }

    /**
     * Sets whether the builder outputs attribute values in double
     * quotes or single quotes.
     * @param useDoubleQuotes If this parameter is <code>true</code>,
     * double quotes are used; otherwise, single quotes are.
     */
    public void setDoubleQuotes(boolean useDoubleQuotes) {
        this.useDoubleQuotes = useDoubleQuotes;
    }

    protected IndentPrinter getPrinter() {
        return this.out;
    }

    protected void setParent(Object parent, Object child) { }

    protected void setClosureDelegate(Closure closure, Object node) {
        super.setClosureDelegate(closure, node);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    }

    public Object getMkp() {
        return this;
    }

    public void yield(String value) {
        if (state == 1) {
            state = 2;
            this.nodeIsEmpty = false;
            out.print(">");
        }
        if (state == 2 || state == 3) {
            out.print(escapeElementContent(value));
        }
    }

    public void yieldUnescaped(String value) {
        if (state == 1) {
            state = 2;
            this.nodeIsEmpty = false;
            out.print(">");
        }
        if (state == 2 || state == 3) {
            out.print(value);
        }
    }

    protected Object createNode(Object name) {
        this.nodeIsEmpty = true;
        toState(1, name);
        return name;
    }

    protected Object createNode(Object name, Object value) {
        toState(2, name);
        this.nodeIsEmpty = false;
        out.print(">");
        out.print(escapeElementContent(value.toString()));
        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        toState(1, name);
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.print(" ");

            // Output the attribute name,
            print(entry.getKey().toString());

            // Output the attribute value within quotes. Use whichever
            // type of quotes are currently configured.
            out.print(this.useDoubleQuotes ? "=\"" : "='");
            print(escapeAttributeValue(entry.getValue().toString()));
            out.print(this.useDoubleQuotes ? "\"" : "'");
        }

        if (value != null) {
            nodeIsEmpty = false;
            out.print(">" + escapeElementContent(value.toString()) + "</" + name + ">");
        }
        else {
            nodeIsEmpty = true;
        }

        return name;
    }

    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }
    
    protected void nodeCompleted(Object parent, Object node) {
        toState(3, node);
        out.flush();
    }

    protected void print(Object node) {
        out.print(node == null ? "null" : node.toString());
    }

    protected Object getName(String methodName) {
        return super.getName(methodName);
    }

    /**
     * Returns a String with special XML characters escaped as entities so that
     * output XML is valid. Escapes the following characters as corresponding 
     * entities:
     * <ul>
     *   <li>\' as &amp;apos;</li>
     *   <li>&amp; as &amp;amp;</li>
     *   <li>&lt; as &amp;lt;</li>
     *   <li>&gt; as &amp;gt;</li>
     * </ul>
     * 
     * @param value to be searched and replaced for XML special characters.
     * @return value with XML characters escaped
     * @deprecated
     * @see #escapeXmlValue(String, boolean)
     */
    protected String transformValue(String value) {
        // & has to be checked and replaced before others
        if (value.matches(".*&.*")) {
            value = value.replaceAll("&", "&amp;");
        }
        if (value.matches(".*\\'.*")) {
            value = value.replaceAll("\\'", "&apos;");
        }
        if (value.matches(".*<.*")) {
            value = value.replaceAll("<", "&lt;");
        }
        if (value.matches(".*>.*")) {
            value = value.replaceAll(">", "&gt;");
        }
        return value;
    }

    /**
     * Escapes a string so that it can be used directly as an XML
     * attribute value.
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     * @see #escapeXmlValue(String, boolean)
     */
    private String escapeAttributeValue(String value) {
        return escapeXmlValue(value, true);
    }

    /**
     * Escapes a string so that it can be used directly in XML element
     * content.
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     * @see #escapeXmlValue(String, boolean)
     */
    private String escapeElementContent(String value) {
        return escapeXmlValue(value, false);
    }

    /**
     * Escapes a string so that it can be used in XML text successfully.
     * It replaces the following characters with the corresponding XML
     * entities:
     * <ul>
     *   <li>&amp; as &amp;amp;</li>
     *   <li>&lt; as &amp;lt;</li>
     *   <li>&gt; as &amp;gt;</li>
     * </ul>
     * If the string is to be added as an attribute value, these
     * characters are also escaped:
     * <ul>
     *   <li>' as &amp;apos;</li>
     * </ul>
     * @param value The string to escape.
     * @param isAttrValue <code>true</code> if the string is to be used
     * as an attribute value, otherwise <code>false</code>.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     */
    private String escapeXmlValue(String value, boolean isAttrValue) {
        StringBuffer buffer = new StringBuffer(value);
        for (int i = 0, n = buffer.length(); i < n; i++) {
            switch (buffer.charAt(i)) {
            case '&':
                buffer.replace(i, i + 1, "&amp;");

                // We're replacing a single character by a string of
                // length 5, so we need to update the index variable
                // and the total length.
                i += 4;
                n += 4;
                break;

            case '<':
                buffer.replace(i, i + 1, "&lt;");

                // We're replacing a single character by a string of
                // length 4, so we need to update the index variable
                // and the total length.
                i += 3;
                n += 3;
                break;

            case '>':
                buffer.replace(i, i + 1, "&gt;");

                // We're replacing a single character by a string of
                // length 4, so we need to update the index variable
                // and the total length.
                i += 3;
                n += 3;
                break;

            case '"':
                // The double quote is only escaped if the value is for
                // an attribute and the builder is configured to output
                // attribute values inside double quotes.
                if (isAttrValue && this.useDoubleQuotes) {
                    buffer.replace(i, i + 1, "&quot;");

                    // We're replacing a single character by a string of
                    // length 6, so we need to update the index variable
                    // and the total length.
                    i += 5;
                    n += 5;
                }
                break;

            case '\'':
                // The apostrophe is only escaped if the value is for an
                // attribute, as opposed to element content, and if the
                // builder is configured to surround attribute values with
                // single quotes.
                if (isAttrValue && !this.useDoubleQuotes){
                    buffer.replace(i, i + 1, "&apos;");

                    // We're replacing a single character by a string of
                    // length 6, so we need to update the index variable
                    // and the total length.
                    i += 5;
                    n += 5;
                }
                break;

            default:
                break;
            }
        }

        return buffer.toString();
    }

    private void toState(int next, Object name) {
        switch (state) {
            case 0:
                switch (next) {
                    case 1:
                    case 2:
                        out.print("<");
                        print(name);
                        break;
                    case 3:
                        throw new Error();
                }
                break;
            case 1:
                switch (next) {
                    case 1:
                    case 2:
                        out.print(">");
                        if (nospace) {
                            nospace = false;
                        } else {
                            out.println();
                            out.incrementIndent();
                            out.printIndent();
                        }
                        out.print("<");
                        print(name);
                        break;
                    case 3:
                        if (nodeIsEmpty) {
                            out.print(" />");
                        }
                        break;
                }
                break;
            case 2:
                switch (next) {
                    case 1:
                    case 2:
                        out.print("<");
                        print(name);
                        break;
                    case 3:
                        out.print("</");
                        print(name);
                        out.print(">");
                        break;
                }
                break;
            case 3:
                switch (next) {
                    case 1:
                    case 2:
                        if (nospace) {
                            nospace = false;
                        } else {
                            out.println();
                            out.printIndent();
                        }
                        out.print("<");
                        print(name);
                        break;
                    case 3:
                        if (nospace) {
                            nospace = false;
                        } else {
                            out.println();
                            out.decrementIndent();
                            out.printIndent();
                        }
                        out.print("</");
                        print(name);
                        out.print(">");
                        break;
                }
                break;
        }
        state = next;
    }
}
