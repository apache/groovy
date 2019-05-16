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

    protected IndentPrinter out;
    protected boolean inMixed = false;
    protected String qt = "'";
    protected Collection<String> keywords = Types.getKeywords();

    public DomToGroovy(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    // TODO allow string quoting delimiter to be specified, e.g. ' vs "
    public DomToGroovy(IndentPrinter out) {
        this.out = out;
    }

    public void print(Document document) {
        printChildren(document, new HashMap());
    }

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

    protected static Document parse(final String fileName) throws Exception {
        return parse(new File(fileName));
    }

    public static Document parse(final File file) throws Exception {
        return parse(new BufferedReader(new FileReader(file)));
    }

    public static Document parse(final Reader input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(input));
    }

    public static Document parse(final InputStream input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(input));
    }

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
        }
    }

    protected void printElement(Element element, Map namespaces, boolean endWithComma) {
        namespaces = defineNamespaces(element, namespaces);

        element.normalize();
        printIndent();

        String prefix = element.getPrefix();
        boolean hasPrefix = prefix != null && prefix.length() > 0;
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
            Node node = list.item(0);
            if (length == 1 && node instanceof Text) {
                Text textNode = (Text) node;
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
    }

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

    protected void printPI(ProcessingInstruction instruction, boolean endWithComma) {
        printIndent();
        print("mkp.pi(" + qt);
        print(instruction.getTarget());
        print(qt + ", " + qt);
        print(instruction.getData());
        printEnd(qt + ");", endWithComma);
    }

    protected void printComment(Comment comment, boolean endWithComma) {
        String text = comment.getData().trim();
        if (text.length() >0) {
            printIndent();
            print("/* ");
            print(text);
            printEnd(" */", endWithComma);
        }
    }

    protected void printText(Text node, boolean endWithComma) {
        String text = getTextNodeData(node);
        if (text.length() > 0) {
            printIndent();
            if (inMixed) print("mkp.yield ");
            printQuoted(text);
            printEnd("", endWithComma);
        }
    }

    protected String escapeQuote(String text) {
        return text.replaceAll("\\\\", "\\\\\\\\").replaceAll(qt, "\\\\" + qt);
    }

    protected Map defineNamespaces(Element element, Map namespaces) {
        Map answer = null;
        String prefix = element.getPrefix();
        if (prefix != null && prefix.length() > 0 && !namespaces.containsKey(prefix)) {
            answer = new HashMap(namespaces);
            defineNamespace(answer, prefix, element.getNamespaceURI());
        }
        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Attr attribute = (Attr) attributes.item(i);
            prefix = attribute.getPrefix();
            if (prefix != null && prefix.length() > 0 && !namespaces.containsKey(prefix)) {
                if (answer == null) {
                    answer = new HashMap(namespaces);
                }
                defineNamespace(answer, prefix, attribute.getNamespaceURI());
            }
        }
        return (answer != null) ? answer : namespaces;
    }

    protected void defineNamespace(Map namespaces, String prefix, String uri) {
        namespaces.put(prefix, uri);
        if (!prefix.equals("xmlns") && !prefix.equals("xml")) {
            printIndent();
            print("mkp.declareNamespace(");
            print(prefix);
            print(":" + qt);
            print(uri);
            println(qt + ")");
        }
    }

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

    protected void printAttributeWithPrefix(Attr attribute, StringBuffer buffer) {
        String prefix = attribute.getPrefix();
        if (prefix != null && prefix.length() > 0 && !prefix.equals("xmlns")) {
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

    protected String getAttributeValue(Attr attribute) {
        return attribute.getValue();
    }

    protected boolean printAttributeWithoutPrefix(Attr attribute, boolean hasAttribute) {
        String prefix = attribute.getPrefix();
        if (prefix == null || prefix.length() == 0) {
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

    protected boolean checkEscaping(String localName) {
        return keywords.contains(localName) || localName.contains("-") || localName.contains(":") || localName.contains(".");
    }

    protected String getTextNodeData(Text node) {
        return node.getData().trim();
    }

    protected boolean mixedContent(NodeList list) {
        boolean hasText = false;
        boolean hasElement = false;
        for (int i = 0, size = list.getLength(); i < size; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                hasElement = true;
            } else if (node instanceof Text) {
                String text = getTextNodeData((Text) node);
                if (text.length() > 0) {
                    hasText = true;
                }
            }
            if (hasText && hasElement) break;
        }
        return hasText && hasElement;
    }

    protected void printChildren(Node parent, Map namespaces) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            print(node, namespaces, false);
        }
    }

    protected String getLocalName(Node node) {
        String answer = node.getLocalName();
        if (answer == null) {
            answer = node.getNodeName();
        }
        return answer.trim();
    }

    protected void printEnd(String text, boolean endWithComma) {
        if (endWithComma) {
            print(text);
            println(",");
        } else {
            println(text);
        }
    }

    protected void println(String text) {
        out.println(text);
    }

    protected void print(String text) {
        out.print(text);
    }

    protected void printIndent() {
        out.printIndent();
    }
}
