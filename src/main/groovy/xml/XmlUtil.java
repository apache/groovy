/*
 * Copyright 2003-2008 the original author or authors.
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
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Used for pretty printing XML content.
 */
public class XmlUtil {
    /**
     * Return a pretty String version of the Element.
     *
     * @param element the element to serialize
     * @return the pretty String representation of the element
     */
    public static String serialize(Element element) {
        StringWriter sw = new StringWriter();
        serialize(new DOMSource(element), sw);
        return sw.toString();
    }

    /**
     * Write a pretty version of the Element to the OutputStream.
     *
     * @param element the element to serialize
     * @param os      the outputstream to write to
     */
    public static void serialize(Element element, OutputStream os) {
        Source source = new DOMSource(element);
        serialize(source, os);
    }

    /**
     * Return a pretty String version of the XML content produced by the Writable.
     *
     * @param writable the writable to serialize
     * @return the pretty String representation of the element
     */
    public static String serialize(Writable writable) {
        return serialize(asString(writable));
    }

    /**
     * Write a pretty version of the XML content produced by the Writable to the OutputStream.
     *
     * @param writable the writable to serialize
     * @param os       the outputstream to write to
     */
    public static void serialize(Writable writable, OutputStream os) {
        serialize(asString(writable), os);
    }

    /**
     * Return a pretty version of the XML content contained in the given String.
     *
     * @param xmlString the string to serialize
     * @return the pretty String representation of the element
     */
    public static String serialize(String xmlString) {
        StringWriter sw = new StringWriter();
        serialize(asStreamSource(xmlString), sw);
        return sw.toString();
    }

    /**
     * Write a pretty version of the given XML string to the OutputStream.
     *
     * @param xmlString the string to serialize
     * @param os        the outputstream to write to
     */
    public static void serialize(String xmlString, OutputStream os) {
        serialize(asStreamSource(xmlString), os);
    }

    // TODO: replace with stream-based version
    private static String asString(Writable writable) {
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
        factory.setAttribute("indent-number", 2);
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
}