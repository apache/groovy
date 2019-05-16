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
package groovy.util;

import groovy.lang.Writable;
import groovy.util.slurpersupport.GPathResult;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Used for pretty printing XML content and other XML related utilities.
 */
@Deprecated
public class XmlUtil {
    /**
     * Return a pretty version of the GPathResult.
     *
     * @param node a GPathResult to serialize to a String
     * @return the pretty String representation of the GPathResult
     */
    @Deprecated
    public static String serialize(GPathResult node) {
        return groovy.xml.XmlUtil.serialize(asString(node));
    }

    /**
     * Write a pretty version of the GPathResult to the OutputStream.
     *
     * @param node a GPathResult to serialize
     * @param os   the OutputStream to write to
     */
    @Deprecated
    public static void serialize(GPathResult node, OutputStream os) {
        groovy.xml.XmlUtil.serialize(asString(node), os);
    }

    /**
     * Write a pretty version of the GPathResult to the Writer.
     *
     * @param node a GPathResult to serialize
     * @param w    the Writer to write to
     */
    @Deprecated
    public static void serialize(GPathResult node, Writer w) {
        groovy.xml.XmlUtil.serialize(asString(node), w);
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

}