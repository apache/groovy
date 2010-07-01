/*
 * Copyright 2003-2010 the original author or authors.
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

import groovy.lang.Writable;
import groovy.util.Node;
import groovy.util.XmlNodePrinter;
import groovy.util.slurpersupport.GPathResult;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Used for pretty printing XML content.
 *
 * @author Paul King
 */
public class XmlUtil {
    /**
     * Return a pretty String version of the Element.
     *
     * @param element the Element to serialize
     * @return the pretty String representation of the Element
     */
    public static String serialize(Element element) {
        StringWriter sw = new StringWriter();
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
     * @param os       the OutputStream to write to
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
        StringWriter sw = new StringWriter();
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

    private static String asString(Node node) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        XmlNodePrinter nodePrinter = new XmlNodePrinter(pw);
        nodePrinter.setPreserveWhitespace(true);
        nodePrinter.print(node);
        return sw.toString();
    }

    private static String asString(GPathResult node) {
        // little bit of hackery to avoid Groovy dependency in this file
        try {
            Object builder = ((Class) Class.forName("groovy.xml.StreamingMarkupBuilder")).newInstance();
            Writable w = (Writable) InvokerHelper.invokeMethod(builder, "bindNode", node);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + w.toString();
        } catch (Exception e) {
            return "Couldn't convert node to string because: " + e.getMessage();
        }
    }

    // TODO: replace with stream-based version
    private static String asString(Writable writable) {
    	if(writable instanceof GPathResult) {
    		return asString((GPathResult) writable); //GROOVY-4285
    	}
    	
        Writer sw = new StringWriter();
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
        try {
            serialize(source, new StreamResult(new OutputStreamWriter(os, "UTF-8")));
        }
        catch (UnsupportedEncodingException e) {
            // ignore
        }
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
            // ignore
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