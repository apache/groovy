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
package org.codehaus.groovy.antlr.treewalker;

import groovy.test.GroovyTestCase;
import org.codehaus.groovy.antlr.java.Java2GroovyProcessor;

public class Java2GroovyTest extends GroovyTestCase {

    public void testSimpleClass() throws Exception {
        assertEquals("private class Foo {int x = 1}", convert("private class Foo{int x=1;}"));
    }

    public void testStringLiteral() throws Exception {
        assertEquals("class Foo {String x = \"mooky\"}", convert("public class Foo{String x = \"mooky\";}"));
        assertEquals("class C {void m(String s) {File f = new File(\"sl\" + s)}}", convert("public class C{void m(String s) {File f=new File(\"sl\" + s);}}"));
    }

    private String convert(String input) throws Exception {
        return Java2GroovyProcessor.convert("Java2GroovyTest.java", input);
    }

    private String mindmap(String input) throws Exception {
        return Java2GroovyProcessor.mindmap(input);
    }

    private String nodePrinter(String input) throws Exception {
        return Java2GroovyProcessor.nodePrinter(input);
    }
}

