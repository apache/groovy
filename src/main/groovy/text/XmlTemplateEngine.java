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

package groovy.text;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.util.IndentPrinter;
import groovy.util.Node;
import groovy.util.XmlNodePrinter;
import groovy.util.XmlParser;
import groovy.xml.QName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.xml.sax.SAXException;

/**
 * Template engine for xml data input.
 *
 * @author Christian Stein
 * @author Paul King
 */
public class XmlTemplateEngine extends TemplateEngine {

    private static class GspPrinter extends XmlNodePrinter {

        public GspPrinter(PrintWriter out, String indent) {
            this(new IndentPrinter(out, indent));
        }

        public GspPrinter(IndentPrinter out) {
            super(out, "\\\"");
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
            throw new RuntimeException("Unsupported tag named \"" + tag + "\".");
        }

        protected void printLineBegin() {
            out.print("out.print(\"");
            out.printIndent();
        }

        protected void printLineEnd(String comment) {
            out.print("\\n\");");
            if (comment != null) {
                out.print(" // ");
                out.print(comment);
            }
            out.print("\n");
        }

        protected boolean printSpecialNode(Node node) {
            Object name = node.name();
            if (name != null && name instanceof QName) {
                QName qn = (QName) name;
                if (qn.getPrefix().equals("gsp")) {
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
            this.result = new WeakReference(null);
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
            String string = writeTo(new StringWriter(1024)).toString();
            result = new WeakReference(string);
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
        Node root = null;
        try {
            root = xmlParser.parse(reader);
        } catch (SAXException e) {
            throw new RuntimeException("Parsing XML source failed.", e);
        }

        if (root == null) {
            throw new IOException("Parsing XML source failed: root node is null.");
        }

        StringWriter writer = new StringWriter(1024);
        writer.write("/* Generated by XmlTemplateEngine */\n");
        new GspPrinter(new PrintWriter(writer), indentation).print(root);

        Script script = groovyShell.parse(writer.toString());
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
