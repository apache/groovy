/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.control;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.FileWriter;

public abstract class XStreamUtils {
    public static void serialize(final String name, final Object ast) {
        XStream xstream = new XStream(new StaxDriver());
        try {
            xstream.toXML(ast, new FileWriter(name + ".xml"));
            System.out.println("Written AST to " + name + ".xml");
        } catch (Exception e) {
            System.out.println("Couldn't write to " + name + ".xml");
            e.printStackTrace();
        }
    }
}
