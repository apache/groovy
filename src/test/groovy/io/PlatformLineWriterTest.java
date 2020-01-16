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
package groovy.io;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class PlatformLineWriterTest extends TestCase {

    public void testPlatformLineWriter() throws IOException, ClassNotFoundException {
        String LS = System.lineSeparator();
        Binding binding = new Binding();
        binding.setVariable("first", "Tom");
        binding.setVariable("last", "Adams");
        StringWriter stringWriter = new StringWriter();
        Writer platformWriter = new PlatformLineWriter(stringWriter);
        GroovyShell shell = new GroovyShell(binding);
        platformWriter.write(shell.evaluate("\"$first\\n$last\\n\"").toString());
        platformWriter.flush();
        assertEquals("Tom" + LS + "Adams" + LS, stringWriter.toString());
        stringWriter = new StringWriter();
        platformWriter = new PlatformLineWriter(stringWriter);
        platformWriter.write(shell.evaluate("\"$first\\r\\n$last\\r\\n\"").toString());
        platformWriter.flush();
        assertEquals("Tom" + LS + "Adams" + LS, stringWriter.toString());
    }
}
