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
package org.codehaus.groovy.runtime;

import groovy.xml.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

/**
 * This class defines all the new XML-related groovy methods which enhance
 * the normal JDK XML classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class XmlGroovyMethods {

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

            public boolean hasNext() {
                return current < nodeList.getLength();
            }

            public Node next() {
                return nodeList.item(current++);
            }

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
}