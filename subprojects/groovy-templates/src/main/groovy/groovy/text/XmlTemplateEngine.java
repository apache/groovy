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
package groovy.text;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.util.IndentPrinter;
import groovy.util.Node;
import groovy.xml.XmlNodePrinter;
import groovy.xml.XmlParser;
import groovy.namespace.QName;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Template engine for use in templating scenarios where both the template
 * source and the expected output are intended to be XML.
 * <p>
 * Templates may use the normal '${expression}' and '$variable' notations
 * to insert an arbitrary expression into the template.
 * In addition, support is also provided for special tags:
 * &lt;gsp:scriptlet&gt; (for inserting code fragments) and
 * &lt;gsp:expression&gt; (for code fragments which produce output).
 * <p>
 * Comments and processing instructions
 * will be removed as part of processing and special XML characters such as
 * &lt;, &gt;, &quot; and &apos; will be escaped using the respective XML notation.
 * The output will also be indented using standard XML pretty printing.
 * <p>
 * The xmlns namespace definition for <code>gsp:</code> tags will be removed
 * but other namespace definitions will be preserved (but may change to an
 * equivalent position within the XML tree).
 * <p>
 * Normally, the template source will be in a file but here is a simple
 * example providing the XML template as a string:
 * <pre>
 * def binding = [firstname:"Jochen", lastname:"Theodorou",
 *                nickname:"blackdrag", salutation:"Dear"]
 * def engine = new groovy.text.XmlTemplateEngine()
 * def text = '''\
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;document xmlns:gsp='http://groovy.codehaus.org/2005/gsp' xmlns:foo='baz' type='letter'&gt;
 *   &lt;gsp:scriptlet&gt;def greeting = "${salutation}est"&lt;/gsp:scriptlet&gt;
 *   &lt;gsp:expression&gt;greeting&lt;/gsp:expression&gt;
 *   &lt;foo:to&gt;$firstname "$nickname" $lastname&lt;/foo:to&gt;
 *   How are you today?
 * &lt;/document&gt;
 * '''
 * def template = engine.createTemplate(text).make(binding)
 * println template.toString()
 * </pre>
 * This example will produce this output:
 * <pre>
 * &lt;document type='letter'&gt;
 * Dearest
 * &lt;foo:to xmlns:foo='baz'&gt;
 *   Jochen &amp;quot;blackdrag&amp;quot; Theodorou
 * &lt;/foo:to&gt;
 * How are you today?
 * &lt;/document&gt;
 * </pre>
 * The XML template engine can also be used as the engine for {@link groovy.servlet.TemplateServlet} by placing the
 * following in your web.xml file (plus a corresponding servlet-mapping element):
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;XmlTemplate&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;template.engine&lt;/param-name&gt;
 *     &lt;param-value&gt;groovy.text.XmlTemplateEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre>
 */
public class XmlTemplateEngine extends TemplateEngine {

    private static int counter = 1;

    private static class GspPrinter extends XmlNodePrinter {

        public GspPrinter(PrintWriter out, String indent) {
            this(new IndentPrinter(out, indent));
        }

        public GspPrinter(IndentPrinter out) {
            super(out, "\\\"");
            setQuote("'");
        }

        protected void printGroovyTag(String tag, String text) {
            if (tag.equals("scriptlet")) {
                out.print(text);
                out.print("\n");
                return;
            }
            if (tag.equals("expression")) {
                printLineBegin();
                out.print("${");
                out.print(text);
                out.print("}");
                printLineEnd();
                return;
            }
            throw new RuntimeException("Unsupported 'gsp:' tag named \"" + tag + "\".");
        }

        protected void printSimpleItem(Object value) {
            this.printLineBegin();
            out.print(escapeSpecialChars(InvokerHelper.toString(value)));
            printLineEnd();
        }

        private String escapeSpecialChars(String s) {
            StringBuilder sb = new StringBuilder();
            boolean inGString = false;
            for (int i = 0; i < s.length(); i++) {
                final char c = s.charAt(i);
                switch (c) {
                    case '$':
                        sb.append("$");
                        if (i < s.length() - 1 && s.charAt(i + 1) == '{') inGString = true;
                        break;
                    case '<':
                        append(sb, c, "&lt;", inGString);
                        break;
                    case '>':
                        append(sb, c, "&gt;", inGString);
                        break;
                    case '"':
                        append(sb, c, "&quot;", inGString);
                        break;
                    case '\'':
                        append(sb, c, "&apos;", inGString);
                        break;
                    case '}':
                        sb.append(c);
                        inGString = false;
                        break;
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        }

        private void append(StringBuilder sb, char plainChar, String xmlString, boolean inGString) {
            if (inGString) {
                sb.append(plainChar);
            } else {
                sb.append(xmlString);
            }
        }

        protected void printLineBegin() {
            out.print("out.print(\"\"\"");
            out.printIndent();
        }

        protected void printLineEnd(String comment) {
            out.print("\\n\"\"\");");
            if (comment != null) {
                out.print(" // ");
                out.print(comment);
            }
            out.print("\n");
        }

        protected boolean printSpecialNode(Node node) {
            Object name = node.name();
            if (name instanceof QName) {
                QName qn = (QName) name;
                // check uri and for legacy cases just check prefix name (not recommended)
                if (qn.getNamespaceURI().equals("http://groovy.codehaus.org/2005/gsp") || qn.getPrefix().equals("gsp")) {
                    String s = qn.getLocalPart();
                    if (s.length() == 0) {
                        throw new RuntimeException("No local part after 'gsp:' given in node " + node);
                    }
                    printGroovyTag(s, node.text());
                    return true;
                }
            }
            return false;
        }
    }

    private static class XmlTemplate implements Template {

        private final Script script;

        public XmlTemplate(Script script) {
            this.script = script;
        }

        public Writable make() {
            return make(new HashMap());
        }

        public Writable make(Map map) {
            if (map == null) {
                throw new IllegalArgumentException("map must not be null");
            }
            return new XmlWritable(script, new Binding(map));
        }
    }

    private static class XmlWritable implements Writable {

        private final Binding binding;
        private final Script script;
        private WeakReference result;

        public XmlWritable(Script script, Binding binding) {
            this.script = script;
            this.binding = binding;
            this.result = new WeakReference<>(null);
        }

        public Writer writeTo(Writer out) {
            Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
            PrintWriter pw = new PrintWriter(out);
            scriptObject.setProperty("out", pw);
            scriptObject.run();
            pw.flush();
            return out;
        }

        public String toString() {
            if (result.get() != null) {
                return result.get().toString();
            }
            String string = writeTo(new StringBuilderWriter(1024)).toString();
            result = new WeakReference<>(string);
            return string;
        }
    }

    public static final String DEFAULT_INDENTATION = "  ";

    private final GroovyShell groovyShell;
    private final XmlParser xmlParser;
    private String indentation;

    public XmlTemplateEngine() throws SAXException, ParserConfigurationException {
        this(DEFAULT_INDENTATION, false);
    }

    public XmlTemplateEngine(String indentation, boolean validating) throws SAXException, ParserConfigurationException {
        this(new XmlParser(validating, true), new GroovyShell());
        this.xmlParser.setTrimWhitespace(true);
        setIndentation(indentation);
    }

    public XmlTemplateEngine(XmlParser xmlParser, ClassLoader parentLoader) {
        this(xmlParser, new GroovyShell(parentLoader));
    }

    public XmlTemplateEngine(XmlParser xmlParser, GroovyShell groovyShell) {
        this.groovyShell = groovyShell;
        this.xmlParser = xmlParser;
        setIndentation(DEFAULT_INDENTATION);
    }

    public Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        Node root ;
        try {
            root = xmlParser.parse(reader);
        } catch (SAXException e) {
            throw new RuntimeException("Parsing XML source failed.", e);
        }

        if (root == null) {
            throw new IOException("Parsing XML source failed: root node is null.");
        }

        StringBuilderWriter writer = new StringBuilderWriter(1024);
        writer.write("/* Generated by XmlTemplateEngine */\n");
        new GspPrinter(new PrintWriter(writer), indentation).print(root);

        Script script;
        try {
            script = groovyShell.parse(writer.toString(), "XmlTemplateScript" + counter++ + ".groovy");
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
        }
        return new XmlTemplate(script);
    }

    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(String indentation) {
        if (indentation == null) {
            indentation = DEFAULT_INDENTATION;
        }
        this.indentation = indentation;
    }

    public String toString() {
        return "XmlTemplateEngine";
    }

}
