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

import groovy.lang.Closure;
import groovy.namespace.QName;
import groovy.util.BuilderSupport;
import groovy.util.IndentPrinter;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class for creating XML or HTML markup.
 * The builder supports various 'pretty printed' formats.
 * <p>
 * Example:
 * <pre>
 * new MarkupBuilder().root {
 *   a( a1:'one' ) {
 *     b { mkp.yield( '3 {@code <} 5' ) }
 *     c( a2:'two', 'blah' )
 *   }
 * }
 * </pre>
 * Will print the following to System.out:
 * <pre>
 * &lt;root&gt;
 *   &lt;a a1='one'&gt;
 *     &lt;b&gt;3 &amp;lt; 5&lt;/b&gt;
 *     &lt;c a2='two'&gt;blah&lt;/c&gt;
 *   &lt;/a&gt;
 * &lt;/root&gt;
 * </pre>
 * Notes:
 * <ul>
 *    <li><code>mkp</code> is a special namespace used to escape
 * away from the normal building mode of the builder and get access
 * to helper markup methods such as 'yield' and 'yieldUnescaped'.
 * See the javadoc for {@link #getMkp()} for further details.</li>
 *     <li>Note that tab, newline and carriage return characters are escaped within attributes, i.e. will become &amp;#09;, &amp;#10; and &amp;#13; respectively</li>
 * </ul>
 */

public class MarkupBuilder extends BuilderSupport {
    private IndentPrinter out;
    private boolean nospace;
    private int state;
    private boolean nodeIsEmpty = true;
    private boolean useDoubleQuotes = false;
    private boolean omitNullAttributes = false;
    private boolean omitEmptyAttributes = false;
    private boolean expandEmptyElements = false;
    private boolean escapeAttributes = true;
    private MarkupCharFilter characterFilter = MarkupCharFilter.NONE;

    /**
     * Returns the escapeAttributes property value.
     *
     * @return the escapeAttributes property value
     * @see #setEscapeAttributes(boolean)
     */
    public boolean isEscapeAttributes() {
        return escapeAttributes;
    }

    /**
     * Defaults to true.&#160;If set to false then you must escape any special
     * characters within attribute values such as '&amp;', '&lt;', CR/LF, single
     * and double quotes etc.&#160;manually as needed. The builder will not guard
     * against producing invalid XML when in this mode and the output may not
     * be able to be parsed/round-tripped but it does give you full control when
     * producing for instance HTML output.
     *
     * @param escapeAttributes the new value
     */
    public void setEscapeAttributes(boolean escapeAttributes) {
        this.escapeAttributes = escapeAttributes;
    }

    /**
     * Prints markup to System.out
     *
     * @see IndentPrinter#IndentPrinter()
     */
    public MarkupBuilder() {
        this(new IndentPrinter());
    }

    /**
     * Sends markup to the given PrintWriter
     *
     * @param pw the PrintWriter to use
     * @see IndentPrinter#IndentPrinter(Writer)
     */
    public MarkupBuilder(PrintWriter pw) {
        this(new IndentPrinter(pw));
    }

    /**
     * Sends markup to the given Writer but first wrapping it in a PrintWriter
     *
     * @param writer the writer to use
     * @see IndentPrinter#IndentPrinter(Writer)
     */
    public MarkupBuilder(Writer writer) {
        this(new IndentPrinter(new PrintWriter(writer)));
    }

    /**
     * Sends markup to the given IndentPrinter.  Use this option if you want
     * to customize the indent used or provide your own IndentPrinter.
     *
     * @param out the IndentPrinter to use
     */
    public MarkupBuilder(IndentPrinter out) {
        this.out = out;
    }

    /**
     * Returns <code>true</code> if attribute values are output with
     * double quotes; <code>false</code> if single quotes are used.
     * By default, single quotes are used.
     *
     * @return true if double quotes are used for attributes
     */
    public boolean getDoubleQuotes() {
        return this.useDoubleQuotes;
    }

    /**
     * Sets whether the builder outputs attribute values in double
     * quotes or single quotes.
     *
     * @param useDoubleQuotes If this parameter is <code>true</code>,
     *                        double quotes are used; otherwise, single quotes are.
     */
    public void setDoubleQuotes(boolean useDoubleQuotes) {
        this.useDoubleQuotes = useDoubleQuotes;
    }

    /**
     * Determine whether null attributes will appear in the produced markup.
     *
     * @return <code>true</code>, if null attributes will be
     *         removed from the resulting markup.
     */
    public boolean isOmitNullAttributes() {
        return omitNullAttributes;
    }

    /**
     * Allows null attributes to be removed from the generated markup.
     *
     * @param omitNullAttributes if <code>true</code>, null
     *                           attributes will not be included in the resulting markup.
     *                           If <code>false</code> null attributes will be included in the
     *                           markup as empty strings regardless of the omitEmptyAttribute
     *                           setting. Defaults to <code>false</code>.
     */
    public void setOmitNullAttributes(boolean omitNullAttributes) {
        this.omitNullAttributes = omitNullAttributes;
    }

    /**
     * Determine whether empty attributes will appear in the produced markup.
     *
     * @return <code>true</code>, if empty attributes will be
     *         removed from the resulting markup.
     */
    public boolean isOmitEmptyAttributes() {
        return omitEmptyAttributes;
    }

    /**
     * Allows empty attributes to be removed from the generated markup.
     *
     * @param omitEmptyAttributes if <code>true</code>, empty
     *                            attributes will not be included in the resulting markup.
     *                            Defaults to <code>false</code>.
     */
    public void setOmitEmptyAttributes(boolean omitEmptyAttributes) {
        this.omitEmptyAttributes = omitEmptyAttributes;
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
     * Returns the current character filter.
     *
     * @return the character filter used by this builder.
     */
    public MarkupCharFilter getCharacterFilter() { return this.characterFilter; }

    /**
     * Set a filter to limit the characters, that can appear in attribute values and text nodes.
     * <p>
     *     Some unicode character are either not allowed, discouraged or not referenceable  with an escape sequence
     *     by specification. Especially XML parsers might have trouble dealing with some of those characters.
     *     Since HTML strives for closeness to XML, filtering might be helpful there, too, albeit to a lesser degree.
     * </p>
     * <p>
     *     Examples include null bytes (0x0), control characters (0x1C "file separator"), surrogates or non-characters.
     *     If a filter policy is used, characters that fail to pass will be replaced by 0xFFFD (&#xFFFD;) in the output.
     * </p>
     * <p>
     *     Available policies are:
     *     <dl>
     *         <dt>NONE (Default)</dt>
     *         <dd>No filter is applied to the output</dd>
     *         <dt>XML_ALL</dt>
     *         <dd>
     *             Allow all characters, that are neccessarily supported. According to the XML spec.<br>
     *             Given as #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] ( | [#x10000-#x10FFFF] )<br>
     *             (as of Aug. 2020)
     *         </dd>
     *         <dt>XML_STRICT</dt>
     *         <dd>
     *             Filter out none-supported <it>and</it> discouraged characters, according to XML spec.
     *         </dd>
     *     </dl>
     * </p>
     * @param characterFilter character policy to use
     */
    public void setCharacterFilter(MarkupCharFilter characterFilter) { this.characterFilter = characterFilter; }

    protected IndentPrinter getPrinter() {
        return this.out;
    }

    protected void setParent(Object parent, Object child) {
    }

    /**
     * Property that may be called from within your builder closure to access
     * helper methods, namely {@link MarkupBuilderHelper#yield(String)},
     * {@link MarkupBuilderHelper#yieldUnescaped(String)},
     * {@link MarkupBuilderHelper#pi(Map)},
     * {@link MarkupBuilderHelper#xmlDeclaration(Map)} and
     * {@link MarkupBuilderHelper#comment(String)}.
     *
     * @return this MarkupBuilder
     */
    public MarkupBuilderHelper getMkp() {
        return new MarkupBuilderHelper(this);
    }

    /**
     * Produce an XML processing instruction in the output.
     * For example:
     * <pre>
     * mkp.pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
     * </pre>
     *
     * @param args a map with a single entry whose key is the name of the
     *             processing instruction and whose value is the attributes
     *             for the processing instruction.
     */
    void pi(Map<String, Map<String, Object>> args) {
        Iterator<Map.Entry<String, Map<String, Object>>> iterator = args.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> mapEntry = iterator.next();
            createNode("?" + mapEntry.getKey(), mapEntry.getValue());
            state = 2;
            out.println("?>");
        }
    }

    void yield(String value, boolean escaping) {
        if (state == 1) {
            state = 2;
            this.nodeIsEmpty = false;
            out.print(">");
        }
        if (state == 0 || state == 2 || state == 3) {
            out.print(escaping ? escapeElementContent(value) : value);
        }
    }

    protected Object createNode(Object name) {
        Object theName = getName(name);
        toState(1, theName);
        this.nodeIsEmpty = true;
        return theName;
    }

    protected Object createNode(Object name, Object value) {
        Object theName = getName(name);
        if (value == null) {
            return createNode(theName);
        } else {
            toState(2, theName);
            this.nodeIsEmpty = false;
            out.print(">");
            out.print(escapeElementContent(value.toString()));
            return theName;
        }
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        Object theName = getName(name);
        toState(1, theName);
        for (Object p : attributes.entrySet()) {
            Map.Entry entry = (Map.Entry) p;
            Object attributeValue = entry.getValue();
            boolean skipNull = attributeValue == null && omitNullAttributes;
            boolean skipEmpty = attributeValue != null && omitEmptyAttributes &&
                    attributeValue.toString().length() == 0;
            if (!skipNull && !skipEmpty) {
                out.print(" ");
                // Output the attribute name,
                print(entry.getKey().toString());
                // Output the attribute value within quotes. Use whichever
                // type of quotes are currently configured.
                out.print(useDoubleQuotes ? "=\"" : "='");
                print(attributeValue == null ? "" : escapeAttributes ? escapeAttributeValue(attributeValue.toString()) : attributeValue.toString());
                out.print(useDoubleQuotes ? "\"" : "'");
            }
        }
        if (value != null) {
            this.yield(value.toString(), true);
        } else {
            nodeIsEmpty = true;
        }

        return theName;
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
     * Escapes a string so that it can be used directly as an XML
     * attribute value.
     *
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding XML entities.
     * @see #escapeXmlValue(String, boolean)
     */
    private String escapeAttributeValue(String value) {
        return escapeXmlValue(value, true);
    }

    /**
     * Escapes a string so that it can be used directly in XML element
     * content.
     *
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding XML entities.
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
     * <li>&amp; as &amp;amp;</li>
     * <li>&lt; as &amp;lt;</li>
     * <li>&gt; as &amp;gt;</li>
     * </ul>
     * If the string is to be added as an attribute value, these
     * characters are also escaped:
     * <ul>
     * <li>' as &amp;apos;</li>
     * </ul>
     *
     * @param value       The string to escape.
     * @param isAttrValue <code>true</code> if the string is to be used
     *                    as an attribute value, otherwise <code>false</code>.
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding XML entities.
     */
    private String escapeXmlValue(String value, boolean isAttrValue) {
        if (value == null)
            throw new IllegalArgumentException();
        return StringGroovyMethods.collectReplacements(value, new ReplacingClosure(isAttrValue, useDoubleQuotes, characterFilter));
    }

    private static class ReplacingClosure extends Closure<String> {
        private final boolean isAttrValue;
        private final boolean useDoubleQuotes;
        private final MarkupCharFilter characterFilter;

        public ReplacingClosure(boolean isAttrValue, boolean useDoubleQuotes, MarkupCharFilter characterFilter) {
            super(null);
            this.isAttrValue = isAttrValue;
            this.useDoubleQuotes = useDoubleQuotes;
            this.characterFilter = characterFilter;
        }

        public String doCall(Character ch) {
            switch (ch) {
                case 0:
                    if (characterFilter != MarkupCharFilter.NONE) return "\uFFFD";
                    break;
                case '&':
                    return "&amp;";
                case '<':
                    return "&lt;";
                case '>':
                    return "&gt;";
                case '\n':
                    if (isAttrValue) return "&#10;";
                    break;
                case '\r':
                    if (isAttrValue) return "&#13;";
                    break;
                case '\t':
                    if (isAttrValue) return "&#09;";
                    break;
                case '"':
                    // The double quote is only escaped if the value is for
                    // an attribute and the builder is configured to output
                    // attribute values inside double quotes.
                    if (isAttrValue && useDoubleQuotes) return "&quot;";
                    break;
                case '\'':
                    // The apostrophe is only escaped if the value is for an
                    // attribute, as opposed to element content, and if the
                    // builder is configured to surround attribute values with
                    // single quotes.
                    if (isAttrValue && !useDoubleQuotes) return "&apos;";
                    break;
            }
            if (ch < 127 && !isXmlAllowedControl(ch) && characterFilter != MarkupCharFilter.NONE) {
                return "\uFFFD";
            } else if (Character.isSurrogate(ch) && characterFilter != MarkupCharFilter.NONE) {
                return "\uFFFD";
            } else if ((Character.isISOControl(ch) || isNonCharacter(ch)) && characterFilter == MarkupCharFilter.XML_STRICT) {
                return "\uFFFD";
            }
            return null;
        }

        private boolean isXmlAllowedControl(char ch) {
            return ch ==  9 || ch == 10 || ch == 12 || ch == 13;
        }
        private boolean isNonCharacter(char ch) {
            return 0xFDD0 <= ch && ch <= 0xFDEF || ((ch ^ 0xFFFE) == 0 || (ch ^ 0xFFFF) == 0);
        }
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
                            if (expandEmptyElements) {
                                out.print("></");
                                print(name);
                                out.print(">");
                            } else {
                                out.print(" />");
                            }
                        }
                        break;
                }
                break;
            case 2:
                switch (next) {
                    case 1:
                    case 2:
                        if (!nodeIsEmpty) {
                            out.println();
                            out.incrementIndent();
                            out.printIndent();
                        }
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

    private static Object getName(Object name) {
        if (name instanceof QName) {
            return ((QName) name).getQualifiedName();
        }
        return name;
    }
}
