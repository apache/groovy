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
import groovy.util.BuilderSupport;
import groovy.util.IndentPrinter;
import groovy.xml.markupsupport.DoubleQuoteFilter;
import groovy.xml.markupsupport.SingleQuoteFilter;
import groovy.xml.markupsupport.StandardXmlAttributeFilter;
import groovy.xml.markupsupport.StandardXmlFilter;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.codehaus.groovy.vmplugin.v8.PluginDefaultGroovyMethods.orElse;

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
    public enum CharFilter { XML_STRICT, XML_ALL, NONE }

    private IndentPrinter out;
    private boolean nospace;
    private int state;
    private boolean nodeIsEmpty = true;
    private boolean useDoubleQuotes = false;
    private boolean omitNullAttributes = false;
    private boolean omitEmptyAttributes = false;
    private boolean expandEmptyElements = false;
    private boolean escapeAttributes = true;
    private List<Function<Character, Optional<String>>> additionalFilters = null;

    public List<Function<Character, Optional<String>>> getAdditionalFilters() {
        return additionalFilters;
    }

    public void setAdditionalFilters(List<Function<Character, Optional<String>>> additionalFilters) {
        this.additionalFilters = additionalFilters;
    }

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
        if (value == null) {
            throw new IllegalArgumentException();
        }
        List<Function<Character, Optional<String>>> transforms = new ArrayList<>();
        transforms.add(new DefaultXmlEscapingFunction(isAttrValue, useDoubleQuotes));
        if (additionalFilters != null) {
            transforms.addAll(additionalFilters);
        }
        return StringGroovyMethods.collectReplacements(value, transforms);
    }

    public static class DefaultXmlEscapingFunction implements Function<Character, Optional<String>> {
        private final boolean isAttrValue;

        private final Function<Character, Optional<String>> stdFilter = new StandardXmlFilter();
        private final Function<Character, Optional<String>> attrFilter = new StandardXmlAttributeFilter();
        private final Function<Character, Optional<String>> quoteFilter;

        public DefaultXmlEscapingFunction(boolean isAttrValue, boolean useDoubleQuotes) {
            this.isAttrValue = isAttrValue;
            this.quoteFilter = useDoubleQuotes ? new DoubleQuoteFilter() : new SingleQuoteFilter();
        }

        public Optional<String> apply(Character ch) {
            return orElse(stdFilter.apply(ch),
                    () -> {
                        if (isAttrValue) {
                            return orElse(attrFilter.apply(ch), () -> quoteFilter.apply(ch));
                        }
                        return Optional.empty();
                    }
            );
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
