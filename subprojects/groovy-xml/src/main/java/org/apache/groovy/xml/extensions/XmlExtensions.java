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
package org.apache.groovy.xml.extensions;

import groovy.xml.XmlUtil;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class defines all the new XML-related groovy methods which enhance
 * the normal JDK XML classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class XmlExtensions {

    /**
     * Makes NodeList iterable by returning a read-only Iterator which traverses
     * over each Node.
     *
     * @param nodeList a NodeList
     * @return an Iterator for a NodeList
     * @since 1.0
     */
    public static Iterator<Node> iterator(final NodeList nodeList) {
        return new Iterator<Node>() {
            private int current /* = 0 */;

            @Override
            public boolean hasNext() {
                return current < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(current++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a NodeList iterator");
            }
        };
    }

    /**
     * Transforms the element to its text equivalent.
     * (The resulting string does not contain a xml declaration. Use {@code XmlUtil.serialize(element)} if you need the declaration.)
     *
     * @param element the element to serialize
     * @return the string representation of the element
     * @since 2.1
     */
    public static String serialize(Element element) {
        return XmlUtil.serialize(element).replaceFirst("<\\?xml version=\"1.0\".*\\?>", "");
    }

    /**
     * Enables {@code node as Type} coercion for XML Nodes.
     * Converts the Node to a Map via {@link groovy.util.Node#toMap()} and then
     * uses Groovy's standard Map coercion to produce the typed object.
     * Does not require Jackson on the classpath.
     *
     * @param self the Node to convert
     * @param type the target type
     * @param <T>  the target type
     * @return a typed object
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(groovy.util.Node self, Class<T> type) {
        if (type == Map.class || type == LinkedHashMap.class) {
            return (T) self.toMap();
        }
        Map<String, Object> map = self.toMap();
        return (T) InvokerHelper.invokeConstructorOf(type, new Object[]{map});
    }
}