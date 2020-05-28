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
package org.codehaus.groovy.control;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;

public abstract class XStreamUtils {

    public static void serialize(final String name, final Object ast) {
        if (name == null || name.length() == 0) return;

        XStream xstream = new XStream(new StaxDriver());
        FileWriter astFileWriter = null;
        try {
            File astFile = astFile(name);
            if (astFile == null) {
                System.out.println("File-name for writing " + name + " AST could not be determined!");
                return;
            }
            astFileWriter = new FileWriter(astFile, false);
            xstream.toXML(ast, astFileWriter);
            System.out.println("Written AST to " + name + ".xml");

        } catch (Exception e) {
            System.out.println("Couldn't write to " + name + ".xml");
            e.printStackTrace();
        } finally {
            DefaultGroovyMethods.closeQuietly(astFileWriter);
        }
    }

    /**
     * Takes the incoming file-name and checks whether this is a URI using the <tt>file:</tt> protocol or a non-URI and treats
     * it accordingly.
     *
     * @return a file-name {@link java.io.File} representation or <tt>null</tt> if the file-name was in an invalid URI format
     */
    private static File astFile(final String uriOrFileName) {
        try {
            final String astFileName = uriOrFileName.replaceAll(" ", "%20") + ".xml";
            return uriOrFileName.startsWith("file:") ? new File(URI.create(astFileName)) : new File(astFileName);
        } catch (IllegalArgumentException e) {
            System.err.println("e = " + e);
            return null;
        }
    }
}
