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
package org.apache.groovy.xml.tools;

import groovy.util.IndentPrinter;
import groovy.xml.FactorySupport;
import org.codehaus.groovy.syntax.Types;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A SAX handler for turning XML into Groovy scripts
 */
public class DomToGroovy {

    /**
     * Destination printer for the generated Groovy source.
     */
    protected IndentPrinter out;
    /**
     * Whether the current element contains mixed text and element content.
     */
    protected boolean inMixed = false;
    /**
     * Quote delimiter used for generated Groovy string literals.
     */
    protected String qt = "'";
    /**
     * Groovy keywords that must be quoted when emitted as method-style element names.
     */
    protected Collection<String> keywords = Types.getKeywords();

    /**
     * Creates a converter that writes generated Groovy code to the supplied writer.
     *
     * @param out destination writer
     */
    public DomToGroovy(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    // TODO allow string quoting delimiter to be specified, e.g. ' vs "
    /**
     * Creates a converter that writes generated Groovy code with the supplied indent printer.
     *
     * @param out destination printer
     */
    public DomToGroovy(IndentPrinter out) {
        this.out = out;
    }

    /**
     * Writes the Groovy builder form of the supplied DOM document.
     *
     * @param document source DOM document
     */
    public void print(Document document) {
        printChildren(document, new HashMap());
    }

    /**
     * Command-line entry point that converts an XML file into a Groovy builder script.
     *
     * @param args {@code infilename [outfilename]}
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: DomToGroovy infilename [outfilename]");
            System.exit(1);
        }
        Document document = null;
        try {
            document = parse(args[0]);
        } catch (Exception e) {
            System.out.println("Unable to parse input file '" + args[0] + "': " + e.getMessage());
            System.exit(1);
        }
        PrintWriter writer = null;
        if (args.length < 2) {
            writer = new PrintWriter(System.out);
        } else {
            try {
                writer = new PrintWriter(new FileWriter(new File(args[1])));
            } catch (IOException e) {
                System.out.println("Unable to create output file '" + args[1] + "': " + e.getMessage());
                System.exit(1);
            }
        }
        DomToGroovy converter = new DomToGroovy(writer);
        converter.out.incrementIndent();
        writer.println("#!/bin/groovy");
        writer.println();
        writer.println("// generated from " + args[0]);
        writer.println("System.out << new groovy.xml.StreamingMarkupBuilder().bind {");
        converter.print(document);
        writer.println("}");
        writer.close();
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Parses the given XML file name into a DOM document.
     *
     * @param fileName XML file path
     * @return parsed DOM document
     * @throws Exception if parsing fails
     */
    protected static Document parse(final String fileName) throws Exception {
        return parse(new File(fileName));
    }

    /**
     * Parses the given XML file into a DOM document.
     *
     * @param file XML file
     * @return parsed DOM document
     * @throws Exception if parsing fails
     */
    public static Document parse(final File file) throws Exception {
        return parse(new BufferedReader(new FileReader(file)));
    }

    /**
     * Parses XML read from the supplied reader into a DOM document.
     *
     * @param input XML reader
     * @return parsed DOM document
     * @throws Exception if parsing fails
     */
    public static Document parse(final Reader input) throws Exception {
        return parse(new InputSource(input));
    }

    /**
     * Parses XML read from the supplied stream into a DOM document.
     *
     * @param input XML stream
     * @return parsed DOM document
     * @throws Exception if parsing fails
     */
    public static Document parse(final InputStream input) throws Exception {
        return parse(new InputSource(input));
    }

    private static Document parse(InputSource is) throws Exception {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    /**
     * Dispatches a DOM node to the appropriate Groovy emission helper.
     *
     * @param node DOM node to emit
     * @param namespaces namespaces currently in scope
     * @param endWithComma whether to terminate the emitted fragment with a comma
     */
    protected void print(Node node, Map namespaces, boolean endWithComma) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE :
                printElement((Element) node, namespaces, endWithComma);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE :
                printPI((ProcessingInstruction) node, endWithComma);
                break;
            case Node.TEXT_NODE :
                printText((Text) node, endWithComma);
                break;
            case Node.COMMENT_NODE :
                printComment((Comment) node, endWithComma);
                break;
            default:
                break;
        }
    }

    /**
     * Emits Groovy markup for a DOM element, including namespaces, attributes, and children.
     *
     * @param element element to emit
     * @param namespaces namespaces currently in scope
     * @param endWithComma whether to terminate the emitted fragment with a comma
     */
    protected void printElement(Element element, Map namespaces, boolean endWithComma) {
        namespaces = defineNamespaces(element, namespaces);

        element.normalize();
        printIndent();

        String prefix = element.getPrefix();
        boolean hasPrefix = prefix != null && !prefix.isEmpty();
        String localName = getLocalName(element);
        boolean isKeyword = checkEscaping(localName);
        if (isKeyword || hasPrefix) print(qt);
        if (hasPrefix) {
            print(prefix);
            print(".");
        }
        print(localName);
        if (isKeyword || hasPrefix) print(qt);
        print("(");

        boolean hasAttributes = printAttributes(element);

        NodeList list = element.getChildNodes();
        int length = list.getLength();
        if (length == 0) {
            printEnd(")", endWithComma);
        } else {
            printChildren(element, namespaces, endWithComma, hasAttributes, list, length);
        }
    }

    private void printChildren(Element element, Map namespaces, boolean endWithComma, boolean hasAttributes, NodeList list, int length) {
        Node node = list.item(0);
        if (length == 1 && node instanceof Text textNode) {
            String text = getTextNodeData(textNode);
            if (hasAttributes) print(", ");
            printQuoted(text);
            printEnd(")", endWithComma);
        } else if (mixedContent(list)) {
            println(") {");
            out.incrementIndent();
            boolean oldInMixed = inMixed;
            inMixed = true;
            for (node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
                print(node, namespaces, false);
            }
            inMixed = oldInMixed;
            out.decrementIndent();
            printIndent();
            printEnd("}", endWithComma);
        } else {
            println(") {");
            out.incrementIndent();
            printChildren(element, namespaces);
            out.decrementIndent();
            printIndent();
            printEnd("}", endWithComma);
        }
    }

    /**
     * Writes text as a Groovy string literal, using triple quotes for multi-line content.
     *
     * @param text text to quote
     */
    protected void printQuoted(String text) {
        if (text.contains("\n")) {
            print("'''");
            print(text);
            print("'''");
        } else {
            print(qt);
            print(escapeQuote(text));
            print(qt);
        }
    }

    /**
     * Emits a processing instruction as an {@code mkp.pi} call.
     *
     * @param instruction processing instruction to emit
     * @param endWithComma whether to terminate the emitted fragment with a comma
     */
    protected void printPI(ProcessingInstruction instruction, boolean endWithComma) {
        printIndent();
        print("mkp.pi(" + qt);
        print(instruction.getTarget());
        print(qt + ", " + qt);
        print(instruction.getData());
        printEnd(qt + ");", endWithComma);
    }

    /**
     * Emits a Groovy block comment for a non-empty DOM comment node.
     *
     * @param comment comment to emit
     * @param endWithComma whether to terminate the emitted fragment with a comma
     */
    protected void printComment(Comment comment, boolean endWithComma) {
        String text = comment.getData().trim();
        if (!text.isEmpty()) {
            printIndent();
            print("/* ");
            print(text);
            printEnd(" */", endWithComma);
        }
    }

    /**
     * Emits a text node, using {@code mkp.yield} when nested in mixed content.
     *
     * @param node text node to emit
     * @param endWithComma whether to terminate the emitted fragment with a comma
     */
    protected void printText(Text node, boolean endWithComma) {
        String text = getTextNodeData(node);
        if (!text.isEmpty()) {
            printIndent();
            if (inMixed) print("mkp.yield ");
            printQuoted(text);
            printEnd("", endWithComma);
        }
    }

    /**
     * Escapes backslashes and the active quote delimiter for generated Groovy strings.
     *
     * @param text raw text
     * @return escaped text
     */
    protected String escapeQuote(String text) {
        return text.replaceAll("\\\\", "\\\\\\\\").replaceAll(qt, "\\\\" + qt);
    }

    /**
     * Returns the namespace map extended with any newly encountered element or attribute prefixes.
     *
     * @param element element whose namespaces should be inspected
     * @param namespaces namespaces currently in scope
     * @return namespace map to use for the element and its children
     */
    protected Map defineNamespaces(Element element, Map namespaces) {
        Map answer = null;
        String prefix = element.getPrefix();
        if (prefix != null && !prefix.isEmpty() && !namespaces.containsKey(prefix)) {
            answer = new HashMap(namespaces);
            defineNamespace(answer, prefix, element.getNamespaceURI());
        }
        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Attr attribute = (Attr) attributes.item(i);
            prefix = attribute.getPrefix();
            if (prefix != null && !prefix.isEmpty() && !namespaces.containsKey(prefix)) {
                if (answer == null) {
                    answer = new HashMap(namespaces);
                }
                defineNamespace(answer, prefix, attribute.getNamespaceURI());
            }
        }
        return (answer != null) ? answer : namespaces;
    }

    /**
     * Records a namespace mapping and emits {@code mkp.declareNamespace} when needed.
     *
     * @param namespaces mutable namespace map
     * @param prefix namespace prefix
     * @param uri namespace URI
     */
    protected void defineNamespace(Map namespaces, String prefix, String uri) {
        namespaces.put(prefix, uri);
        if (!"xmlns".equals(prefix) && !"xml".equals(prefix)) {
            printIndent();
            print("mkp.declareNamespace(");
            print(prefix);
            print(":" + qt);
            print(uri);
            println(qt + ")");
        }
    }

    /**
     * Emits the attributes for the supplied element.
     *
     * @param element element whose attributes should be emitted
     * @return {@code true} if any attributes were written
     */
    protected boolean printAttributes(Element element) {
        boolean hasAttribute = false;
        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        if (length > 0) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                printAttributeWithPrefix((Attr) attributes.item(i), buffer);
            }
            for (int i = 0; i < length; i++) {
                hasAttribute = printAttributeWithoutPrefix((Attr) attributes.item(i), hasAttribute);
            }
            if (buffer.length() > 0) {
                if (hasAttribute) {
                    print(", ");
                }
                print(buffer.toString());
                hasAttribute = true;
            }
        }
        return hasAttribute;
    }

    /**
     * Appends a prefixed attribute representation to the deferred attribute buffer.
     *
     * @param attribute attribute to render
     * @param buffer buffer collecting prefixed attributes
     */
    protected void printAttributeWithPrefix(Attr attribute, StringBuffer buffer) {
        String prefix = attribute.getPrefix();
        if (prefix != null && !prefix.isEmpty() && !"xmlns".equals(prefix)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(qt);
            buffer.append(prefix);
            buffer.append(":");
            buffer.append(getLocalName(attribute));
            buffer.append(qt).append(":").append(qt);
            buffer.append(escapeQuote(getAttributeValue(attribute)));
            buffer.append(qt);
        }
    }

    /**
     * Returns the attribute value used when emitting the Groovy builder call.
     *
     * @param attribute attribute to inspect
     * @return attribute value
     */
    protected String getAttributeValue(Attr attribute) {
        return attribute.getValue();
    }

    /**
     * Emits an unprefixed attribute and updates the separator state.
     *
     * @param attribute attribute to render
     * @param hasAttribute whether a previous attribute has already been written
     * @return updated attribute-written flag
     */
    protected boolean printAttributeWithoutPrefix(Attr attribute, boolean hasAttribute) {
        String prefix = attribute.getPrefix();
        if (prefix == null || prefix.isEmpty()) {
            if (!hasAttribute) {
                hasAttribute = true;
            } else {
                print(", ");
            }
            String localName = getLocalName(attribute);
            boolean needsEscaping = checkEscaping(localName);
            if (needsEscaping) print(qt);
            print(localName);
            if (needsEscaping) print(qt);
            print(":");
            printQuoted(getAttributeValue(attribute));
        }
        return hasAttribute;
    }

    /**
     * Returns whether a generated Groovy name must be quoted.
     *
     * @param localName candidate element or attribute name
     * @return {@code true} if the name requires quoting
     */
    protected boolean checkEscaping(String localName) {
        return keywords.contains(localName) || localName.contains("-") || localName.contains(":") || localName.contains(".");
    }

    /**
     * Returns the trimmed text content used when emitting a DOM text node.
     *
     * @param node text node
     * @return trimmed text content
     */
    protected String getTextNodeData(Text node) {
        return node.getData().trim();
    }

    /**
     * Detects whether a node list contains both non-empty text and element children.
     *
     * @param list child nodes to inspect
     * @return {@code true} for mixed content
     */
    protected boolean mixedContent(NodeList list) {
        boolean hasText = false;
        boolean hasElement = false;
        for (int i = 0, size = list.getLength(); i < size; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                hasElement = true;
            } else if (node instanceof Text) {
                String text = getTextNodeData((Text) node);
                if (!text.isEmpty()) {
                    hasText = true;
                }
            }
            if (hasText && hasElement) break;
        }
        return hasText && hasElement;
    }

    /**
     * Emits all children of the supplied parent node.
     *
     * @param parent DOM parent node
     * @param namespaces namespaces currently in scope
     */
    protected void printChildren(Node parent, Map namespaces) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            print(node, namespaces, false);
        }
    }

    /**
     * Returns the trimmed local name for a DOM node, falling back to {@link Node#getNodeName()}.
     *
     * @param node DOM node
     * @return trimmed local name
     */
    protected String getLocalName(Node node) {
        String answer = node.getLocalName();
        if (answer == null) {
            answer = node.getNodeName();
        }
        return answer.trim();
    }

    /**
     * Finishes the current emitted fragment and optionally appends a comma.
     *
     * @param text trailing text to emit before line termination
     * @param endWithComma whether to append a comma
     */
    protected void printEnd(String text, boolean endWithComma) {
        if (endWithComma) {
            print(text);
            println(",");
        } else {
            println(text);
        }
    }

    /**
     * Writes a line through the configured indent printer.
     *
     * @param text line text
     */
    protected void println(String text) {
        out.println(text);
    }

    /**
     * Writes text through the configured indent printer.
     *
     * @param text text to print
     */
    protected void print(String text) {
        out.print(text);
    }

    /**
     * Writes the current indentation through the configured indent printer.
     */
    protected void printIndent() {
        out.printIndent();
    }
}
