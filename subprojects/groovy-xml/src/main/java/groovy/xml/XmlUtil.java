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
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Writable;
import groovy.util.Node;
import groovy.xml.slurpersupport.GPathResult;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Used for pretty printing XML content and other XML related utilities.
 */
public class XmlUtil {
    /**
     * Return a pretty String version of the Element.
     *
     * @param element the Element to serialize
     * @return the pretty String representation of the Element
     */
    public static String serialize(Element element) {
        Writer sw = new StringBuilderWriter();
        serialize(new DOMSource(element), sw);
        return sw.toString();
    }

    /**
     * Write a pretty version of the Element to the OutputStream.
     *
     * @param element the Element to serialize
     * @param os      the OutputStream to write to
     */
    public static void serialize(Element element, OutputStream os) {
        Source source = new DOMSource(element);
        serialize(source, os);
    }

    /**
     * Write a pretty version of the Element to the Writer.
     *
     * @param element the Element to serialize
     * @param w       the Writer to write to
     */
    public static void serialize(Element element, Writer w) {
        Source source = new DOMSource(element);
        serialize(source, w);
    }

    /**
     * Return a pretty String version of the Node.
     *
     * @param node the Node to serialize
     * @return the pretty String representation of the Node
     */
    public static String serialize(Node node) {
        return serialize(asString(node));
    }

    /**
     * Write a pretty version of the Node to the OutputStream.
     *
     * @param node the Node to serialize
     * @param os   the OutputStream to write to
     */
    public static void serialize(Node node, OutputStream os) {
        serialize(asString(node), os);
    }

    /**
     * Write a pretty version of the Node to the Writer.
     *
     * @param node the Node to serialize
     * @param w    the Writer to write to
     */
    public static void serialize(Node node, Writer w) {
        serialize(asString(node), w);
    }

    /**
     * Return a pretty version of the GPathResult.
     *
     * @param node a GPathResult to serialize to a String
     * @return the pretty String representation of the GPathResult
     */
    public static String serialize(GPathResult node) {
        return serialize(asString(node));
    }

    /**
     * Write a pretty version of the GPathResult to the OutputStream.
     *
     * @param node a GPathResult to serialize
     * @param os   the OutputStream to write to
     */
    public static void serialize(GPathResult node, OutputStream os) {
        serialize(asString(node), os);
    }

    /**
     * Write a pretty version of the GPathResult to the Writer.
     *
     * @param node a GPathResult to serialize
     * @param w    the Writer to write to
     */
    public static void serialize(GPathResult node, Writer w) {
        serialize(asString(node), w);
    }

    /**
     * Return a pretty String version of the XML content produced by the Writable.
     *
     * @param writable the Writable to serialize
     * @return the pretty String representation of the content from the Writable
     */
    public static String serialize(Writable writable) {
        return serialize(asString(writable));
    }

    /**
     * Write a pretty version of the XML content produced by the Writable to the OutputStream.
     *
     * @param writable the Writable to serialize
     * @param os       the OutputStream to write to
     */
    public static void serialize(Writable writable, OutputStream os) {
        serialize(asString(writable), os);
    }

    /**
     * Write a pretty version of the XML content produced by the Writable to the Writer.
     *
     * @param writable the Writable to serialize
     * @param w        the Writer to write to
     */
    public static void serialize(Writable writable, Writer w) {
        serialize(asString(writable), w);
    }

    /**
     * Return a pretty version of the XML content contained in the given String.
     *
     * @param xmlString the String to serialize
     * @return the pretty String representation of the original content
     */
    public static String serialize(String xmlString) {
        Writer sw = new StringBuilderWriter();
        serialize(asStreamSource(xmlString), sw);
        return sw.toString();
    }

    /**
     * Write a pretty version of the given XML string to the OutputStream.
     *
     * @param xmlString the String to serialize
     * @param os        the OutputStream to write to
     */
    public static void serialize(String xmlString, OutputStream os) {
        serialize(asStreamSource(xmlString), os);
    }

    /**
     * Write a pretty version of the given XML string to the Writer.
     *
     * @param xmlString the String to serialize
     * @param w         the Writer to write to
     */
    public static void serialize(String xmlString, Writer w) {
        serialize(asStreamSource(xmlString), w);
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * optionally providing the schema sources to validate with.
     * The created SAXParser will be namespace-aware and not validate against DTDs.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param schemas        the schemas to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @see #newSAXParser(String, boolean, boolean, Source...)
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, Source... schemas) throws SAXException, ParserConfigurationException {
        return newSAXParser(schemaLanguage, true, false, schemas);
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * optionally providing the schema sources to validate with.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param namespaceAware will the parser be namespace aware
     * @param validating     will the parser also validate against DTDs
     * @param schemas        the schemas to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, boolean namespaceAware, boolean validating, Source... schemas) throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(namespaceAware);
        if (schemas.length != 0) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
            factory.setSchema(schemaFactory.newSchema(schemas));
        }
        SAXParser saxParser = factory.newSAXParser();
        if (schemas.length == 0) {
            saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", schemaLanguage);
        }
        return saxParser;
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * a File containing the schema to validate against.
     * The created SAXParser will be namespace-aware and not validate against DTDs.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param schema         a file containing the schema to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @see #newSAXParser(String, boolean, boolean, File)
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, File schema) throws SAXException, ParserConfigurationException {
        return newSAXParser(schemaLanguage, true, false, schema);
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * a File containing the schema to validate against.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param namespaceAware will the parser be namespace aware
     * @param validating     will the parser also validate against DTDs
     * @param schema         a file containing the schema to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, boolean namespaceAware, boolean validating, File schema) throws SAXException, ParserConfigurationException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        return newSAXParser(namespaceAware, validating, schemaFactory.newSchema(schema));
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * an URL pointing to the schema to validate against.
     * The created SAXParser will be namespace-aware and not validate against DTDs.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param schema         a URL pointing to the schema to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @see #newSAXParser(String, boolean, boolean, URL)
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, URL schema) throws SAXException, ParserConfigurationException {
        return newSAXParser(schemaLanguage, true, false, schema);
    }

    /**
     * Factory method to create a SAXParser configured to validate according to a particular schema language and
     * an URL pointing to the schema to validate against.
     *
     * @param schemaLanguage the schema language used, e.g. XML Schema or RelaxNG (as per the String representation in javax.xml.XMLConstants)
     * @param namespaceAware will the parser be namespace aware
     * @param validating     will the parser also validate against DTDs
     * @param schema         a URL pointing to the schema to validate against
     * @return the created SAXParser
     * @throws SAXException
     * @throws ParserConfigurationException
     * @since 1.8.7
     */
    public static SAXParser newSAXParser(String schemaLanguage, boolean namespaceAware, boolean validating, URL schema) throws SAXException, ParserConfigurationException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        return newSAXParser(namespaceAware, validating, schemaFactory.newSchema(schema));
    }

    /**
     * Escape the following characters {@code " ' & < >} with their XML entities, e.g.
     * {@code "bread" & "butter"} becomes {@code &quot;bread&quot; &amp; &quot;butter&quot}.
     * Notes:<ul>
     * <li>Supports only the five basic XML entities (gt, lt, quot, amp, apos)</li>
     * <li>Does not escape control characters</li>
     * <li>Does not support DTDs or external entities</li>
     * <li>Does not treat surrogate pairs specially</li>
     * <li>Does not perform Unicode validation on its input</li>
     * </ul>
     *
     * @param orig the original String
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding XML entities.
     * @see #escapeControlCharacters(String)
     */
    public static String escapeXml(String orig) {
        return StringGroovyMethods.collectReplacements(orig, new Closure<String>(null) {
            public String doCall(Character arg) {
                switch (arg) {
                    case '&':
                        return "&amp;";
                    case '<':
                        return "&lt;";
                    case '>':
                        return "&gt;";
                    case '"':
                        return "&quot;";
                    case '\'':
                        return "&apos;";
                }
                return null;
            }
        });
    }

    /**
     * Escape control characters (below 0x20) with their XML entities, e.g.
     * carriage return ({@code Ctrl-M or \r}) becomes {@code &#13;}
     * Notes:<ul>
     * <li>Does not escape non-ascii characters above 0x7e</li>
     * <li>Does not treat surrogate pairs specially</li>
     * <li>Does not perform Unicode validation on its input</li>
     * </ul>
     *
     * @param orig the original String
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding XML entities.
     * @see #escapeXml(String)
     */
    public static String escapeControlCharacters(String orig) {
        return StringGroovyMethods.collectReplacements(orig, new Closure<String>(null) {
            public String doCall(Character arg) {
                if (arg < 31) {
                        return "&#" + (int) arg + ";";
                }
                return null;
            }
        });
    }

    private static SAXParser newSAXParser(boolean namespaceAware, boolean validating, Schema schema1) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(namespaceAware);
        factory.setSchema(schema1);
        return factory.newSAXParser();
    }

    private static String asString(Node node) {
        Writer sw = new StringBuilderWriter();
        PrintWriter pw = new PrintWriter(sw);
        XmlNodePrinter nodePrinter = new XmlNodePrinter(pw);
        nodePrinter.setPreserveWhitespace(true);
        nodePrinter.print(node);
        return sw.toString();
    }

    private static String asString(GPathResult node) {
        // little bit of hackery to avoid Groovy dependency in this file
        try {
            Object builder = Class.forName("groovy.xml.StreamingMarkupBuilder").getDeclaredConstructor().newInstance();
            InvokerHelper.setProperty(builder, "encoding", "UTF-8");
            Writable w = (Writable) InvokerHelper.invokeMethod(builder, "bindNode", node);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + w.toString();
        } catch (Exception e) {
            return "Couldn't convert node to string because: " + e.getMessage();
        }
    }

    // TODO: replace with stream-based version

    private static String asString(Writable writable) {
        if (writable instanceof GPathResult) {
            return asString((GPathResult) writable); //GROOVY-4285
        }
        Writer sw = new StringBuilderWriter();
        try {
            writable.writeTo(sw);
        } catch (IOException e) {
            // ignore
        }
        return sw.toString();
    }

    private static StreamSource asStreamSource(String xmlString) {
        return new StreamSource(new StringReader(xmlString));
    }

    private static void serialize(Source source, OutputStream os) {
        serialize(source, new StreamResult(new OutputStreamWriter(os, StandardCharsets.UTF_8)));
    }

    private static void serialize(Source source, Writer w) {
        serialize(source, new StreamResult(w));
    }

    private static void serialize(Source source, StreamResult target) {
        TransformerFactory factory = TransformerFactory.newInstance();
        setIndent(factory, 2);
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            transformer.transform(source, target);
        }
        catch (TransformerException e) {
            throw new GroovyRuntimeException(e.getMessage());
        }
    }

    private static void setIndent(TransformerFactory factory, int indent) {
        // TODO: support older parser attribute values as well
        try {
            factory.setAttribute("indent-number", indent);
        } catch (IllegalArgumentException e) {
            // ignore for factories that don't support this
        }
    }
}